package com.qrzen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qrzen.app.service.BackgroundService

/**
 * Listens to 25 different system broadcasts at MAX priority (2147483647).
 *
 * This is the primary ultra-battery-saver survival mechanism, mirroring
 * yuanlishouji's approach. By catching every possible system event we
 * ensure BackgroundService is restarted as quickly as possible after a
 * reboot, system kill, or any power/screen state change — even when the
 * system has aggressively killed background processes.
 */
class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BackgroundService.start(context)
    }
}
