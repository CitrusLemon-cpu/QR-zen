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
import java.util.Calendar

class BlockAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QrZenAccessibility"
        @Volatile var isRunning: Boolean = false
        @Volatile var currentForegroundPackage: String? = null

        private val SYSTEM_EXEMPT_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.intentresolver",
            "com.android.documentsui",
            "com.google.android.documentsui",
            "com.miui.securitycenter",
            "com.miui.securitycore",
            "com.miui.guardprovider",
            "com.miui.systemui.plugin",
            "com.miui.mishare",
            "com.miui.volume",
            "com.miui.securityinputmethod",
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
            "com.samsung.android.app.sharelive",
            "com.android.emergency"
        )
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error in accessibility service", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private var accessibilitySettingsObserver: android.database.ContentObserver? = null

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

    private val shareHandlerPackages: Set<String> by lazy {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        }
        packageManager.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .filter { pkg ->
                try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (_: Exception) {
                    false
                }
            }
            .toSet()
    }

    private val imePackages: Set<String> by lazy {
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.enabledInputMethodList?.map { it.packageName }?.toSet() ?: emptySet()
    }

    private val systemNonLauncherCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private fun isSystemNonLauncherApp(pkg: String): Boolean {
        systemNonLauncherCache[pkg]?.let { return it }
        val result = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            isSystem && packageManager.getLaunchIntentForPackage(pkg) == null
        } catch (_: Exception) {
            false
        }
        systemNonLauncherCache[pkg] = result
        return result
    }

    private fun isExemptFromAllowlist(pkg: String): Boolean {
        return pkg in SYSTEM_EXEMPT_PACKAGES ||
            pkg in launcherPackages ||
            pkg in dialerPackages ||
            pkg in imePackages ||
            pkg in shareHandlerPackages ||
            isSystemNonLauncherApp(pkg)
    }

    private fun getAppLabel(pkg: String): String {
        return try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(pkg, 0)
            ).toString()
        } catch (_: Exception) {
            pkg
        }
    }

    private fun getFreshImePackages(): Set<String> {
        return getSystemService(InputMethodManager::class.java)
            ?.enabledInputMethodList
            ?.map { it.packageName }
            ?.toSet()
            ?: emptySet()
    }

    private fun isDeviceLocked(): Boolean {
        val km = getSystemService(KeyguardManager::class.java) ?: return false
        return km.isKeyguardLocked
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        accessibilitySettingsObserver?.let { contentResolver.unregisterContentObserver(it) }
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val enabled = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""
                val target = android.content.ComponentName(
                    this@BlockAccessibilityService,
                    BlockAccessibilityService::class.java
                )
                val stillEnabled = enabled.split(":").any { entry ->
                    entry.equals(target.flattenToString(), ignoreCase = true) ||
                        entry.equals(target.flattenToShortString(), ignoreCase = true)
                }
                if (!stillEnabled) {
                    isRunning = false
                    BackgroundService.start(this@BlockAccessibilityService)
                }
            }
        }
        accessibilitySettingsObserver = observer
        contentResolver.registerContentObserver(
            android.provider.Settings.Secure.getUriFor(
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ),
            false,
            observer
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        // Only filter genuinely transient windows (system UI, keyboards).
        // Launchers and dialers represent real navigation away from an app.
        if (pkg !in SYSTEM_EXEMPT_PACKAGES &&
            pkg !in imePackages &&
            pkg !in shareHandlerPackages &&
            !isSystemNonLauncherApp(pkg)
        ) {
            currentForegroundPackage = pkg
        }

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
                .filter { !it.isAllowlistMode && !it.blockAudio }
                .firstOrNull { block ->
                    block.appPackages.split(",").map { it.trim() }.contains(pkg)
                }
            if (blocklistMatch != null) {
                launchLockScreen(pkg, blocklistMatch)
                if (Prefs.diagnosticNotifications) {
                    val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
                    DiagnosticNotifier.notifyBlockTriggered(
                        context = applicationContext,
                        source = "Accessibility",
                        detectedPkg = pkg,
                        appLabel = getAppLabel(pkg),
                        triggerType = "Blocklist",
                        matchedBlocks = listOf(blocklistMatch),
                        cachedImePackages = imePackages,
                        freshImePackages = getFreshImePackages(),
                        isSystemNonLauncher = isSystemNonLauncher,
                        exemptReason = null,
                        extraInfo = null
                    )
                }
                return@launch
            }

            if (isExemptFromAllowlist(pkg) || isDeviceLocked()) return@launch

            val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode && !it.blockAudio }
            if (allowlistBlocks.isNotEmpty()) {
                val allowedSets = allowlistBlocks.map { block ->
                    block.appPackages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .filterNot { Prefs.isAppTimerExpired(block.id, it) }
                        .toSet()
                }
                val intersection = allowedSets.reduce { acc, set -> acc.intersect(set) }
                if (!intersection.contains(pkg)) {
                    launchAllowlistOverlay(pkg, allowlistBlocks)
                    if (Prefs.diagnosticNotifications) {
                        val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
                        DiagnosticNotifier.notifyBlockTriggered(
                            context = applicationContext,
                            source = "Accessibility",
                            detectedPkg = pkg,
                            appLabel = getAppLabel(pkg),
                            triggerType = "Allowlist",
                            matchedBlocks = allowlistBlocks,
                            cachedImePackages = imePackages,
                            freshImePackages = getFreshImePackages(),
                            isSystemNonLauncher = isSystemNonLauncher,
                            exemptReason = null,
                            extraInfo = "Pkg not in intersection of allowed sets"
                        )
                    }
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
            UnlockMethodUtils.STYLE_MANUAL -> true
            UnlockMethodUtils.STYLE_POMODORO -> {
                val state = UnlockMethodUtils.computePomodoroState(block)
                state.isInFocus
            }
            UnlockMethodUtils.STYLE_SCHEDULE -> {
                val scheduleActive = isScheduleActive(block)
                when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
                    UnlockMethodUtils.BREAK_NONE -> scheduleActive
                    UnlockMethodUtils.BREAK_POMODORO -> scheduleActive && isSchedulePomodoroBlocking(block)
                    UnlockMethodUtils.BREAK_WAIT_TIMER -> scheduleActive && isScheduleWaitTimerBlocking(block)
                    UnlockMethodUtils.BREAK_USAGE_LIMIT -> scheduleActive && isScheduleUsageLimitExceeded(block)
                    UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> scheduleActive && isScheduledAllowanceExhausted(block)
                    else -> scheduleActive
                }
            }
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> isUsageLimitExceeded(block)
            UnlockMethodUtils.STYLE_WAIT_TIMER -> isWaitTimerBlocking(block)
            else -> isScheduleActive(block)
        }
    }

    private suspend fun isScheduleActive(block: AppBlock): Boolean {
        val timeBlocks = entryPoint.timeBlockDao().getByBlockId(block.id)
        return if (timeBlocks.isEmpty()) {
            isLegacyScheduleActive(block)
        } else {
            UnlockMethodUtils.isScheduleCurrentlyActive(block, timeBlocks)
        }
    }

    private fun isLegacyScheduleActive(block: AppBlock): Boolean {
        return UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block) != null
    }

    private suspend fun isSchedulePomodoroBlocking(block: AppBlock): Boolean {
        val timeBlocks = entryPoint.timeBlockDao().getByBlockId(block.id)
        val windowStartMs = if (timeBlocks.isEmpty()) {
            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block)
        } else {
            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks)
        } ?: return true
        val now = System.currentTimeMillis()
        val elapsed = now - windowStartMs
        if (elapsed < 0L) return true
        val focusMs = block.pomodoroDurationMin * 60_000L
        val breakMs = block.pomodoroBreakMin * 60_000L
        val cycleMs = focusMs + breakMs
        if (cycleMs <= 0L) return true
        return elapsed % cycleMs < focusMs
    }

    private suspend fun isScheduleUsageLimitExceeded(block: AppBlock): Boolean {
        val timeBlocks = entryPoint.timeBlockDao().getByBlockId(block.id)
        if (timeBlocks.isEmpty()) return isUsageLimitExceeded(block)
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val hasTodayBlock = timeBlocks.any { it.activeDays.getOrNull(dayIndex) == '1' }
        if (!hasTodayBlock) return false
        return computeUsageLimitRemainingMs(block) <= 0L
    }

    private fun isScheduleWaitTimerBlocking(block: AppBlock): Boolean {
        return Prefs.getScheduleWtBlockingUntil(block.id) > System.currentTimeMillis()
    }

    private fun isScheduledAllowanceExhausted(block: AppBlock): Boolean {
        return Prefs.getSchedAllowanceRemaining(block.id) == 0L
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
        return computeUsageLimitRemainingMs(block) <= 0L
    }

    private fun computeUsageLimitRemainingMs(block: AppBlock): Long {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return block.usageLimitMinutes * 60_000L
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
        return (limitMs - totalUsageMs).coerceAtLeast(0L)
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
        accessibilitySettingsObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        accessibilitySettingsObserver = null
        BackgroundService.start(this)
        scope.cancel()
        super.onDestroy()
    }
}
