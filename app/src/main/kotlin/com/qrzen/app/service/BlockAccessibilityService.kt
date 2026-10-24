package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar

class BlockAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QrZenAccessibility"
        private const val RECENTS_CONFIRM_DELAY_MS = 300L
        @Volatile var isRunning: Boolean = false
        @Volatile var currentForegroundPackage: String? = null
        @Volatile var currentForegroundClassName: String? = null
        @Volatile var currentForegroundIsRecents: Boolean = false

        fun isRecentsActivityClass(className: String?): Boolean {
            if (className.isNullOrBlank()) return false
            return className.endsWith(".RecentsActivity", ignoreCase = true) ||
                (className.contains("quickstep", ignoreCase = true) &&
                    className.contains("recents", ignoreCase = true))
        }

        private val SYSTEM_EXEMPT_PACKAGES = setOf(
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
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error in accessibility service", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingRecentsCheck: Runnable? = null
    private var foregroundEvaluationJob: Job? = null
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
        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val defaultLauncherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
            ?.let(::setOf)
            ?: emptySet()
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

    private fun isExemptFromAllowlist(pkg: String, confirmedRecents: Boolean): Boolean {
        val normallyExempt = pkg in SYSTEM_EXEMPT_PACKAGES ||
            pkg in defaultLauncherPackages ||
            pkg in dialerPackages ||
            pkg in imePackages ||
            pkg in shareHandlerPackages ||
            pkg in systemPhotoPickerPackages ||
            isSystemNonLauncherApp(pkg)
        return ForegroundSurfacePolicy.isExemptFromBlocking(normallyExempt, confirmedRecents)
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
        AccessibilityHealthMonitor.markConnected()
        ProtectionDiagnostics.record("Accessibility onServiceConnected")
        BackgroundService.start(this, BackgroundService.ACTION_ACCESSIBILITY_CONNECTED)
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
                    AccessibilityHealthMonitor.markDisconnected()
                    ProtectionDiagnostics.record("Accessibility setting disabled")
                    BackgroundService.start(
                        this@BlockAccessibilityService,
                        BackgroundService.ACTION_ACCESSIBILITY_DISCONNECTED
                    )
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
        if (event == null ||
            (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED)
        ) return
        val pkg = event.packageName?.toString() ?: return
        val className = event.className?.toString()
        AccessibilityHealthMonitor.markEvent()

        val confirmedRecentsPkg = currentForegroundPackage
            ?.takeIf { currentForegroundIsRecents }
        val activeRoot = rootInActiveWindow
        val activeRootPkg = activeRoot?.packageName?.toString()
        val activeRootClassName = activeRoot?.className?.toString()
        if (confirmedRecentsPkg != null &&
            pkg != confirmedRecentsPkg &&
            activeRootPkg == confirmedRecentsPkg
        ) {
            Log.d(TAG, "Ignoring Recents thumbnail event pkg=$pkg class=${className.orEmpty()}")
            return
        }

        val isNonDefaultLauncher =
            pkg in launcherPackages && pkg !in defaultLauncherPackages
        if (isNonDefaultLauncher &&
            currentForegroundIsRecents &&
            currentForegroundPackage == pkg
        ) {
            return
        }
        if (isNonDefaultLauncher) {
            Log.d(TAG, "Non-default launcher event pkg=$pkg class=${className.orEmpty()} type=${event.eventType}")
        }
        val eventIsRecents = isRecentsActivityClass(className)
        val activeRootIsRecents = isRecentsActivityClass(activeRootClassName)
        if (eventIsRecents || activeRootIsRecents) {
            pendingRecentsCheck?.let(mainHandler::removeCallbacks)
            pendingRecentsCheck = null
            val recentsPkg = if (activeRootIsRecents) activeRootPkg ?: pkg else pkg
            val recentsClassName = if (activeRootIsRecents) activeRootClassName else className
            val diagnostic =
                "Recents confirmed immediately pkg=$recentsPkg " +
                    "class=${recentsClassName.orEmpty()} eventPkg=$pkg"
            Log.d(TAG, diagnostic)
            ProtectionDiagnostics.record(diagnostic)
            processForegroundWindow(recentsPkg, recentsClassName, confirmedRecents = true)
            return
        }
        if (isNonDefaultLauncher) {
            scheduleConfirmedRecentsCheck(pkg, className)
            return
        }

        processForegroundWindow(pkg, className, confirmedRecents = false)
    }

    private fun scheduleConfirmedRecentsCheck(pkg: String, className: String?) {
        pendingRecentsCheck?.let(mainHandler::removeCallbacks)
        val check = Runnable {
            pendingRecentsCheck = null
            val rootPkg = rootInActiveWindow?.packageName?.toString()
            val activeWindowPkg = windows
                .firstOrNull { it.isActive }
                ?.root
                ?.packageName
                ?.toString()
            val usageForeground = isLatestUsageForeground(pkg)
            val confirmed = rootPkg == pkg || activeWindowPkg == pkg || usageForeground
            val diagnostic =
                "Recents confirmation pkg=$pkg confirmed=$confirmed " +
                    "root=${rootPkg ?: "none"} activeWindow=${activeWindowPkg ?: "none"} " +
                    "usageForeground=$usageForeground"
            Log.d(TAG, diagnostic)
            ProtectionDiagnostics.record(diagnostic)
            if (confirmed) {
                processForegroundWindow(pkg, className, confirmedRecents = true)
            }
        }
        pendingRecentsCheck = check
        mainHandler.postDelayed(check, RECENTS_CONFIRM_DELAY_MS)
    }

    private fun isLatestUsageForeground(pkg: String): Boolean {
        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return false
        val endTime = System.currentTimeMillis()
        val events = runCatching {
            usageStatsManager.queryEvents(endTime - 2_000L, endTime)
        }.getOrNull() ?: return false
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val eventPackage = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundPackage = eventPackage
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (foregroundPackage == eventPackage) foregroundPackage = null
                }
            }
        }
        return foregroundPackage == pkg
    }

    private fun processForegroundWindow(
        pkg: String,
        className: String?,
        confirmedRecents: Boolean
    ) {

        // Only filter genuinely transient windows (system UI, keyboards).
        // Launchers and dialers represent real navigation away from an app.
        val isUnconfirmedNonDefaultLauncher =
            pkg in launcherPackages && pkg !in defaultLauncherPackages && !confirmedRecents
        if (confirmedRecents) {
            // Pixel's Overview UI is hosted by the launcher. Preserve that surface
            // as the foreground target so Home can remain exempt while Recents is not.
            currentForegroundPackage = pkg
            currentForegroundClassName = className
            currentForegroundIsRecents = true
        } else if (pkg !in SYSTEM_EXEMPT_PACKAGES &&
            pkg !in imePackages &&
            pkg !in shareHandlerPackages &&
            !isUnconfirmedNonDefaultLauncher &&
            !isSystemNonLauncherApp(pkg)
        ) {
            currentForegroundPackage = pkg
            currentForegroundClassName = className
            currentForegroundIsRecents = confirmedRecents
        }

        // Do not leave the previous app cached while a QR Zen screen is visible.
        // A stale value makes every usage-based allowance continue ticking.
        if (pkg == packageName) return

        val isTransientExemptWindow = pkg in SYSTEM_EXEMPT_PACKAGES ||
            pkg in imePackages ||
            pkg in shareHandlerPackages ||
            pkg in systemPhotoPickerPackages ||
            isSystemNonLauncherApp(pkg)
        if (!confirmedRecents && isTransientExemptWindow) return

        foregroundEvaluationJob?.cancel()
        foregroundEvaluationJob = scope.launch {
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

            if (isExemptFromAllowlist(pkg, confirmedRecents) || isDeviceLocked()) return@launch

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
                    if (currentForegroundPackage != pkg) return@launch
                    val blockReason = allowlistBlocks.joinToString(separator = ", ") { block ->
                        val configured = block.appPackages.split(",").map { it.trim() }.contains(pkg)
                        val timerExpired = Prefs.isAppTimerExpired(block.id, pkg)
                        "${block.id}:${block.title}[configured=$configured,timerExpired=$timerExpired]"
                    }
                    val diagnostic = "Accessibility allowlist blocked pkg=$pkg; blocks=$blockReason"
                    Log.w(TAG, diagnostic)
                    ProtectionDiagnostics.record(diagnostic)
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
        val trackingPeriodStart = Calendar.getInstance().apply {
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
        val startTime = when (block.usageLimitPeriod) {
            "HOURLY" -> now - 3_600_000L
            else -> trackingPeriodStart
        }
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val queryStart = (startTime - 24 * 60 * 60_000L).coerceAtLeast(0L)
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
                    if (start != null) {
                        if (event.timeStamp > startTime) {
                            totalUsageMs += (event.timeStamp - maxOf(start, startTime)).coerceAtLeast(0L)
                        }
                    }
                }
            }
        }
        for ((_, start) in foregroundStartTimes) {
            totalUsageMs += (now - maxOf(start, startTime)).coerceAtLeast(0L)
        }
        if (Prefs.getUsageLimitPeriodStart(block.id) == trackingPeriodStart) {
            totalUsageMs = maxOf(totalUsageMs, Prefs.getUsageLimitTrackedMs(block.id))
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
        AccessibilityHealthMonitor.markDisconnected()
        ProtectionDiagnostics.record("Accessibility onDestroy/disconnected")
        currentForegroundPackage = null
        currentForegroundClassName = null
        currentForegroundIsRecents = false
        pendingRecentsCheck?.let(mainHandler::removeCallbacks)
        pendingRecentsCheck = null
        foregroundEvaluationJob?.cancel()
        foregroundEvaluationJob = null
        accessibilitySettingsObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        accessibilitySettingsObserver = null
        BackgroundService.start(this, BackgroundService.ACTION_ACCESSIBILITY_DISCONNECTED)
        scope.cancel()
        super.onDestroy()
    }
}
