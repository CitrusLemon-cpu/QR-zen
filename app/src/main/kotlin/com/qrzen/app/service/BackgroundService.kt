package com.qrzen.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.net.Uri
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.receiver.AlarmKeepaliveReceiver
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
import com.qrzen.app.ui.lock.LockScreenActivity
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundService : Service() {

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private val usageHandler = Handler(Looper.getMainLooper())
    private val previouslyActiveBlockIds = mutableSetOf<Int>()
    private var usagePollingActive = false
    private var lastBlockedPkg: String? = null
    private var lastBlockedTime = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    private val systemExemptPackages = setOf(
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

    private val launcherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val imePackages: Set<String> by lazy {
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.enabledInputMethodList?.map { it.packageName }?.toSet() ?: emptySet()
    }

    private val dialerPackages: Set<String> by lazy {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:") }
        packageManager.queryIntentActivities(dialIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            scope.launch { checkExpiredPauses() }
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    private val usageCheckRunnable = object : Runnable {
        override fun run() {
            scope.launch { checkForegroundApp() }
            if (usagePollingActive) {
                usageHandler.postDelayed(this, USAGE_POLL_INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val NOTIF_CHANNEL_ID = "qrzen_bg"
        private const val NOTIF_ID = 1001
        private const val CHECK_INTERVAL_MS = 60_000L
        private const val USAGE_POLL_INTERVAL_MS = 2_000L
        private const val BLOCK_COOLDOWN_MS = 3_000L

        fun start(context: Context) {
            val intent = Intent(context, BackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        acquireWakeLock()
        AlarmKeepaliveReceiver.schedule(applicationContext)
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
        return START_STICKY
    }

    private suspend fun checkExpiredPauses() {
        val now = System.currentTimeMillis()
        var shouldRefresh = false
        if (Prefs.pauseAllUntil != 0L && now > Prefs.pauseAllUntil) {
            Prefs.pauseAllUntil = 0L
            shouldRefresh = true
        }
        val allBlocks = dao.getAll()
        allBlocks
            .filter { block ->
                block.pausedUntil != 0L &&
                    block.pausedUntil != Long.MAX_VALUE &&
                    now > block.pausedUntil
            }
            .forEach { block ->
                dao.setPausedUntil(block.id, 0L)
                shouldRefresh = true
            }
        allBlocks
            .filter { block ->
                block.blockNowUntil != 0L &&
                    block.blockNowUntil != Long.MAX_VALUE &&
                    now > block.blockNowUntil
            }
            .forEach { block ->
                dao.update(block.copy(blockNowUntil = 0L))
                shouldRefresh = true
            }
        val currentlyActiveIds = mutableSetOf<Int>()
        allBlocks
            .filter { it.isEnabled && !it.isArchived && it.pausedUntil <= now }
            .forEach { block ->
                if (isBlockActive(block)) currentlyActiveIds.add(block.id)
            }
        val newlyActive = currentlyActiveIds - previouslyActiveBlockIds
        previouslyActiveBlockIds.clear()
        previouslyActiveBlockIds.addAll(currentlyActiveIds)
        if (newlyActive.isNotEmpty()) {
            sendToHome()
            shouldRefresh = true
        }
        if (shouldRefresh) WidgetRefresh.refresh(applicationContext)

        val hasActiveBlocks = currentlyActiveIds.isNotEmpty()
        val hasWaitTimerBlocks = allBlocks.any {
            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
                it.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER
        }
        val needsPolling = hasActiveBlocks || hasWaitTimerBlocks
        if (needsPolling) {
            startUsagePolling()
        } else {
            stopUsagePolling()
        }
    }

    private suspend fun isBlockActive(block: AppBlock, foregroundPkg: String? = null): Boolean {
        if (block.blockNowUntil > System.currentTimeMillis()) return true

        return when (block.blockingStyle) {
            UnlockMethodUtils.STYLE_MANUAL -> false
            UnlockMethodUtils.STYLE_SCHEDULE -> isScheduleActive(block)
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> isUsageLimitExceeded(block)
            UnlockMethodUtils.STYLE_WAIT_TIMER -> isWaitTimerBlocking(block, foregroundPkg)
            else -> isScheduleActive(block)
        }
    }

    private suspend fun isScheduleActive(block: AppBlock): Boolean {
        val timeBlocks = timeBlockDao.getByBlockId(block.id)
        if (timeBlocks.isEmpty()) {
            return isLegacyScheduleActive(block)
        }
        val now = LocalTime.now()
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7

        return timeBlocks.any { tb ->
            if (tb.activeDays.getOrNull(dayIndex) != '1') return@any false
            val fmt = DateTimeFormatter.ofPattern("HH:mm")
            val start = LocalTime.parse(tb.startTime, fmt)
            val end = LocalTime.parse(tb.endTime, fmt)
            if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
            else !now.isBefore(start) || !now.isAfter(end)
        }
    }

    private fun isLegacyScheduleActive(block: AppBlock): Boolean {
        val now = LocalTime.now()
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val start = LocalTime.parse(block.startTime, fmt)
        val end = LocalTime.parse(block.endTime, fmt)
        val timeOk = if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
        else !now.isBefore(start) || !now.isAfter(end)
        if (!timeOk) return false
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return block.activeDays.getOrNull(dayIndex) == '1'
    }

    private fun isUsageLimitExceeded(block: AppBlock): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return false
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

    private fun isWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
        val now = System.currentTimeMillis()
        val blockingUntilKey = "wait_timer_blocking_${block.id}"
        val remainingKey = "wait_timer_remaining_${block.id}"
        val lastUpdateKey = "wait_timer_last_update_${block.id}"
        val inAppKey = "wait_timer_in_app_${block.id}"
        val maxRemainingMs = block.waitTimerUseMinutes * 60_000L

        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)
        if (blockingUntil > now) return true

        if (blockingUntil > 0L) {
            kv.encode(remainingKey, maxRemainingMs)
            kv.encode(blockingUntilKey, 0L)
            kv.encode(lastUpdateKey, now)
            kv.encode(inAppKey, false)
            return false
        }

        if (foregroundPkg == null) return false

        var remaining = kv.decodeLong(remainingKey, -1L)
        if (remaining < 0L) {
            remaining = maxRemainingMs
            kv.encode(remainingKey, remaining)
            kv.encode(lastUpdateKey, now)
            kv.encode(inAppKey, false)
            return false
        }

        val lastUpdate = kv.decodeLong(lastUpdateKey, now)
        val elapsed = (now - lastUpdate).coerceAtLeast(0L)
        val wasInApp = kv.decodeBool(inAppKey, false)
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isInApp = foregroundPkg in packages

        if (isInApp) {
            if (wasInApp) {
                val decrement = elapsed.coerceAtMost(5_000L)
                remaining = (remaining - decrement).coerceAtLeast(0L)
            }
            kv.encode(inAppKey, true)
        } else {
            if (block.waitTimerAdaptive && !wasInApp && elapsed > 0L) {
                val refillRate = block.waitTimerUseMinutes.toDouble() / block.waitTimerWaitMinutes.toDouble()
                val refillMs = (elapsed * refillRate).toLong()
                remaining = (remaining + refillMs).coerceAtMost(maxRemainingMs)
            }
            kv.encode(inAppKey, false)
        }

        kv.encode(remainingKey, remaining)
        kv.encode(lastUpdateKey, now)

        if (remaining <= 0L) {
            val waitUntil = now + block.waitTimerWaitMinutes * 60_000L
            kv.encode(blockingUntilKey, waitUntil)
            return true
        }

        return false
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "QR Zen",
            NotificationManager.IMPORTANCE_MIN
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("QR Zen is active")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    private fun isExemptPackage(pkg: String): Boolean {
        return pkg == packageName ||
            pkg in systemExemptPackages ||
            pkg in launcherPackages ||
            pkg in imePackages ||
            pkg in dialerPackages
    }

    private fun isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(KeyguardManager::class.java) ?: return false
        return keyguardManager.isKeyguardLocked
    }

    private fun getForegroundPackage(): String? {
        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5_000
        val events = usageStatsManager.queryEvents(startTime, endTime)
        var lastPkg: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPkg = event.packageName
            }
        }
        if (lastPkg == null) {
            lastPkg = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                endTime - 10_000,
                endTime
            )
                ?.filter { it.totalTimeInForeground > 0 }
                ?.maxByOrNull { it.lastTimeUsed }
                ?.packageName
        }
        return lastPkg
    }

    private suspend fun checkForegroundApp() {
        if (isDeviceLocked()) return
        val now = System.currentTimeMillis()
        if (Prefs.pauseAllUntil > now) return

        val pkg = getForegroundPackage() ?: return
        if (isExemptPackage(pkg)) return
        if (pkg == lastBlockedPkg && now - lastBlockedTime < BLOCK_COOLDOWN_MS) return

        val allCandidates = dao.getAll().filter {
            it.isEnabled && !it.isArchived && now > it.pausedUntil
        }
        val activeBlocks = mutableListOf<AppBlock>()
        for (block in allCandidates) {
            if (isBlockActive(block, pkg)) activeBlocks.add(block)
        }

        val blocklistBlock = activeBlocks
            .filter { !it.isAllowlistMode }
            .firstOrNull { block ->
                block.appPackages.split(",").map { it.trim() }.contains(pkg)
            }
        if (blocklistBlock != null) {
            lastBlockedPkg = pkg
            lastBlockedTime = now
            launchLockScreen(pkg, blocklistBlock)
            return
        }

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
                lastBlockedPkg = pkg
                lastBlockedTime = now
                launchAllowlistOverlay(pkg, allowlistBlocks)
            }
        }
    }

    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
    }

    private fun launchAllowlistOverlay(blockedPkg: String, blocks: List<AppBlock>) {
        startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_IDS, blocks.map { it.id }.toIntArray())
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
    }

    private fun sendToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        applicationContext.startActivity(homeIntent)
    }

    private fun startUsagePolling() {
        if (usagePollingActive) return
        usagePollingActive = true
        usageHandler.post(usageCheckRunnable)
    }

    private fun stopUsagePolling() {
        usagePollingActive = false
        usageHandler.removeCallbacks(usageCheckRunnable)
        lastBlockedPkg = null
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(PowerManager::class.java)
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "QrZen::BackgroundService"
            )
            wakeLock?.acquire()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        stopUsagePolling()
        wakeLock?.let {
            if (it.isHeld) it.release()
            wakeLock = null
        }
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
