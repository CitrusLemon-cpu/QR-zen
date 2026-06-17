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
import android.content.IntentFilter
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
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.receiver.AlarmKeepaliveReceiver
import com.qrzen.app.receiver.PackageInstallReceiver
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
import com.qrzen.app.ui.lock.LockScreenActivity
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.widget.WidgetRefresh
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundService : Service() {

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var blockFolderDao: BlockFolderDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private val usageHandler = Handler(Looper.getMainLooper())
    private val previouslyActiveBlockIds = mutableSetOf<Int>()
    private var usagePollingActive = false
    private var lastBlockedPkg: String? = null
    private var lastBlockedTime = 0L
    private var wakeLock: PowerManager.WakeLock? = null
    private var waitTimerOverlay: WaitTimerOverlay? = null
    private var appTimerOverlay: AppTimerOverlay? = null
    private var accessibilityObserver: android.database.ContentObserver? = null
    @Volatile private var isAccessibilityEnabled = false
    private var accessibilityBlockOverlay: AccessibilityBlockOverlay? = null
    private var packageInstallReceiver: PackageInstallReceiver? = null
    private var audioBlockManager: AudioBlockManager? = null
    private var overlayHideCounter = 0
    private val pomodoroNotifIds = mutableMapOf<Int, Int>()
    private var nextPomodoroNotifId = 3000
    private val iconCache = mutableMapOf<String, android.graphics.drawable.Drawable>()

    private val systemExemptPackages = setOf(
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
        private const val OVERLAY_HIDE_DEBOUNCE = 2

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
        if (waitTimerOverlay == null) {
            waitTimerOverlay = WaitTimerOverlay(this)
        }
        if (appTimerOverlay == null) {
            appTimerOverlay = AppTimerOverlay(this)
        }
        if (accessibilityBlockOverlay == null) {
            accessibilityBlockOverlay = AccessibilityBlockOverlay(this)
        }
        if (audioBlockManager == null) {
            audioBlockManager = AudioBlockManager(this)
        }
        if (accessibilityObserver == null) {
            isAccessibilityEnabled = checkAccessibilityEnabled()
            val observer = object : android.database.ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    val wasEnabled = isAccessibilityEnabled
                    isAccessibilityEnabled = checkAccessibilityEnabled()
                    if (wasEnabled && !isAccessibilityEnabled) {
                        scope.launch { checkForegroundApp() }
                    } else if (!wasEnabled && isAccessibilityEnabled) {
                        accessibilityBlockOverlay?.hide()
                    }
                }
            }
            accessibilityObserver = observer
            contentResolver.registerContentObserver(
                android.provider.Settings.Secure.getUriFor(
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ),
                false,
                observer
            )
        }
        if (packageInstallReceiver == null) {
            val receiver = PackageInstallReceiver()
            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
                addDataScheme("package")
            }
            registerReceiver(receiver, filter)
            packageInstallReceiver = receiver
        }
        acquireWakeLock()
        AlarmKeepaliveReceiver.schedule(applicationContext)
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
        return START_STICKY
    }

    private fun checkAccessibilityEnabled(): Boolean {
        val enabled = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val target = android.content.ComponentName(this, BlockAccessibilityService::class.java)
        val flatFull = target.flattenToString()
        val flatShort = target.flattenToShortString()
        return enabled.split(":").any { entry ->
            entry.equals(flatFull, ignoreCase = true) ||
                entry.equals(flatShort, ignoreCase = true)
        }
    }

    private suspend fun checkExpiredPauses() {
        val now = System.currentTimeMillis()
        var shouldRefresh = false
        if (Prefs.pauseAllUntil != 0L && now > Prefs.pauseAllUntil) {
            Prefs.pauseAllUntil = 0L
            shouldRefresh = true
        }
        var allBlocks = dao.getAll()
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
        blockFolderDao.getAll()
            .filter { folder ->
                folder.pausedUntil != 0L &&
                    folder.pausedUntil != Long.MAX_VALUE &&
                    now > folder.pausedUntil
            }
            .forEach { folder ->
                blockFolderDao.setPausedUntil(folder.id, 0L)
                dao.setPausedUntilByFolderId(folder.id, 0L)
                shouldRefresh = true
            }
        if (shouldRefresh) {
            allBlocks = dao.getAll()
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
        allBlocks
            .filter { block ->
                block.toggleLockUntil != 0L &&
                    block.toggleLockUntil != Long.MAX_VALUE &&
                    now > block.toggleLockUntil
            }
            .forEach { block ->
                if (block.autoDisableOnToggleLockExpiry || block.isAllowlistMode) {
                    Prefs.clearAllowlistUsageTimer(block.id)
                    if (block.isAllowlistMode) {
                        Prefs.clearAppTimersForBlock(block.id)
                    }
                    Prefs.clearWaitTimerState(block.id)
                    Prefs.clearScheduleWtState(block.id)
                    dao.update(
                        block.copy(
                            isEnabled = false,
                            toggleLockUntil = 0L,
                            autoDisableOnToggleLockExpiry = false,
                            activeUntil = 0L
                        )
                    )
                } else {
                    dao.update(
                        block.copy(
                            toggleLockUntil = 0L,
                            autoDisableOnToggleLockExpiry = false
                        )
                    )
                }
                shouldRefresh = true
            }
        allBlocks
            .filter { block ->
                block.activeUntil != 0L &&
                    block.activeUntil != Long.MAX_VALUE &&
                    now > block.activeUntil
            }
            .forEach { block ->
                Prefs.clearAllowlistUsageTimer(block.id)
                if (block.isAllowlistMode) {
                    Prefs.clearAppTimersForBlock(block.id)
                }
                Prefs.clearWaitTimerState(block.id)
                Prefs.clearScheduleWtState(block.id)
                dao.update(
                    block.copy(
                        isEnabled = false,
                        activeUntil = 0L,
                        toggleLockUntil = 0L,
                        autoDisableOnToggleLockExpiry = false
                    )
                )
                shouldRefresh = true
            }
        allBlocks
            .filter { block ->
                block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
                    block.isEnabled &&
                    block.pomodoroRoundsTotal > 0
            }
            .forEach { block ->
                val state = UnlockMethodUtils.computePomodoroState(block, now)
                if (!state.isSessionActive) {
                    dao.update(
                        block.copy(
                            isEnabled = false,
                            pomodoroRoundsTotal = 0,
                            pomodoroSessionStartMillis = 0L,
                            toggleLockUntil = 0L,
                            autoDisableOnToggleLockExpiry = false,
                            activeUntil = 0L
                        )
                    )
                    cancelPomodoroBreakNotification(block.id)
                    shouldRefresh = true
                } else if (state.isInBreak) {
                    showPomodoroBreakNotification(block, state)
                } else {
                    cancelPomodoroBreakNotification(block.id)
                }
            }
        if (shouldRefresh) {
            allBlocks = dao.getAll()
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
        resetAppTimersOnWindowChange(allBlocks)
        refreshAudioBlocking(allBlocks)

        val hasActiveBlocks = currentlyActiveIds.isNotEmpty()
        val hasWaitTimerBlocks = allBlocks.any {
            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
                it.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER
        }
        val hasPomodoroBlocks = allBlocks.any {
            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
                it.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
                it.pomodoroRoundsTotal > 0
        }
        val hasScheduleBreakBlocks = allBlocks.any {
            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
                it.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
                it.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_NONE
        }
        val needsPolling =
            hasActiveBlocks || hasWaitTimerBlocks || hasPomodoroBlocks || hasScheduleBreakBlocks
        if (needsPolling) {
            startUsagePolling()
        } else {
            stopUsagePolling()
        }
    }

    private suspend fun isBlockActive(block: AppBlock, foregroundPkg: String? = null): Boolean {
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
                    UnlockMethodUtils.BREAK_WAIT_TIMER -> scheduleActive && isScheduleWaitTimerBlocking(block, foregroundPkg)
                    UnlockMethodUtils.BREAK_USAGE_LIMIT -> scheduleActive && isScheduleUsageLimitExceeded(block)
                    UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> scheduleActive && isScheduledAllowanceExhausted(block)
                    else -> scheduleActive
                }
            }
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> isUsageLimitExceeded(block)
            UnlockMethodUtils.STYLE_WAIT_TIMER -> isWaitTimerBlocking(block, foregroundPkg)
            else -> isScheduleActive(block)
        }
    }

    private suspend fun isScheduleActive(block: AppBlock): Boolean {
        return isScheduleActive(block, timeBlockDao.getByBlockId(block.id))
    }

    private fun isScheduleActive(block: AppBlock, timeBlocks: List<TimeBlock>): Boolean {
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
        val timeBlocks = timeBlockDao.getByBlockId(block.id)
        val windowStartMs = if (timeBlocks.isEmpty()) {
            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block)
        } else {
            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks)
        } ?: return true
        return isSchedulePomodoroBlocking(block, windowStartMs)
    }

    private fun isSchedulePomodoroBlocking(block: AppBlock, windowStartMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val elapsed = now - windowStartMs
        if (elapsed < 0L) return true
        val focusMs = block.pomodoroDurationMin * 60_000L
        val breakMs = block.pomodoroBreakMin * 60_000L
        val cycleMs = focusMs + breakMs
        if (cycleMs <= 0L) return true
        val positionInCycle = elapsed % cycleMs
        return positionInCycle < focusMs
    }

    private suspend fun isScheduleUsageLimitExceeded(block: AppBlock): Boolean {
        val timeBlocks = timeBlockDao.getByBlockId(block.id)
        if (timeBlocks.isEmpty()) {
            return isUsageLimitExceeded(block)
        }
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val hasTodayBlock = timeBlocks.any { it.activeDays.getOrNull(dayIndex) == '1' }
        if (!hasTodayBlock) return false
        return computeUsageLimitRemainingMs(block) <= 0L
    }

    private fun isScheduleWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
        return Prefs.getScheduleWtBlockingUntil(block.id) > System.currentTimeMillis()
    }

    private fun isScheduledAllowanceExhausted(block: AppBlock): Boolean {
        return Prefs.getSchedAllowanceRemaining(block.id) == 0L
    }

    private fun isUsageLimitExceeded(block: AppBlock): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false
        return computeUsageLimitRemainingMs(block) <= 0L
    }

    private fun computeUsageLimitRemainingMs(block: AppBlock): Long {
        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return block.usageLimitMinutes * 60_000L
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
                    if (start != null) totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
                }
            }
        }
        for ((_, start) in foregroundStartTimes) {
            totalUsageMs += (now - start).coerceAtLeast(0L)
        }
        val limitMs = block.usageLimitMinutes * 60_000L
        return (limitMs - totalUsageMs).coerceAtLeast(0L)
    }

    private fun isWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val kv = MMKV.defaultMMKV()
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

    private suspend fun trackScheduleBreakState(blocks: List<AppBlock>, foregroundPkg: String?) {
        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
            when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
                UnlockMethodUtils.BREAK_WAIT_TIMER -> {
                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
                    trackScheduleWaitTimerState(block, foregroundPkg, isScheduleActive(block, timeBlocks))
                }
                UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> {
                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
                    trackScheduledAllowanceState(block, foregroundPkg, isScheduleActive(block, timeBlocks), timeBlocks)
                }
            }
        }
    }

    private fun trackScheduleWaitTimerState(block: AppBlock, foregroundPkg: String?, scheduleActive: Boolean) {
        val blockId = block.id
        val now = System.currentTimeMillis()
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages

        if (!scheduleActive) {
            if (block.waitTimerAdaptive) {
                Prefs.clearScheduleWtState(blockId)
            }
            Prefs.setScheduleWtLastTick(blockId, 0L)
            return
        }

        val blockingUntil = Prefs.getScheduleWtBlockingUntil(blockId)
        if (blockingUntil > now) {
            Prefs.setScheduleWtLastTick(blockId, 0L)
            return
        }

        if (blockingUntil > 0L && blockingUntil <= now) {
            Prefs.clearScheduleWtState(blockId)
            return
        }

        val budgetMs = block.waitTimerUseMinutes * 60_000L
        if (isUsingBlockedApp) {
            val lastTick = Prefs.getScheduleWtLastTick(blockId)
            var usedMs = Prefs.getScheduleWtUsedMs(blockId)
            if (lastTick > 0L) {
                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
                usedMs = (usedMs + delta).coerceAtMost(budgetMs)
            }
            if (usedMs >= budgetMs) {
                Prefs.setScheduleWtBlockingUntil(blockId, now + block.waitTimerWaitMinutes * 60_000L)
                Prefs.setScheduleWtUsedMs(blockId, 0L)
                Prefs.setScheduleWtLastTick(blockId, 0L)
            } else {
                Prefs.setScheduleWtUsedMs(blockId, usedMs)
                Prefs.setScheduleWtLastTick(blockId, now)
            }
            return
        }

        val lastTick = Prefs.getScheduleWtLastTick(blockId)
        if (block.waitTimerAdaptive) {
            if (lastTick > 0L) {
                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
                val refillRate = if (block.waitTimerWaitMinutes > 0) {
                    block.waitTimerUseMinutes.toDouble() / block.waitTimerWaitMinutes.toDouble()
                } else {
                    0.0
                }
                val refillMs = (delta * refillRate).toLong()
                val usedMs = (Prefs.getScheduleWtUsedMs(blockId) - refillMs).coerceAtLeast(0L)
                Prefs.setScheduleWtUsedMs(blockId, usedMs)
            }
            Prefs.setScheduleWtLastTick(blockId, now)
        } else {
            Prefs.setScheduleWtLastTick(blockId, 0L)
        }
    }

    private fun trackScheduledAllowanceState(
        block: AppBlock,
        foregroundPkg: String?,
        scheduleActive: Boolean,
        timeBlocks: List<TimeBlock>
    ) {
        val blockId = block.id
        val now = System.currentTimeMillis()

        if (!scheduleActive) {
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
            return
        }

        val currentWindowStart = if (timeBlocks.isEmpty()) {
            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block, now)
        } else {
            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks, now)
        } ?: return
        val savedWindowStart = Prefs.getSchedAllowanceWindowStart(blockId)
        val maxAllowanceMs = block.scheduledAllowanceMinutes * 60_000L

        if (savedWindowStart != currentWindowStart || Prefs.getSchedAllowanceRemaining(blockId) < 0L) {
            Prefs.setSchedAllowanceRemaining(blockId, maxAllowanceMs)
            Prefs.setSchedAllowanceWindowStart(blockId, currentWindowStart)
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
        }

        val remaining = Prefs.getSchedAllowanceRemaining(blockId)
        if (remaining <= 0L) {
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
            return
        }

        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages
        if (isUsingBlockedApp) {
            val lastTick = Prefs.getSchedAllowanceLastTick(blockId)
            if (lastTick > 0L) {
                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
                val newRemaining = (remaining - delta).coerceAtLeast(0L)
                Prefs.setSchedAllowanceRemaining(blockId, newRemaining)
                Prefs.setSchedAllowanceLastTick(blockId, if (newRemaining > 0L) now else 0L)
            } else {
                Prefs.setSchedAllowanceLastTick(blockId, now)
            }
        } else {
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
        }
    }

    private suspend fun resetAppTimersOnWindowChange(blocks: List<AppBlock>) {
        val now = System.currentTimeMillis()
        val todayStartMs = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        for (block in blocks) {
            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue

            when (block.blockingStyle) {
                UnlockMethodUtils.STYLE_SCHEDULE -> {
                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
                    val currentWindowStart = if (timeBlocks.isEmpty()) {
                        UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block, now)
                    } else {
                        UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks, now)
                    }

                    if (currentWindowStart == null) {
                        Prefs.setAppTimerWindowStart(block.id, 0L)
                        continue
                    }

                    val savedWindowStart = Prefs.getAppTimerWindowStart(block.id)
                    if (savedWindowStart != currentWindowStart) {
                        Prefs.resetAppTimersForBlock(block.id)
                        Prefs.setAppTimerWindowStart(block.id, currentWindowStart)
                    }
                }

                UnlockMethodUtils.STYLE_MANUAL -> {
                    val savedWindowStart = Prefs.getAppTimerWindowStart(block.id)
                    if (savedWindowStart != todayStartMs) {
                        Prefs.resetAppTimersForBlock(block.id)
                        Prefs.setAppTimerWindowStart(block.id, todayStartMs)
                    }
                }
            }
        }
    }

    private fun pauseScheduleBreakUsageTracking(blocks: List<AppBlock>) {
        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
            when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
                UnlockMethodUtils.BREAK_WAIT_TIMER -> Prefs.setScheduleWtLastTick(block.id, 0L)
                UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> Prefs.setSchedAllowanceLastTick(block.id, 0L)
            }
        }
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

    private fun showPomodoroBreakNotification(
        block: AppBlock,
        state: UnlockMethodUtils.PomodoroState
    ) {
        val nm = getSystemService(NotificationManager::class.java)
        val channelId = "qrzen_pomodoro_break"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pomodoro Breaks",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            nm.createNotificationChannel(channel)
        }
        val notifId = pomodoroNotifIds.getOrPut(block.id) { nextPomodoroNotifId++ }
        val breakEndMs = System.currentTimeMillis() + state.periodRemainingMs
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("☕ Break – ${block.title}")
            .setContentText(
                "Round ${state.currentRound}/${state.totalRounds} complete. Next round in ${
                    UnlockMethodUtils.formatCountdown(state.periodRemainingMs)
                }"
            )
            .setWhen(breakEndMs)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        nm.notify(notifId, notification)
    }

    private fun cancelPomodoroBreakNotification(blockId: Int) {
        val notifId = pomodoroNotifIds.remove(blockId) ?: return
        getSystemService(NotificationManager::class.java).cancel(notifId)
    }

    private fun isExemptPackage(pkg: String): Boolean {
        return pkg == packageName ||
            pkg in systemExemptPackages ||
            pkg in launcherPackages ||
            pkg in imePackages ||
            pkg in dialerPackages ||
            pkg in shareHandlerPackages ||
            isSystemNonLauncherApp(pkg)
    }

    private fun isPackageTrackedByBlock(block: AppBlock, pkg: String): Boolean {
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return pkg in packages
    }

    private fun isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(KeyguardManager::class.java) ?: return false
        return keyguardManager.isKeyguardLocked
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

    private fun getForegroundPackage(): String? {
        val accessibilityPkg = BlockAccessibilityService.currentForegroundPackage
        if (accessibilityPkg != null && BlockAccessibilityService.isRunning) {
            return accessibilityPkg
        }

        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60_000
        val events = usageStatsManager.queryEvents(startTime, endTime)
        var lastPkg: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPkg = event.packageName
            }
        }
        return lastPkg
    }

    private suspend fun refreshAudioBlocking(blocks: List<AppBlock>, foregroundPkg: String? = null) {
        val now = System.currentTimeMillis()
        val packages = if (Prefs.pauseAllUntil > now) {
            emptySet()
        } else {
            blocks
                .filter { it.blockAudio && it.isEnabled && !it.isArchived && now > it.pausedUntil }
                .filter { isBlockActive(it, foregroundPkg) }
                .flatMap { block ->
                    block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }
                .minus(foregroundPkg.orEmpty())
                .toSet()
        }
        val manager = audioBlockManager ?: return
        manager.updateBlockedPackages(packages)
        if (packages.isEmpty()) {
            manager.stop()
        } else {
            manager.start()
        }
    }

    private suspend fun checkForegroundApp() {
        isAccessibilityEnabled = checkAccessibilityEnabled()
        val now = System.currentTimeMillis()
        val allCandidates = dao.getAll().filter {
            it.isEnabled && !it.isArchived && now > it.pausedUntil
        }
        refreshAudioBlocking(allCandidates)
        if (isDeviceLocked()) {
            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
            waitTimerOverlay?.hide()
            pauseUsageTimers(allCandidates)
            pauseScheduleBreakUsageTracking(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        if (Prefs.pauseAllUntil > now) {
            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
            waitTimerOverlay?.hide()
            pauseUsageTimers(allCandidates)
            pauseScheduleBreakUsageTracking(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }

        val pkg = getForegroundPackage()
        trackScheduleBreakState(allCandidates, pkg)
        if (pkg == null) {
            updateTimerOverlays(allCandidates, null)
            pauseUsageTimers(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        if (isExemptPackage(pkg)) {
            updateTimerOverlays(allCandidates, null)
            pauseUsageTimers(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        if (pkg == lastBlockedPkg && now - lastBlockedTime < BLOCK_COOLDOWN_MS) {
            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
            waitTimerOverlay?.hide()
            pauseUsageTimers(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        val activeBlocks = mutableListOf<AppBlock>()
        for (block in allCandidates) {
            if (isBlockActive(block, pkg)) activeBlocks.add(block)
        }
        refreshAudioBlocking(allCandidates, pkg)

        val blocklistBlock = activeBlocks
            .filter { !it.isAllowlistMode && !it.blockAudio }
            .firstOrNull { block ->
                block.appPackages.split(",").map { it.trim() }.contains(pkg)
            }

        val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode && !it.blockAudio }
        var allowedForegroundPkg: String? = pkg
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
                lastBlockedPkg = pkg
                lastBlockedTime = now
                overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
                waitTimerOverlay?.hide()
                allowedForegroundPkg = null
                accessibilityBlockOverlay?.hide()
            }
        }

        if (Prefs.diagnosticNotifications) {
            val freshImePackages = getFreshImePackages()
            val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
            val exemptReason = when {
                pkg == packageName -> "self"
                pkg in systemExemptPackages -> "system"
                pkg in launcherPackages -> "launcher"
                pkg in imePackages -> "keyboard/IME"
                pkg in dialerPackages -> "dialer"
                pkg in shareHandlerPackages -> "share handler"
                isSystemNonLauncher -> "system non-launcher"
                else -> null
            }
            DiagnosticNotifier.notifyPollState(
                context = applicationContext,
                source = if (isAccessibilityEnabled) "Accessibility" else "UsageStats",
                detectedPkg = pkg,
                appLabel = getAppLabel(pkg),
                cachedImePackages = imePackages,
                freshImePackages = freshImePackages,
                isSystemNonLauncher = isSystemNonLauncher,
                isExempt = isExemptPackage(pkg),
                exemptReason = exemptReason,
                activeBlockCount = activeBlocks.size,
                blocklistMatchName = blocklistBlock?.title,
                allowlistResult = if (allowlistBlocks.isNotEmpty()) {
                    if (allowedForegroundPkg != null) "allowed" else "BLOCKED"
                } else {
                    null
                }
            )
        }

        if (blocklistBlock != null) {
            lastBlockedPkg = pkg
            lastBlockedTime = now
            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
            waitTimerOverlay?.hide()
            pauseUsageTimers(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            launchLockScreen(pkg, blocklistBlock)
            if (Prefs.diagnosticNotifications) {
                val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
                DiagnosticNotifier.notifyBlockTriggered(
                    context = applicationContext,
                    source = "UsageStats",
                    detectedPkg = pkg,
                    appLabel = getAppLabel(pkg),
                    triggerType = "Blocklist",
                    matchedBlocks = listOf(blocklistBlock),
                    cachedImePackages = imePackages,
                    freshImePackages = getFreshImePackages(),
                    isSystemNonLauncher = isSystemNonLauncher,
                    exemptReason = null,
                    extraInfo = null
                )
            }
            return
        }

        if (allowedForegroundPkg == null) {
            launchAllowlistOverlay(pkg, allowlistBlocks)
            if (Prefs.diagnosticNotifications) {
                val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
                DiagnosticNotifier.notifyBlockTriggered(
                    context = applicationContext,
                    source = "UsageStats",
                    detectedPkg = pkg,
                    appLabel = getAppLabel(pkg),
                    triggerType = "Allowlist",
                    matchedBlocks = allowlistBlocks,
                    cachedImePackages = imePackages,
                    freshImePackages = getFreshImePackages(),
                    isSystemNonLauncher = isSystemNonLauncher,
                    exemptReason = null,
                    extraInfo = "Pkg not in allowlist intersection"
                )
            }
        }

        updateTimerOverlays(allCandidates, pkg)
        if (!isAccessibilityEnabled) {
            val accessibilityMessage = when {
                allCandidates.any { block ->
                    block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
                        block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } == UnlockMethodUtils.BREAK_WAIT_TIMER &&
                        isPackageTrackedByBlock(block, pkg) &&
                        isScheduleActive(block)
                } -> getString(R.string.accessibility_block_wait_timer)
                allCandidates.any { block ->
                    block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
                        block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } == UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE &&
                        isPackageTrackedByBlock(block, pkg) &&
                        isScheduleActive(block)
                } -> getString(R.string.accessibility_block_scheduled_allowance)
                allCandidates.any { block ->
                    block.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER &&
                        isPackageTrackedByBlock(block, pkg)
                } -> getString(R.string.accessibility_block_wait_timer)
                else -> null
            }
            if (accessibilityMessage != null) {
                accessibilityBlockOverlay?.show(accessibilityMessage)
                sendToHome()
                return
            }
        } else {
            accessibilityBlockOverlay?.hide()
        }
        accessibilityBlockOverlay?.hide()
        trackAllowlistUsageTimers(activeBlocks, allowedForegroundPkg ?: "")
        trackPerAppTimers(activeBlocks, allowedForegroundPkg ?: "")
        updateAppTimerOverlay(activeBlocks, allowedForegroundPkg)
    }

    private fun pauseUsageTimers(blocks: List<AppBlock>) {
        trackAllowlistUsageTimers(blocks, "")
        trackPerAppTimers(blocks, "")
    }

    private fun trackAllowlistUsageTimers(blocks: List<AppBlock>, foregroundPkg: String) {
        // Block-level timer is now pure wall-clock via activeUntil;
        // expiry handled by checkExpiredPauses().
    }

    private fun trackPerAppTimers(blocks: List<AppBlock>, foregroundPkg: String) {
        val now = System.currentTimeMillis()
        for (block in blocks) {
            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
            for (pkg in block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                val remaining = Prefs.getAppTimerRemaining(block.id, pkg)
                if (remaining <= 0L) continue

                if (Prefs.getAppTimerOriginal(block.id, pkg) <= 0L) {
                    Prefs.setAppTimerOriginal(block.id, pkg, remaining)
                }

                if (pkg == foregroundPkg) {
                    val lastFg = Prefs.getAppTimerLastFg(block.id, pkg)
                    if (lastFg > 0L) {
                        val elapsed = (now - lastFg).coerceAtLeast(0L).coerceAtMost(5_000L)
                        val newRemaining = (remaining - elapsed).coerceAtLeast(0L)
                        Prefs.setAppTimerRemaining(block.id, pkg, newRemaining)
                        if (newRemaining <= 0L) {
                            Prefs.setAppTimerLastFg(block.id, pkg, 0L)
                            continue
                        }
                    }
                    Prefs.setAppTimerLastFg(block.id, pkg, now)
                } else {
                    Prefs.setAppTimerLastFg(block.id, pkg, 0L)
                }
            }
        }
    }

    private fun updateAppTimerOverlay(blocks: List<AppBlock>, foregroundPkg: String?) {
        if (foregroundPkg == null || isExemptPackage(foregroundPkg)) {
            appTimerOverlay?.hide()
            return
        }

        val entries = mutableListOf<AppTimerOverlay.TimerEntry>()
        var perAppRemaining = -1L
        for (block in blocks) {
            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
            val remaining = Prefs.getAppTimerRemaining(block.id, foregroundPkg)
            if (remaining > 0L) {
                perAppRemaining = if (perAppRemaining < 0L) remaining else minOf(perAppRemaining, remaining)
            }
        }
        if (perAppRemaining > 0L) {
            val icon = getOrLoadIcon(foregroundPkg)
            if (icon != null) {
                entries.add(AppTimerOverlay.TimerEntry(foregroundPkg, icon, perAppRemaining))
            }
        }

        if (entries.isNotEmpty()) {
            appTimerOverlay?.update(entries)
        } else {
            appTimerOverlay?.hide()
        }
    }

    private fun getOrLoadIcon(pkg: String): android.graphics.drawable.Drawable? {
        iconCache[pkg]?.let { return it }
        return try {
            packageManager.getApplicationIcon(pkg).also { iconCache[pkg] = it }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun updateTimerOverlays(blocks: List<AppBlock>, foregroundPkg: String?) {
        val kv = MMKV.defaultMMKV()
        val now = System.currentTimeMillis()
        val entries = mutableListOf<WaitTimerOverlay.TimerEntry>()

        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_WAIT_TIMER) continue
            if (!block.showTimer) continue
            if (kv.decodeBool("wait_timer_in_app_${block.id}", false)) {
                val remaining = kv.decodeLong("wait_timer_remaining_${block.id}", -1L)
                if (remaining > 0L) {
                    entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
                }
            }
        }

        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
            if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_WAIT_TIMER) continue
            if (!block.showTimer) continue
            val blockingUntil = Prefs.getScheduleWtBlockingUntil(block.id)
            if (blockingUntil > now) {
                entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, blockingUntil - now))
            }
        }

        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
            if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_POMODORO) continue
            if (!block.showTimer) continue
            val timeBlocks = timeBlockDao.getByBlockId(block.id)
            val phase = UnlockMethodUtils.computeSchedulePomodoroPhase(block, timeBlocks, now)
            if (phase.isActive && phase.phaseRemainingMs > 0L) {
                val phaseLabel = if (phase.isInFocus) "\uD83C\uDFAF ${block.title}" else "\u2615 ${block.title}"
                entries.add(WaitTimerOverlay.TimerEntry(block.id, phaseLabel, phase.phaseRemainingMs))
            }
        }

        if (foregroundPkg != null) {
            for (block in blocks) {
                if (block.blockingStyle != UnlockMethodUtils.STYLE_USAGE_LIMIT) continue
                if (!block.showTimer) continue
                val cal = Calendar.getInstance()
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                if (block.activeDays.getOrNull(dayIndex) != '1') continue
                val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                if (foregroundPkg in packages) {
                    val remaining = computeUsageLimitRemainingMs(block)
                    if (remaining > 0L) {
                        entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
                    }
                }
            }

            for (block in blocks) {
                if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
                if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE) continue
                if (!block.showTimer) continue
                if (!isPackageTrackedByBlock(block, foregroundPkg)) continue
                if (!isScheduleActive(block)) continue
                val remaining = Prefs.getSchedAllowanceRemaining(block.id)
                if (remaining > 0L) {
                    entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
                }
            }
        }

        if (entries.isNotEmpty()) {
            overlayHideCounter = 0
            waitTimerOverlay?.update(entries)
        } else {
            overlayHideCounter++
            if (overlayHideCounter >= OVERLAY_HIDE_DEBOUNCE) {
                waitTimerOverlay?.hide()
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
        overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
        waitTimerOverlay?.hide()
        appTimerOverlay?.hide()
        accessibilityBlockOverlay?.hide()
        DiagnosticNotifier.cancelPollState(applicationContext)
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
        val nm = getSystemService(NotificationManager::class.java)
        pomodoroNotifIds.values.forEach { nm.cancel(it) }
        pomodoroNotifIds.clear()
        accessibilityObserver?.let { contentResolver.unregisterContentObserver(it) }
        accessibilityObserver = null
        waitTimerOverlay?.destroy()
        waitTimerOverlay = null
        appTimerOverlay?.destroy()
        appTimerOverlay = null
        accessibilityBlockOverlay?.destroy()
        accessibilityBlockOverlay = null
        packageInstallReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
            packageInstallReceiver = null
        }
        audioBlockManager?.destroy()
        audioBlockManager = null
        iconCache.clear()
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
