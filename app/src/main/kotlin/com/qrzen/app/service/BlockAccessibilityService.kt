package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.di.WidgetEntryPoint
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
import com.qrzen.app.ui.lock.LockScreenActivity
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class BlockAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QrZenAccessibility"
        @Volatile var isRunning: Boolean = false
        @Volatile var currentForegroundPackage: String? = null

        private val SYSTEM_EXEMPT_PACKAGES = setOf(
            "com.android.systemui",
            "com.android.settings",
            "com.miui.securitycenter",
            "com.miui.guardprovider",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.android.emergency"
        )
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error in accessibility service", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            WidgetEntryPoint::class.java
        )
    }

    private val launcherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val dialerPackages: Set<String> by lazy {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:")
        }
        packageManager.queryIntentActivities(dialIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val imePackages: Set<String> by lazy {
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.enabledInputMethodList?.map { it.packageName }?.toSet() ?: emptySet()
    }

    private fun isExemptFromAllowlist(pkg: String): Boolean {
        return pkg in SYSTEM_EXEMPT_PACKAGES || pkg in launcherPackages || pkg in dialerPackages || pkg in imePackages
    }

    private fun isDeviceLocked(): Boolean {
        val km = getSystemService(KeyguardManager::class.java) ?: return false
        return km.isKeyguardLocked
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        currentForegroundPackage = pkg

        scope.launch {
            val dao = entryPoint.appBlockDao()
            val now = System.currentTimeMillis()
            if (Prefs.pauseAllUntil > now) return@launch

            val allBlocks = dao.getAll().filter { it.isEnabled && !it.isArchived && now > it.pausedUntil }
            val activeBlocks = mutableListOf<AppBlock>()
            for (block in allBlocks) {
                if (isBlockActive(block)) activeBlocks.add(block)
            }

            val blocklistMatch = activeBlocks
                .filter { !it.isAllowlistMode }
                .firstOrNull { block ->
                    block.appPackages.split(",").map { it.trim() }.contains(pkg)
                }
            if (blocklistMatch != null) {
                launchLockScreen(pkg, blocklistMatch)
                return@launch
            }

            if (isExemptFromAllowlist(pkg) || isDeviceLocked()) return@launch

            val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode }
            if (allowlistBlocks.isNotEmpty()) {
                val allowedSets = allowlistBlocks.map { block ->
                    block.appPackages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .filterNot { Prefs.isAppTimerExpired(it) }
                        .toSet()
                }
                val intersection = allowedSets.reduce { acc, set -> acc.intersect(set) }
                if (!intersection.contains(pkg)) {
                    launchAllowlistOverlay(pkg, allowlistBlocks)
                }
            }
        }
    }

    /**
     * Style-aware block activation check.
     * - MANUAL: only active via blockNowUntil
     * - SCHEDULE: checks TimeBlock entries (falls back to legacy fields)
     * - WAIT_TIMER: checks MMKV for active blocking period
     * - USAGE_LIMIT: checks UsageStatsManager for exceeded limit
     */
    private suspend fun isBlockActive(block: AppBlock): Boolean {
        if (block.blockNowUntil > System.currentTimeMillis()) return true
        return when (block.blockingStyle) {
            UnlockMethodUtils.STYLE_MANUAL -> false
            UnlockMethodUtils.STYLE_SCHEDULE -> isScheduleActive(block)
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> isUsageLimitExceeded(block)
            UnlockMethodUtils.STYLE_WAIT_TIMER -> isWaitTimerBlocking(block)
            else -> isScheduleActive(block)
        }
    }

    private suspend fun isScheduleActive(block: AppBlock): Boolean {
        val timeBlocks = entryPoint.timeBlockDao().getByBlockId(block.id)
        if (timeBlocks.isEmpty()) return isLegacyScheduleActive(block)
        val now = LocalTime.now()
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return timeBlocks.any { tb ->
            if (tb.activeDays.getOrNull(dayIndex) != '1') return@any false
            val start = LocalTime.parse(tb.startTime, fmt)
            val end = LocalTime.parse(tb.endTime, fmt)
            if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
            else !now.isBefore(start) || !now.isAfter(end)
        }
    }

    private fun isLegacyScheduleActive(block: AppBlock): Boolean {
        val now = LocalTime.now()
        val start = LocalTime.parse(block.startTime, fmt)
        val end = LocalTime.parse(block.endTime, fmt)
        val timeOk = if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
        else !now.isBefore(start) || !now.isAfter(end)
        if (!timeOk) return false
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return block.activeDays.getOrNull(dayIndex) == '1'
    }

    /**
     * Check if wait timer is currently in a blocking period by reading MMKV state.
     * This is a fast check — the BackgroundService writes the blocking deadline to MMKV
     * when usage threshold is exceeded.
     */
    private fun isWaitTimerBlocking(block: AppBlock): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
        val blockingUntil = kv.decodeLong("wait_timer_blocking_${block.id}", 0L)
        return blockingUntil > System.currentTimeMillis()
    }

    /**
     * Check if usage limit is exceeded by querying UsageStatsManager.
     * Mirrors the logic in BackgroundService.isUsageLimitExceeded().
     */
    private fun isUsageLimitExceeded(block: AppBlock): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        val now = System.currentTimeMillis()
        val startTime = when (block.usageLimitPeriod) {
            "HOURLY" -> now - 3_600_000L
            else -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val events = usageStatsManager.queryEvents(startTime, now)
        val event = UsageEvents.Event()
        val foregroundStartTimes = mutableMapOf<String, Long>()
        var totalUsageMs = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg !in packages) continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundStartTimes[pkg] = event.timeStamp
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = foregroundStartTimes.remove(pkg)
                    if (start != null) {
                        totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
                    }
                }
            }
        }
        for ((_, start) in foregroundStartTimes) {
            totalUsageMs += (now - start).coerceAtLeast(0L)
        }

        val limitMs = block.usageLimitMinutes * 60_000L
        return totalUsageMs >= limitMs
    }

    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
        logBlockEvent(block, blockedPkg)
    }

    private fun launchAllowlistOverlay(blockedPkg: String, blocks: List<AppBlock>) {
        startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_IDS, blocks.map { it.id }.toIntArray())
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
        for (block in blocks) {
            logBlockEvent(block, blockedPkg)
        }
    }

    private fun logBlockEvent(block: AppBlock, blockedPkg: String) {
        scope.launch {
            entryPoint.blockEventDao().insert(
                BlockEvent(
                    blockId = block.id,
                    blockTitle = block.title,
                    packageName = blockedPkg,
                    eventType = "BLOCKED"
                )
            )
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        isRunning = false
        currentForegroundPackage = null
        super.onDestroy()
        scope.cancel()
    }
}
