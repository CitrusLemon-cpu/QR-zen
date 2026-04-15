package com.qrzen.app.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.qrzen.app.data.prefs.Prefs

object SilentModeHelper {
    fun applySilentMode(context: Context) {
        if (!Prefs.silentMode) return
        val nm = context.getSystemService(NotificationManager::class.java)
        if (!nm.isNotificationPolicyAccessGranted) return
        context.getSystemService(AudioManager::class.java)
            .ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    fun restoreRinger(context: Context) {
        if (!Prefs.silentMode) return
        val nm = context.getSystemService(NotificationManager::class.java)
        if (!nm.isNotificationPolicyAccessGranted) return
        context.getSystemService(AudioManager::class.java)
            .ringerMode = AudioManager.RINGER_MODE_NORMAL
    }
}
