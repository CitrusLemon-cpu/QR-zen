package com.qrzen.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
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
    private val previouslyActiveBlockIds = mutableSetOf<Int>()

    private val checkRunnable = object : Runnable {
        override fun run() {
            scope.launch { checkExpiredPauses() }
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    companion object {
        private const val NOTIF_CHANNEL_ID = "qrzen_bg"
        private const val NOTIF_ID = 1001
        private const val CHECK_INTERVAL_MS = 60_000L

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
        val currentlyActiveIds = allBlocks
            .filter { it.isEnabled && !it.isArchived && it.pausedUntil <= now && isBlockActive(it) }
            .map { it.id }
            .toSet()
        val newlyActive = currentlyActiveIds - previouslyActiveBlockIds
        previouslyActiveBlockIds.clear()
        previouslyActiveBlockIds.addAll(currentlyActiveIds)
        if (newlyActive.isNotEmpty()) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            applicationContext.startActivity(homeIntent)
            shouldRefresh = true
        }
        if (shouldRefresh) WidgetRefresh.refresh(applicationContext)
    }

    private fun isBlockActive(block: AppBlock): Boolean {
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
