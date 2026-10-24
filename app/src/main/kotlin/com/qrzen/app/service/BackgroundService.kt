package com.qrzen.app.service

import android.app.AppOpsManager
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
import android.os.Process
import android.os.PowerManager
import android.os.SystemClock
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
import kotlinx.coroutines.Job
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
    @Volatile private var usagePollingActive = false
    @Volatile private var usageCheckJob: Job? = null
    private var usageStatsForegroundPackage: String? = null
    private var usageStatsForegroundClassName: String? = null
    private var usageStatsEventCursorMs = 0L
    private val foregroundTracker = ForegroundTracker(FOREGROUND_UNKNOWN_GRACE_MS)
    private var lastBlockedPkg: String? = null
    private var lastBlockedTime = 0L
    private var wakeLock: PowerManager.WakeLock? = null
    private var waitTimerOverlay: WaitTimerOverlay? = null
    private var appTimerOverlay: AppTimerOverlay? = null
    private var accessibilityObserver: android.database.ContentObserver? = null
    @Volatile private var isAccessibilityEnabled = false
    private var accessibilityBlockOverlay: AccessibilityBlockOverlay? = null
    private var fallbackBlockOverlay: FallbackBlockOverlay? = null
    @Volatile private var protectionSnapshot = ProtectionSnapshot(
        accessibilityConfigured = false,
        accessibilityConnected = false,
        usageAccess = false,
        overlayAccess = false,
        userUnlocked = false,
        mode = ProtectionMode.UNPROTECTED
    )
    private var lastRecordedProtectionMode: ProtectionMode? = null
    private var packageInstallReceiver: PackageInstallReceiver? = null
    private var audioBlockManager: AudioBlockManager? = null
    private var overlayHideCounter = 0
    private var timerOverlayStateSignature: String? = null
    private val pomodoroNotifIds = mutableMapOf<Int, Int>()
    private var nextPomodoroNotifId = 3000
    private val iconCache = mutableMapOf<String, android.graphics.drawable.Drawable>()
    private val recoveryRunnables = mutableListOf<Runnable>()

    private val systemExemptPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.intentresolver",
        "com.android.documentsui",
        "com.android.pixeldisplayservice",
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
        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val defaultLauncherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
            ?.let(::setOf)
            ?: emptySet()
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

    private val systemPhotoPickerPackages: Set<String> by lazy {
        val pickImagesIntent = Intent("android.provider.action.PICK_IMAGES").apply {
            type = "image/*"
        }
        packageManager.queryIntentActivities(pickImagesIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { appInfo ->
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    packageManager.getLaunchIntentForPackage(appInfo.packageName) == null
            }
            .map { it.packageName }
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

    private val usageCheckRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!usagePollingActive || usageCheckJob?.isActive == true) return
            usageCheckJob = scope.launch {
                try {
                    checkForegroundApp()
                } finally {
                    val completedJob = coroutineContext[Job]
                    if (usageCheckJob === completedJob) {
                        usageCheckJob = null
                        if (usagePollingActive) {
                            usageHandler.postDelayed(usageCheckRunnable, currentPollIntervalMs())
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val NOTIF_CHANNEL_ID = "qrzen_bg"
        private const val NOTIF_ID = 1001
        private const val CHECK_INTERVAL_MS = 60_000L
        private const val USAGE_POLL_INTERVAL_MS = 2_000L
        private const val FALLBACK_POLL_INTERVAL_MS = 750L
        private const val BLOCK_COOLDOWN_MS = 3_000L
        private const val OVERLAY_HIDE_DEBOUNCE = 2
        private const val USAGE_EVENTS_INITIAL_LOOKBACK_MS = 24 * 60 * 60_000L
        private const val USAGE_EVENTS_OVERLAP_MS = 5_000L
        private const val FOREGROUND_UNKNOWN_GRACE_MS = 10_000L
        private val RECOVERY_CHECK_DELAYS_MS = longArrayOf(0L, 2_000L, 5_000L, 15_000L, 30_000L, 60_000L)

        const val ACTION_ACCESSIBILITY_CONNECTED = "com.qrzen.app.ACCESSIBILITY_CONNECTED"
        const val ACTION_ACCESSIBILITY_DISCONNECTED = "com.qrzen.app.ACCESSIBILITY_DISCONNECTED"
        const val ACTION_BOOT_RECOVERY = "com.qrzen.app.BOOT_RECOVERY"

        fun start(context: Context, action: String? = null) {
            val intent = Intent(context, BackgroundService::class.java).apply {
                this.action = action
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }.onFailure { error ->
                runCatching {
                    ProtectionDiagnostics.record(
                        "BackgroundService start failed action=${action ?: "normal"}: " +
                            "${error.javaClass.simpleName}: ${error.message.orEmpty()}"
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ProtectionDiagnostics.record("BackgroundService start action=${intent?.action ?: "normal"}")
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
        if (fallbackBlockOverlay == null) {
            fallbackBlockOverlay = FallbackBlockOverlay(
                context = this,
                onUnlock = ::openFallbackUnlock,
                onGoHome = ::sendToHome
            )
        }
        if (audioBlockManager == null) {
            audioBlockManager = AudioBlockManager(this)
        }
        if (accessibilityObserver == null) {
            refreshProtectionStatus()
            val observer = object : android.database.ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    refreshProtectionStatus()
                    scope.launch { checkForegroundApp() }
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
        refreshProtectionStatus()
        if (intent?.action in setOf(
                ACTION_ACCESSIBILITY_CONNECTED,
                ACTION_ACCESSIBILITY_DISCONNECTED,
                ACTION_BOOT_RECOVERY
            )
        ) {
            resetForegroundSources()
            scheduleRecoveryChecks()
        }
        return START_STICKY
    }

    private fun checkAccessibilityEnabled(): Boolean {
        return ProtectionStatus.isAccessibilityConfigured(this)
    }

    private fun refreshProtectionStatus() {
        protectionSnapshot = ProtectionStatus.snapshot(this)
        isAccessibilityEnabled = protectionSnapshot.accessibilityConfigured
        if (lastRecordedProtectionMode != protectionSnapshot.mode) {
            ProtectionDiagnostics.record(
                "Protection ${lastRecordedProtectionMode ?: "START"} -> ${protectionSnapshot.mode}; " +
                    "configured=${protectionSnapshot.accessibilityConfigured}, " +
                    "connected=${protectionSnapshot.accessibilityConnected}, " +
                    "usage=${protectionSnapshot.usageAccess}, overlay=${protectionSnapshot.overlayAccess}, " +
                    "unlocked=${protectionSnapshot.userUnlocked}"
            )
            lastRecordedProtectionMode = protectionSnapshot.mode
        }
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification())
    }

    private fun currentPollIntervalMs(): Long =
        if (protectionSnapshot.mode == ProtectionMode.FALLBACK) {
            FALLBACK_POLL_INTERVAL_MS
        } else {
            USAGE_POLL_INTERVAL_MS
        }

    private fun resetForegroundSources() {
        foregroundTracker.clear()
        usageStatsForegroundPackage = null
        usageStatsForegroundClassName = null
        usageStatsEventCursorMs = 0L
    }

    private fun scheduleRecoveryChecks() {
        recoveryRunnables.forEach(handler::removeCallbacks)
        recoveryRunnables.clear()
        RECOVERY_CHECK_DELAYS_MS.forEach { delay ->
            val runnable = Runnable {
                refreshProtectionStatus()
                scope.launch {
                    checkExpiredPauses()
                    checkForegroundApp()
                }
            }
            recoveryRunnables.add(runnable)
            handler.postDelayed(runnable, delay)
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
        val hasUsageLimitBlocks = allBlocks.any {
            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
                it.blockingStyle == UnlockMethodUtils.STYLE_USAGE_LIMIT
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
            hasActiveBlocks || hasWaitTimerBlocks || hasUsageLimitBlocks ||
                hasPomodoroBlocks || hasScheduleBreakBlocks
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
        val trackingPeriodStart = usageLimitPeriodStart(block, now)
        val startTime = if (block.usageLimitPeriod == "HOURLY") {
            now - 3_600_000L
        } else {
            trackingPeriodStart
        }
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        // Include history before the accounting boundary so a foreground session
        // that began before midnight/hour start can be reconstructed and clipped.
        val queryStart = (startTime - USAGE_EVENTS_INITIAL_LOOKBACK_MS).coerceAtLeast(0L)
        val events = runCatching { usageStatsManager.queryEvents(queryStart, now) }
            .getOrElse { return block.usageLimitMinutes * 60_000L }
        val event = UsageEvents.Event()
        val foregroundStartTimes = mutableMapOf<String, Long>()
        var totalUsageMs = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg !in packages) continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundStartTimes.putIfAbsent(pkg, event.timeStamp)
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = foregroundStartTimes.remove(pkg)
                    if (start != null && event.timeStamp > startTime) {
                        totalUsageMs += (event.timeStamp - maxOf(start, startTime)).coerceAtLeast(0L)
                    }
                }
            }
        }
        for ((_, start) in foregroundStartTimes) {
            totalUsageMs += (now - maxOf(start, startTime)).coerceAtLeast(0L)
        }
        if (Prefs.getUsageLimitPeriodStart(block.id) == trackingPeriodStart) {
            // UsageEvents can be buffered on Pixel devices until the foreground app
            // pauses. The live counter keeps the visible timer moving in the meantime.
            totalUsageMs = maxOf(totalUsageMs, Prefs.getUsageLimitTrackedMs(block.id))
        }
        val limitMs = block.usageLimitMinutes * 60_000L
        return (limitMs - totalUsageMs).coerceAtLeast(0L)
    }

    private fun usageLimitPeriodStart(block: AppBlock, now: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            if (block.usageLimitPeriod == "HOURLY") {
                set(Calendar.MINUTE, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun trackUsageLimitState(blocks: List<AppBlock>, foregroundPkg: String?) {
        val wallNow = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()
        for (block in blocks) {
            val tracksUsage = block.blockingStyle == UnlockMethodUtils.STYLE_USAGE_LIMIT ||
                (block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
                    block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } ==
                    UnlockMethodUtils.BREAK_USAGE_LIMIT)
            if (!tracksUsage) continue

            val periodStart = usageLimitPeriodStart(block, wallNow)
            if (Prefs.getUsageLimitPeriodStart(block.id) != periodStart) {
                Prefs.setUsageLimitPeriodStart(block.id, periodStart)
                Prefs.setUsageLimitTrackedMs(block.id, 0L)
                Prefs.setUsageLimitLastTick(block.id, 0L)
                Prefs.setUsageLimitWasInApp(block.id, false)
            }

            val packages = block.appPackages.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
            val isInApp = foregroundPkg in packages
            val lastTick = Prefs.getUsageLimitLastTick(block.id)
            val delta = TimerEngine.elapsedSince(lastTick, elapsedNow)
            if (Prefs.getUsageLimitWasInApp(block.id) && delta > 0L) {
                Prefs.setUsageLimitTrackedMs(
                    block.id,
                    Prefs.getUsageLimitTrackedMs(block.id) + delta
                )
            }
            Prefs.setUsageLimitWasInApp(block.id, isInApp)
            Prefs.setUsageLimitLastTick(block.id, elapsedNow)
        }
    }

    private fun isWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (block.activeDays.getOrNull(dayIndex) != '1') return false

        val kv = MMKV.defaultMMKV()
        val wallNow = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()
        val blockingUntilKey = "wait_timer_blocking_${block.id}"
        val remainingKey = "wait_timer_remaining_${block.id}"
        val lastUpdateKey = "wait_timer_last_update_${block.id}"
        val inAppKey = "wait_timer_in_app_${block.id}"
        val maxRemainingMs = block.waitTimerUseMinutes * 60_000L
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isInApp = foregroundPkg in packages

        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)
        if (blockingUntil > wallNow) return true

        if (blockingUntil > 0L) {
            kv.encode(remainingKey, maxRemainingMs)
            kv.encode(blockingUntilKey, 0L)
            kv.encode(lastUpdateKey, elapsedNow)
            kv.encode(inAppKey, false)
            return false
        }

        if (foregroundPkg == null) return false

        var remaining = kv.decodeLong(remainingKey, -1L)
        if (remaining < 0L) {
            val initialState = TimerEngine.initializeRemaining(
                maximumMs = maxRemainingMs,
                nowElapsed = elapsedNow,
                isTrackedNow = isInApp
            )
            remaining = initialState.remainingMs
            kv.encode(remainingKey, initialState.remainingMs)
            kv.encode(lastUpdateKey, initialState.lastTickElapsed)
            // The first confirmed foreground sample owns the start of the session.
            // Persisting false here delayed the overlay until a later poll and made
            // short foreground-signal gaps look like a failed timer launch.
            kv.encode(inAppKey, initialState.wasTracked)
            return false
        }

        val lastUpdate = kv.decodeLong(lastUpdateKey, 0L)
        val elapsed = TimerEngine.elapsedSince(lastUpdate, elapsedNow)
        val wasInApp = kv.decodeBool(inAppKey, false)

        remaining = TimerEngine.updateRemaining(
            remainingMs = remaining,
            maximumMs = maxRemainingMs,
            elapsedMs = elapsed,
            wasTracked = wasInApp,
            adaptive = block.waitTimerAdaptive,
            useMinutes = block.waitTimerUseMinutes,
            waitMinutes = block.waitTimerWaitMinutes
        )
        kv.encode(inAppKey, isInApp)

        kv.encode(remainingKey, remaining)
        kv.encode(lastUpdateKey, elapsedNow)

        if (remaining <= 0L) {
            val waitUntil = wallNow + block.waitTimerWaitMinutes * 60_000L
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
        val wallNow = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages

        if (!scheduleActive) {
            // Every schedule window owns a fresh wait-timer cycle.
            Prefs.clearScheduleWtState(blockId)
            return
        }

        val blockingUntil = Prefs.getScheduleWtBlockingUntil(blockId)
        if (blockingUntil > wallNow) {
            Prefs.setScheduleWtLastTick(blockId, 0L)
            Prefs.setScheduleWtWasInApp(blockId, false)
            return
        }

        if (blockingUntil > 0L && blockingUntil <= wallNow) {
            Prefs.clearScheduleWtState(blockId)
            return
        }

        val budgetMs = block.waitTimerUseMinutes * 60_000L
        val lastTick = Prefs.getScheduleWtLastTick(blockId)
        val delta = TimerEngine.elapsedSince(lastTick, elapsedNow)
        val usedMs = TimerEngine.updateUsed(
            usedMs = Prefs.getScheduleWtUsedMs(blockId),
            maximumMs = budgetMs,
            elapsedMs = delta,
            wasTracked = Prefs.getScheduleWtWasInApp(blockId),
            adaptive = block.waitTimerAdaptive,
            useMinutes = block.waitTimerUseMinutes,
            waitMinutes = block.waitTimerWaitMinutes
        )

        if (usedMs >= budgetMs) {
            Prefs.setScheduleWtBlockingUntil(blockId, wallNow + block.waitTimerWaitMinutes * 60_000L)
            Prefs.setScheduleWtUsedMs(blockId, 0L)
            Prefs.setScheduleWtLastTick(blockId, 0L)
            Prefs.setScheduleWtWasInApp(blockId, false)
        } else {
            Prefs.setScheduleWtUsedMs(blockId, usedMs)
            Prefs.setScheduleWtWasInApp(blockId, isUsingBlockedApp)
            Prefs.setScheduleWtLastTick(blockId, elapsedNow)
        }
    }

    private fun trackScheduledAllowanceState(
        block: AppBlock,
        foregroundPkg: String?,
        scheduleActive: Boolean,
        timeBlocks: List<TimeBlock>
    ) {
        val blockId = block.id
        val wallNow = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()

        if (!scheduleActive) {
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
            Prefs.setSchedAllowanceWasInApp(blockId, false)
            return
        }

        val currentWindowStart = if (timeBlocks.isEmpty()) {
            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block, wallNow)
        } else {
            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks, wallNow)
        } ?: return
        val savedWindowStart = Prefs.getSchedAllowanceWindowStart(blockId)
        val maxAllowanceMs = block.scheduledAllowanceMinutes * 60_000L

        if (savedWindowStart != currentWindowStart || Prefs.getSchedAllowanceRemaining(blockId) < 0L) {
            Prefs.setSchedAllowanceRemaining(blockId, maxAllowanceMs)
            Prefs.setSchedAllowanceWindowStart(blockId, currentWindowStart)
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
            Prefs.setSchedAllowanceWasInApp(blockId, false)
        }

        val remaining = Prefs.getSchedAllowanceRemaining(blockId)
        if (remaining <= 0L) {
            Prefs.setSchedAllowanceLastTick(blockId, 0L)
            Prefs.setSchedAllowanceWasInApp(blockId, false)
            return
        }

        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages
        val delta = TimerEngine.elapsedSince(Prefs.getSchedAllowanceLastTick(blockId), elapsedNow)
        val newRemaining = TimerEngine.updateRemaining(
            remainingMs = remaining,
            maximumMs = maxAllowanceMs,
            elapsedMs = delta,
            wasTracked = Prefs.getSchedAllowanceWasInApp(blockId)
        )
        Prefs.setSchedAllowanceRemaining(blockId, newRemaining)
        Prefs.setSchedAllowanceWasInApp(blockId, isUsingBlockedApp && newRemaining > 0L)
        Prefs.setSchedAllowanceLastTick(blockId, if (newRemaining > 0L) elapsedNow else 0L)
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
                UnlockMethodUtils.BREAK_WAIT_TIMER -> {
                    Prefs.setScheduleWtLastTick(block.id, 0L)
                    Prefs.setScheduleWtWasInApp(block.id, false)
                }
                UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> {
                    Prefs.setSchedAllowanceLastTick(block.id, 0L)
                    Prefs.setSchedAllowanceWasInApp(block.id, false)
                }
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

        val (title, text) = when (protectionSnapshot.mode) {
            ProtectionMode.PRIMARY -> "QR Zen protection active" to "Accessibility protection connected"
            ProtectionMode.FALLBACK -> "QR Zen fallback protection active" to "Using Usage Access while Accessibility is unavailable"
            ProtectionMode.LIMITED -> "QR Zen protection limited" to "Enable Display over Apps for reliable fallback blocking"
            ProtectionMode.UNPROTECTED -> "QR Zen protection needs attention" to if (
                protectionSnapshot.accessibilityConfigured && !protectionSnapshot.accessibilityConnected
            ) {
                "Accessibility is enabled but not connected"
            } else {
                "Enable Accessibility or Usage Access fallback"
            }
        }
        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
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
            pkg in defaultLauncherPackages ||
            pkg in imePackages ||
            pkg in dialerPackages ||
            pkg in shareHandlerPackages ||
            pkg in systemPhotoPickerPackages ||
            isSystemNonLauncherApp(pkg)
    }

    private fun isRecentsForeground(pkg: String): Boolean {
        val className = if (
            protectionSnapshot.accessibilityConnected &&
            BlockAccessibilityService.currentForegroundPackage == pkg
        ) {
            return BlockAccessibilityService.currentForegroundIsRecents
        } else if (usageStatsForegroundPackage == pkg) {
            usageStatsForegroundClassName
        } else {
            null
        }
        return BlockAccessibilityService.isRecentsActivityClass(className)
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
        if (accessibilityPkg != null && protectionSnapshot.accessibilityConnected) {
            return accessibilityPkg
        }

        // Usage Access can be revoked while this service is running. Some OEMs
        // throw SecurityException from queryEvents() rather than returning no data.
        if (!protectionSnapshot.usageAccess) {
            usageStatsForegroundPackage = null
            usageStatsForegroundClassName = null
            usageStatsEventCursorMs = 0L
            return null
        }

        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return null
        val endTime = System.currentTimeMillis()
        if (usageStatsEventCursorMs > endTime) {
            usageStatsEventCursorMs = 0L
        }
        val startTime = if (usageStatsEventCursorMs == 0L) {
            endTime - USAGE_EVENTS_INITIAL_LOOKBACK_MS
        } else {
            (usageStatsEventCursorMs - USAGE_EVENTS_OVERLAP_MS).coerceAtLeast(0L)
        }
        val events = runCatching { usageStatsManager.queryEvents(startTime, endTime) }
            .getOrElse { error ->
                usageStatsForegroundPackage = null
                usageStatsForegroundClassName = null
                usageStatsEventCursorMs = 0L
                ProtectionDiagnostics.record(
                    "Usage events unavailable: ${error.javaClass.simpleName}: ${error.message.orEmpty()}"
                )
                return null
            }
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val eventPackage = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    usageStatsForegroundPackage = eventPackage
                    usageStatsForegroundClassName = event.className
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (usageStatsForegroundPackage == eventPackage) {
                        usageStatsForegroundPackage = null
                        usageStatsForegroundClassName = null
                    }
                }
            }
        }
        usageStatsEventCursorMs = endTime
        return usageStatsForegroundPackage
    }

    private fun resolveForegroundPackage(): ForegroundTracker.Snapshot? {
        return foregroundTracker.observe(getForegroundPackage(), SystemClock.elapsedRealtime())
    }

    private fun hasUsageStatsAccess(): Boolean {
        return ProtectionStatus.hasUsageAccess(this)
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
        refreshProtectionStatus()
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
            fallbackBlockOverlay?.hide()
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
            fallbackBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }

        val foreground = resolveForegroundPackage()
        if (foreground == null) {
            // Keep the last rendered overlay and persisted active-session marker. A
            // transient unknown result must not erase usage or grant a fresh interval.
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        val pkg = foreground.packageName
        trackUsageLimitState(allCandidates, pkg)
        trackScheduleBreakState(allCandidates, pkg)
        val recentsForeground = isRecentsForeground(pkg)
        if (ForegroundSurfacePolicy.isExemptFromBlocking(isExemptPackage(pkg), recentsForeground)) {
            updateTimerOverlays(allCandidates, null)
            pauseUsageTimers(allCandidates)
            appTimerOverlay?.hide()
            accessibilityBlockOverlay?.hide()
            fallbackBlockOverlay?.hide()
            DiagnosticNotifier.cancelPollState(applicationContext)
            return
        }
        if (protectionSnapshot.mode != ProtectionMode.PRIMARY &&
            protectionSnapshot.mode != ProtectionMode.FALLBACK
        ) {
            val requirement = findAccessibilityRequirement(allCandidates, pkg)
            if (requirement != null) {
                pauseUsageTimers(allCandidates)
                pauseScheduleBreakUsageTracking(allCandidates)
                waitTimerOverlay?.hide()
                appTimerOverlay?.hide()
                accessibilityBlockOverlay?.show(protectionUnavailableMessage(requirement))
                sendToHome()
                return
            }
        }
        accessibilityBlockOverlay?.hide()
        if (!recentsForeground && pkg == lastBlockedPkg && now - lastBlockedTime < BLOCK_COOLDOWN_MS) {
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
                val blockReason = allowlistBlocks.joinToString(separator = ", ") { block ->
                    val configured = block.appPackages.split(",").map { it.trim() }.contains(pkg)
                    val timerExpired = Prefs.isAppTimerExpired(block.id, pkg)
                    "${block.id}:${block.title}[configured=$configured,timerExpired=$timerExpired]"
                }
                val diagnostic = "Foreground allowlist blocked pkg=$pkg; blocks=$blockReason"
                android.util.Log.w("QrZenBackground", diagnostic)
                ProtectionDiagnostics.record(diagnostic)
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
                source = if (protectionSnapshot.mode == ProtectionMode.PRIMARY) "Accessibility" else "UsageStats fallback",
                detectedPkg = pkg,
                appLabel = getAppLabel(pkg),
                cachedImePackages = imePackages,
                freshImePackages = freshImePackages,
                isSystemNonLauncher = isSystemNonLauncher,
                isExempt = ForegroundSurfacePolicy.isExemptFromBlocking(
                    isExemptPackage(pkg),
                    recentsForeground
                ),
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
            if (protectionSnapshot.mode == ProtectionMode.FALLBACK) {
                fallbackBlockOverlay?.show(
                    FallbackBlockOverlay.Target(
                        packageName = pkg,
                        blockIds = intArrayOf(blocklistBlock.id),
                        title = blocklistBlock.title,
                        isAllowlist = false
                    )
                )
            } else {
                fallbackBlockOverlay?.hide()
                launchLockScreen(pkg, blocklistBlock)
            }
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
            if (protectionSnapshot.mode == ProtectionMode.FALLBACK) {
                fallbackBlockOverlay?.show(
                    FallbackBlockOverlay.Target(
                        packageName = pkg,
                        blockIds = allowlistBlocks.map { it.id }.toIntArray(),
                        title = getString(R.string.fallback_allowlist_title),
                        isAllowlist = true
                    )
                )
            } else {
                fallbackBlockOverlay?.hide()
                launchAllowlistOverlay(pkg, allowlistBlocks)
            }
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

        if (allowedForegroundPkg != null) {
            fallbackBlockOverlay?.hide()
        }
        updateTimerOverlays(allCandidates, pkg)
        trackAllowlistUsageTimers(activeBlocks, allowedForegroundPkg ?: "")
        trackPerAppTimers(activeBlocks, allowedForegroundPkg ?: "")
        updateAppTimerOverlay(activeBlocks, allowedForegroundPkg)
    }

    private fun pauseUsageTimers(blocks: List<AppBlock>) {
        trackAllowlistUsageTimers(blocks, "")
        trackPerAppTimers(blocks, "")
        val elapsedNow = SystemClock.elapsedRealtime()
        val kv = MMKV.defaultMMKV()
        for (block in blocks) {
            if (block.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER) {
                val remainingKey = "wait_timer_remaining_${block.id}"
                if (kv.containsKey(remainingKey)) {
                    kv.encode("wait_timer_last_update_${block.id}", elapsedNow)
                    kv.encode("wait_timer_in_app_${block.id}", false)
                }
            }
            Prefs.setUsageLimitLastTick(block.id, 0L)
            Prefs.setUsageLimitWasInApp(block.id, false)
        }
    }

    private suspend fun findAccessibilityRequirement(
        blocks: List<AppBlock>,
        foregroundPackage: String
    ): AccessibilityRulePolicy.Requirement? {
        val calendar = Calendar.getInstance()
        val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        for (block in blocks) {
            val scheduleActive = block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
                isScheduleActive(block)
            AccessibilityRulePolicy.requirementFor(
                block = block,
                foregroundPackage = foregroundPackage,
                dayIndex = dayIndex,
                scheduleActive = scheduleActive
            )?.let { return it }
        }
        return null
    }

    private fun accessibilityMessage(requirement: AccessibilityRulePolicy.Requirement): String =
        when (requirement) {
            AccessibilityRulePolicy.Requirement.WAIT_TIMER ->
                getString(R.string.accessibility_block_wait_timer)
            AccessibilityRulePolicy.Requirement.USAGE_LIMIT ->
                getString(R.string.accessibility_block_usage_limit)
            AccessibilityRulePolicy.Requirement.SCHEDULED_ALLOWANCE ->
                getString(R.string.accessibility_block_scheduled_allowance)
        }

    private fun protectionUnavailableMessage(
        requirement: AccessibilityRulePolicy.Requirement
    ): String = when {
        protectionSnapshot.accessibilityConfigured && !protectionSnapshot.accessibilityConnected ->
            getString(R.string.accessibility_enabled_not_connected)
        !protectionSnapshot.usageAccess -> accessibilityMessage(requirement)
        !protectionSnapshot.overlayAccess -> getString(R.string.fallback_overlay_required)
        else -> accessibilityMessage(requirement)
    }

    private fun trackAllowlistUsageTimers(blocks: List<AppBlock>, foregroundPkg: String) {
        // Block-level timer is now pure wall-clock via activeUntil;
        // expiry handled by checkExpiredPauses().
    }

    private fun trackPerAppTimers(blocks: List<AppBlock>, foregroundPkg: String) {
        val elapsedNow = SystemClock.elapsedRealtime()
        for (block in blocks) {
            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
            for (pkg in block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                val remaining = Prefs.getAppTimerRemaining(block.id, pkg)
                if (remaining <= 0L) continue

                if (Prefs.getAppTimerOriginal(block.id, pkg) <= 0L) {
                    Prefs.setAppTimerOriginal(block.id, pkg, remaining)
                }

                val lastFg = Prefs.getAppTimerLastFg(block.id, pkg)
                val elapsed = TimerEngine.elapsedSince(lastFg, elapsedNow)
                val newRemaining = if (lastFg > 0L) {
                    (remaining - elapsed).coerceAtLeast(0L)
                } else {
                    remaining
                }
                Prefs.setAppTimerRemaining(block.id, pkg, newRemaining)
                Prefs.setAppTimerLastFg(
                    block.id,
                    pkg,
                    if (pkg == foregroundPkg && newRemaining > 0L) elapsedNow else 0L
                )
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
        val candidates = mutableListOf<TimerOverlayResolver.Candidate>()

        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_WAIT_TIMER) continue
            if (!block.showTimer) continue
            val isCurrentApp = foregroundPkg != null && isPackageTrackedByBlock(block, foregroundPkg)
            val remaining = kv.decodeLong("wait_timer_remaining_${block.id}", -1L)
            candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, remaining, isCurrentApp))
        }

        for (block in blocks) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
            if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_WAIT_TIMER) continue
            if (!block.showTimer) continue
            val blockingUntil = Prefs.getScheduleWtBlockingUntil(block.id)
            if (blockingUntil > now) {
                candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, blockingUntil - now, true))
            } else if (
                foregroundPkg != null &&
                isPackageTrackedByBlock(block, foregroundPkg) &&
                isScheduleActive(block)
            ) {
                val budgetMs = block.waitTimerUseMinutes * 60_000L
                val remaining = (budgetMs - Prefs.getScheduleWtUsedMs(block.id)).coerceAtLeast(0L)
                candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, remaining, true))
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
                candidates.add(TimerOverlayResolver.Candidate(block.id, phaseLabel, phase.phaseRemainingMs, true))
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
                    candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, remaining, true))
                }
            }

            for (block in blocks) {
                if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
                if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_USAGE_LIMIT) continue
                if (!block.showTimer) continue
                if (!isPackageTrackedByBlock(block, foregroundPkg)) continue
                if (!isScheduleActive(block)) continue
                val remaining = computeUsageLimitRemainingMs(block)
                candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, remaining, true))
            }

            for (block in blocks) {
                if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
                if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE) continue
                if (!block.showTimer) continue
                if (!isPackageTrackedByBlock(block, foregroundPkg)) continue
                if (!isScheduleActive(block)) continue
                val remaining = Prefs.getSchedAllowanceRemaining(block.id)
                candidates.add(TimerOverlayResolver.Candidate(block.id, block.title, remaining, true))
            }
        }

        val entries = TimerOverlayResolver.resolve(candidates).map {
            WaitTimerOverlay.TimerEntry(it.blockId, it.label, it.remainingMs)
        }
        val stateSignature = "${foregroundPkg.orEmpty()}|${entries.map { it.blockId }}"
        if (timerOverlayStateSignature != stateSignature) {
            timerOverlayStateSignature = stateSignature
            ProtectionDiagnostics.record(
                "Timer overlay foreground=${foregroundPkg ?: "none"}, " +
                    "visible=${entries.map { it.blockId }}, " +
                    "candidates=${candidates.map { "${it.blockId}:${it.shouldBeVisible}:${it.remainingMs}" }}"
            )
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
        usageHandler.removeCallbacks(usageCheckRunnable)
        usageHandler.post(usageCheckRunnable)
    }

    private fun openFallbackUnlock(target: FallbackBlockOverlay.Target) {
        if (target.isAllowlist) {
            startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_IDS, target.blockIds)
                putExtra(AllowlistOverlayActivity.EXTRA_BLOCKED_PKG, target.packageName)
            })
        } else {
            val blockId = target.blockIds.firstOrNull() ?: return
            startActivity(Intent(this, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(LockScreenActivity.EXTRA_BLOCK_ID, blockId)
                putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, target.packageName)
            })
        }
    }

    private fun stopUsagePolling() {
        usagePollingActive = false
        usageHandler.removeCallbacks(usageCheckRunnable)
        usageCheckJob?.cancel()
        usageCheckJob = null
        foregroundTracker.clear()
        usageStatsForegroundPackage = null
        usageStatsForegroundClassName = null
        usageStatsEventCursorMs = 0L
        timerOverlayStateSignature = null
        lastBlockedPkg = null
        overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
        waitTimerOverlay?.hide()
        appTimerOverlay?.hide()
        accessibilityBlockOverlay?.hide()
        fallbackBlockOverlay?.hide()
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
        fallbackBlockOverlay?.destroy()
        fallbackBlockOverlay = null
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
        recoveryRunnables.forEach(handler::removeCallbacks)
        recoveryRunnables.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
