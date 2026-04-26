package com.qrzen.app.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.qrzen.app.R

class WaitTimerOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var timerText: TextView? = null
    private var isShowing = false

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 24
        y = 100
    }

    fun show(remainingMs: Long) {
        mainHandler.post { showInternal(remainingMs) }
    }

    fun hide() {
        mainHandler.post { hideInternal() }
    }

    fun destroy() {
        mainHandler.post { hideInternal() }
    }

    private fun showInternal(remainingMs: Long) {
        if (!Settings.canDrawOverlays(context)) {
            hideInternal()
            return
        }

        val min = remainingMs / 60_000
        val sec = (remainingMs % 60_000) / 1000
        val text = String.format("%d:%02d", min, sec)

        if (!isShowing) {
            val view = LayoutInflater.from(context).inflate(R.layout.overlay_wait_timer, null)
            overlayView = view
            timerText = view.findViewById(R.id.tvOverlayTimer)
            timerText?.text = text
            try {
                windowManager.addView(view, layoutParams)
                isShowing = true
            } catch (_: Exception) {
                overlayView = null
                timerText = null
                isShowing = false
            }
        } else {
            timerText?.text = text
        }
    }

    private fun hideInternal() {
        val view = overlayView ?: run {
            timerText = null
            isShowing = false
            return
        }

        if (isShowing) {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        timerText = null
        isShowing = false
    }
}
