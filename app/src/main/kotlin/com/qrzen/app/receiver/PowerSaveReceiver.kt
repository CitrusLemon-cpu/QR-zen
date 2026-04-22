package com.qrzen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.qrzen.app.service.BackgroundService

class PowerSaveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) return
        val powerManager = context.getSystemService(PowerManager::class.java) ?: return
        if (!powerManager.isPowerSaveMode) {
            BackgroundService.start(context)
        }
    }
}
