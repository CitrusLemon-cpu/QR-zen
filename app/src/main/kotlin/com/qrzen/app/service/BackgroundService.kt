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
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.receiver.AlarmKeepaliveReceiver
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
import com.qrzen.app.ui.lock.LockScreenActivity
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
        val currentlyActiveIds = allBlocks
            .filter { it.isEnabled && !it.isArchived && it.pausedUntil <= now && isBlockActive(it) }
            .map { it.id }
            .toSet()
        val newlyActive = currentlyActiveIds - previouslyActiveBlockIds
        previouslyActiveBlockIds.clear()
        previouslyActiveBlockIds.addAll(currentlyActiveIds)
        if (newlyActive.isNotEmpty()) {
            sendToHome()
            shouldRefresh = true
        }
        if (shouldRefresh) WidgetRefresh.refresh(applicationContext)

        val hasActiveBlocks = currentlyActiveIds.isNotEmpty()
        val needsPolling = hasActiveBlocks
        if (needsPolling) {
            startUsagePolling()
        } else {
            stopUsagePolling()
        }
    }

    private fun isBlockActive(block: AppBlock): Boolean {
        if (block.blockNowUntil > System.currentTimeMillis()) return true
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

        val activeBlocks = dao.getAll().filter {
            it.isEnabled && !it.isArchived && now > it.pausedUntil && isBlockActive(it)
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

        val allowlistBlock = activeBlocks
            .filter { it.isAllowlistMode }
            .firstOrNull { block ->
                val allowed = block.appPackages.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                !allowed.contains(pkg) || Prefs.isAppTimerExpired(pkg)
            }
        if (allowlistBlock != null) {
            lastBlockedPkg = pkg
            lastBlockedTime = now
            launchAllowlistOverlay(pkg, allowlistBlock)
        }
    }

    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
    }

    private fun launchAllowlistOverlay(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_ID, block.id)
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
        stopUsagePolling()
        wakeLock?.let {
            if (it.isHeld) it.release()
            wakeLock = null
        }
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
