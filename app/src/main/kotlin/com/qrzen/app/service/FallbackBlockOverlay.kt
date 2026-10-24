package com.qrzen.app.service

import android.content.Context
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

/** Touch-blocking safety screen used when Usage Access is the foreground source. */
class FallbackBlockOverlay(
    private val context: Context,
    private val onUnlock: (Target) -> Unit,
    private val onGoHome: () -> Unit
) {
    data class Target(
        val packageName: String,
        val blockIds: IntArray,
        val title: String,
        val isAllowlist: Boolean
    )

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var currentTarget: Target? = null
    private var overlayView: View? = null

    fun show(target: Target) {
        val previous = currentTarget
        currentTarget = target
        mainHandler.post {
            if (overlayView != null && targetsEquivalent(previous, target)) return@post
            showInternal(target)
        }
    }

    fun hide() {
        currentTarget = null
        mainHandler.post { hideInternal() }
    }

    fun destroy() = hide()

    private fun showInternal(target: Target) {
        if (!Settings.canDrawOverlays(context)) {
            hideInternal()
            return
        }
        hideInternal()
        val view = buildView(target)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        runCatching { windowManager.addView(view, params) }
            .onSuccess { overlayView = view }
            .onFailure { overlayView = null }
    }

    private fun hideInternal() {
        overlayView?.let { view -> runCatching { windowManager.removeView(view) } }
        overlayView = null
    }

    private fun targetsEquivalent(first: Target?, second: Target): Boolean =
        first != null &&
            first.packageName == second.packageName &&
            first.isAllowlist == second.isAllowlist &&
            first.blockIds.contentEquals(second.blockIds)

    private fun buildView(target: Target): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.parseColor("#F0121212"))
        val pad = dp(32)
        setPadding(pad, pad, pad, pad)
        isClickable = true
        isFocusable = true

        addView(TextView(context).apply {
            text = "\uD83D\uDEE1\uFE0F"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 46f)
        })
        addView(TextView(context).apply {
            text = target.title
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            layoutParams = marginParams(20)
        })
        addView(TextView(context).apply {
            text = context.getString(R.string.fallback_block_message)
            setTextColor(Color.parseColor("#CCFFFFFF"))
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            layoutParams = marginParams(12)
        })
        addView(Button(context).apply {
            text = context.getString(R.string.fallback_complete_unlock)
            layoutParams = marginParams(28)
            setOnClickListener {
                currentTarget?.let(onUnlock)
                hide()
            }
        })
        addView(Button(context).apply {
            text = context.getString(R.string.fallback_go_home)
            layoutParams = marginParams(8)
            setOnClickListener {
                hide()
                onGoHome()
            }
        })
    }

    private fun marginParams(top: Int) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, dp(top), 0, 0) }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}
