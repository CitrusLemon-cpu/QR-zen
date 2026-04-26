package com.qrzen.app.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.qrzen.app.R

class AppTimerOverlay(private val context: Context) {

    data class TimerEntry(val packageName: String, val appIcon: Drawable, val remainingMs: Long)

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: LinearLayout? = null
    private val entryViews = mutableListOf<Pair<ImageView, TextView>>()
    private var isShowing = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.END
        x = 24
        y = 100
    }

    private val touchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = initialX - (event.rawX - initialTouchX).toInt()
                layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
                true
            }
            else -> false
        }
    }

    fun update(entries: List<TimerEntry>) {
        mainHandler.post { updateInternal(entries) }
    }

    fun hide() {
        mainHandler.post { hideInternal() }
    }

    fun destroy() {
        mainHandler.post { hideInternal() }
    }

    private fun updateInternal(entries: List<TimerEntry>) {
        if (entries.isEmpty() || !Settings.canDrawOverlays(context)) {
            hideInternal()
            return
        }

        val container = ensureOverlayView() ?: return
        syncEntryViews(container, entries.size)
        entries.forEachIndexed { index, entry ->
            val (iconView, textView) = entryViews[index]
            iconView.setImageDrawable(entry.appIcon)
            iconView.alpha = 0.5f
            textView.text = formatDuration(entry.remainingMs)
        }
        if (!isShowing) {
            try {
                windowManager.addView(container, layoutParams)
                isShowing = true
            } catch (_: Exception) {
                overlayView = null
                entryViews.clear()
                isShowing = false
            }
        } else {
            try {
                windowManager.updateViewLayout(container, layoutParams)
            } catch (_: Exception) {
            }
        }
    }

    private fun ensureOverlayView(): LinearLayout? {
        overlayView?.let { return it }
        return runCatching {
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_overlay_timer)
                val horizontalPadding = dpToPx(10)
                val verticalPadding = dpToPx(6)
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                setOnTouchListener(touchListener)
            }
        }.getOrNull()?.also { overlayView = it }
    }

    private fun syncEntryViews(container: LinearLayout, count: Int) {
        while (entryViews.size < count) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val iconSize = dpToPx(24)
            val iconView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                alpha = 0.5f
            }
            val textView = TextView(context).apply {
                setTextColor(Color.WHITE)
                alpha = 0.85f
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(typeface, Typeface.BOLD)
                val startMargin = dpToPx(8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = startMargin }
            }
            row.addView(iconView)
            row.addView(textView)
            entryViews.add(iconView to textView)
            container.addView(row)
        }
        while (entryViews.size > count) {
            entryViews.removeAt(entryViews.lastIndex)
            container.removeViewAt(container.childCount - 1)
        }
    }

    private fun formatDuration(remainingMs: Long): String {
        val totalSeconds = (remainingMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    private fun hideInternal() {
        val view = overlayView ?: run {
            entryViews.clear()
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
        entryViews.clear()
        isShowing = false
    }

    private fun dpToPx(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
