package com.qrzen.app.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.qrzen.app.R

class AccessibilityBlockOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var overlayView: View? = null
    @Volatile private var isShowing = false

    fun show() {
        mainHandler.post { showInternal() }
    }

    fun hide() {
        mainHandler.post { hideInternal() }
    }

    fun destroy() {
        mainHandler.post { hideInternal() }
    }

    private fun showInternal() {
        if (isShowing) return
        if (!Settings.canDrawOverlays(context)) return

        val view = buildOverlayView()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(view, params)
            overlayView = view
            isShowing = true
        } catch (_: Exception) {
            overlayView = null
            isShowing = false
        }
    }

    private fun hideInternal() {
        val view = overlayView ?: return
        if (isShowing) {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        isShowing = false
    }

    private fun buildOverlayView(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F0121212"))
            val pad = dpToPx(32)
            setPadding(pad, pad, pad, pad)

            addView(TextView(context).apply {
                text = "\u26A0\uFE0F"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 48f)
                gravity = Gravity.CENTER
            })

            addView(TextView(context).apply {
                text = context.getString(R.string.accessibility_block_title)
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
                val topMargin = dpToPx(24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, topMargin, 0, 0) }
            })

            addView(TextView(context).apply {
                text = context.getString(R.string.accessibility_block_message)
                setTextColor(Color.parseColor("#AAFFFFFF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                gravity = Gravity.CENTER
                val topMargin = dpToPx(16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, topMargin, 0, 0) }
            })

            addView(Button(context).apply {
                text = context.getString(R.string.accessibility_block_button)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#BB86FC"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                val topMargin = dpToPx(32)
                val hPad = dpToPx(24)
                val vPad = dpToPx(12)
                setPadding(hPad, vPad, hPad, vPad)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, topMargin, 0, 0) }
                setOnClickListener {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            })
        }
    }

    private fun dpToPx(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
