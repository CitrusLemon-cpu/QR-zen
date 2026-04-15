package com.qrzen.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.qrzen.app.service.BackgroundService

/**
 * 1-pixel transparent activity used as the third survival mechanism.
 *
 * When launched silently (e.g. from a widget or alarm), it brings the
 * process to the foreground just long enough for BackgroundService to
 * restart, then immediately finishes. It never appears in the Recents
 * screen and has a fully transparent theme.
 *
 * This is how yuanlishouji survives ultra battery saver mode on devices
 * where foreground services alone are insufficient.
 */
class StartApp1PxActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        BackgroundService.start(this)
        finish()
    }
}
