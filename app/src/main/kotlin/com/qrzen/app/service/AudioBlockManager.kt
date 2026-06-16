package com.qrzen.app.service

import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap

class AudioBlockManager(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val mediaSessionManager = appContext.getSystemService(MediaSessionManager::class.java)
    private val packageManager = appContext.packageManager
    private val handler = Handler(Looper.getMainLooper())
    private val uidPackagesCache = ConcurrentHashMap<Int, Set<String>>()
    private val activeSessionsComponent = ComponentName(appContext, QrZenNotificationListener::class.java)
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { }
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val audioFocusRequest =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
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

    private val playbackCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : AudioManager.AudioPlaybackCallback() {
                override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>) {
                    updateAudioFocusForPlayback(configs.toList(), getBlockedSessionPackages())
                }
            }
        } else {
            null
        }

    fun start() {
        if (isStarted) return
        isStarted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playbackCallback?.let { audioManager.registerAudioPlaybackCallback(it, handler) }
        }
        handler.post(monitorRunnable)
    }

    fun stop() {
        if (!isStarted) return
        isStarted = false
        handler.removeCallbacks(monitorRunnable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playbackCallback?.let {
                runCatching { audioManager.unregisterAudioPlaybackCallback(it) }
            }
        }
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

        val blockedSessionPackages = pauseBlockedMediaSessions(packages)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            updateAudioFocusForPlayback(audioManager.activePlaybackConfigurations.orEmpty(), blockedSessionPackages)
        }
    }

    private fun pauseBlockedMediaSessions(packages: Set<String>): Set<String> {
        val controllers = getActiveMediaControllers()
        val blockedSessionPackages = mutableSetOf<String>()
        controllers.forEach { controller ->
            val packageName = controller.packageName ?: return@forEach
            if (packageName !in packages) return@forEach
            val paused = runCatching { controller.transportControls.pause(); true }.getOrDefault(false)
            if (paused) blockedSessionPackages += packageName
        }
        return blockedSessionPackages
    }

    private fun getBlockedSessionPackages(): Set<String> {
        val packages = blockedPackages
        if (packages.isEmpty()) return emptySet()
        return getActiveMediaControllers()
            .mapNotNull { it.packageName }
            .filterTo(mutableSetOf()) { it in packages }
    }

    private fun getActiveMediaControllers(): List<MediaController> {
        return runCatching {
            mediaSessionManager.getActiveSessions(activeSessionsComponent)
        }.getOrDefault(emptyList())
    }

    private fun updateAudioFocusForPlayback(
        configs: List<AudioPlaybackConfiguration>,
        blockedSessionPackages: Set<String>
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val packages = blockedPackages
        if (packages.isEmpty()) {
            abandonAudioFocus()
            return
        }
        val shouldInterrupt = configs.any { config ->
            config.isActive && resolvePackagesForUid(config.clientUid).any { pkg ->
                pkg in packages && pkg !in blockedSessionPackages
            }
        }
        if (shouldInterrupt) {
            requestAudioFocus()
        } else {
            abandonAudioFocus()
        }
    }

    private fun resolvePackagesForUid(uid: Int): Set<String> {
        return uidPackagesCache.getOrPut(uid) {
            packageManager.getPackagesForUid(uid)?.toSet().orEmpty()
        }
    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
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
            audioManager.abandonAudioFocus(audioFocusListener)
        }
        hasAudioFocus = false
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 2_500L
    }
}
