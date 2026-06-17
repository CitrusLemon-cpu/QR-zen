package com.qrzen.app.service

import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import android.os.Looper

class AudioBlockManager(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val mediaSessionManager = appContext.getSystemService(MediaSessionManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val activeSessionsComponent = ComponentName(appContext, QrZenNotificationListener::class.java)
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { }
    private val audioFocusRequest =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
        } else {
            null
        }

    @Volatile
    private var blockedPackages: Set<String> = emptySet()

    private var isStarted = false
    private var hasAudioFocus = false

    private val monitorRunnable = object : Runnable {
        override fun run() {
            enforceBlocking()
            if (isStarted) {
                handler.postDelayed(this, CHECK_INTERVAL_MS)
            }
        }
    }

    fun start() {
        if (isStarted) return
        isStarted = true
        handler.post(monitorRunnable)
    }

    fun stop() {
        if (!isStarted) return
        isStarted = false
        handler.removeCallbacks(monitorRunnable)
        abandonAudioFocus()
    }

    fun destroy() {
        stop()
    }

    fun updateBlockedPackages(packages: Set<String>) {
        blockedPackages = packages
        if (!isStarted) return
        handler.post { enforceBlocking() }
    }

    private fun enforceBlocking() {
        val packages = blockedPackages
        if (packages.isEmpty()) {
            abandonAudioFocus()
            return
        }

        val controllers = getActiveMediaControllers()
        var pausedAny = false
        var blockedSessionStillActive = false

        controllers.forEach { controller ->
            val packageName = controller.packageName ?: return@forEach
            if (packageName !in packages) return@forEach
            val state = runCatching { controller.playbackState?.state }.getOrNull()
            val isPlaying = state == android.media.session.PlaybackState.STATE_PLAYING
                    || state == android.media.session.PlaybackState.STATE_BUFFERING
            if (isPlaying) {
                val paused = runCatching { controller.transportControls.pause(); true }.getOrDefault(false)
                if (paused) pausedAny = true else blockedSessionStillActive = true
            } else {
                blockedSessionStillActive = blockedSessionStillActive ||
                    (state != null && state != android.media.session.PlaybackState.STATE_PAUSED
                            && state != android.media.session.PlaybackState.STATE_STOPPED
                            && state != android.media.session.PlaybackState.STATE_NONE
                            && state != android.media.session.PlaybackState.STATE_ERROR)
            }
        }

        if (blockedSessionStillActive || pausedAny) {
            requestAudioFocus()
        } else {
            abandonAudioFocus()
        }
    }

    private fun getActiveMediaControllers(): List<MediaController> {
        return runCatching {
            mediaSessionManager.getActiveSessions(activeSessionsComponent)
        }.getOrDefault(emptyList())
    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        hasAudioFocus = granted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
        hasAudioFocus = false
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 2_500L
    }
}
