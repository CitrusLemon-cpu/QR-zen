package com.qrzen.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Persistent foreground service — the heartbeat of QR Zen.
 *
 * Uses START_STICKY so the system restarts it if killed.
 * Combined with RebootReceiver + StartupContentProvider, this forms
 * the three-layer ultra-battery-saver survival stack.
 */
@AndroidEntryPoint
class BackgroundService : Service() {

    @Inject lateinit var dao: AppBlockDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

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
        dao.getAll()
            .filter { block ->
                block.pausedUntil != 0L &&
                block.pausedUntil != Long.MAX_VALUE &&
                now > block.pausedUntil
            }
            .forEach { block ->
                dao.setPausedUntil(block.id, 0L)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
