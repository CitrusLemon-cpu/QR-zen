package com.qrzen.app.ui.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.ActivityPomodoroBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        private const val CHANNEL_ID = "qrzen_pomodoro"
        private const val NOTIF_ID = 2001
    }

    @Inject lateinit var dao: AppBlockDao

    private lateinit var binding: ActivityPomodoroBinding
    private var currentBlock: AppBlock? = null
    private var isFocusPhase = true
    private var sessionCount = 0
    private var timer: CountDownTimer? = null
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
        lifecycleScope.launch {
            val block = dao.getById(blockId) ?: run { finish(); return@launch }
            currentBlock = block
            runOnUiThread { startPhase(block, focusPhase = true) }
        }
    }

    private fun startPhase(block: AppBlock, focusPhase: Boolean) {
        isFocusPhase = focusPhase
        isPaused = false
        timer?.cancel()
        val durationMs = if (focusPhase) block.pomodoroDurationMin * 60_000L
        else block.pomodoroBreakMin * 60_000L

        binding.tvPhaseLabel.text = if (focusPhase) "FOCUS" else "BREAK"
        binding.tvPhaseLabel.setTextColor(getColor(
            if (focusPhase) android.R.color.holo_green_light
            else android.R.color.holo_blue_light
        ))
        binding.tvSessionCount.text = "Session ${sessionCount + 1}"
        binding.btnPauseResume.text = "Pause"
        binding.progressBar.progress = 0

        lifecycleScope.launch {
            if (focusPhase) dao.setPausedUntil(block.id, 0L)
            else dao.setPausedUntil(block.id, System.currentTimeMillis() + durationMs)
        }

        timer = object : CountDownTimer(durationMs, 1000L) {
            override fun onTick(ms: Long) {
                val min = ms / 60_000
                val sec = (ms % 60_000) / 1000
                val text = "%02d:%02d".format(min, sec)
                binding.tvCountdown.text = text
                binding.progressBar.progress = ((1 - ms.toFloat() / durationMs) * 100).toInt()
                showNotification("${if (focusPhase) "Focus" else "Break"}: $text remaining")
            }

            override fun onFinish() {
                vibrate()
                if (focusPhase) sessionCount++
                startPhase(block, !focusPhase)
            }
        }.start()

        binding.btnPauseResume.setOnClickListener {
            if (!isPaused) {
                timer?.cancel()
                isPaused = true
                binding.btnPauseResume.text = "Resume"
            } else {
                isPaused = false
                binding.btnPauseResume.text = "Pause"
                startPhase(block, isFocusPhase)
            }
        }
        binding.btnStop.setOnClickListener {
            timer?.cancel()
            cancelNotification()
            lifecycleScope.launch { dao.setPausedUntil(block.id, 0L) }
            finish()
        }
    }

    private fun vibrate() {
        val v = getSystemService(Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(500)
        }
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "Pomodoro", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun showNotification(text: String) {
        val stopPi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, PomodoroActivity::class.java).apply { action = "STOP" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("QR Zen Pomodoro")
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPi)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, notif)
    }

    private fun cancelNotification() {
        getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == "STOP") {
            timer?.cancel()
            cancelNotification()
            lifecycleScope.launch { currentBlock?.let { dao.setPausedUntil(it.id, 0L) } }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        cancelNotification()
    }
}
