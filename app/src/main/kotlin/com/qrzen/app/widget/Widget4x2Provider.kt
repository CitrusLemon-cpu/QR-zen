package com.qrzen.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.qrzen.app.R
import com.qrzen.app.di.WidgetEntryPoint
import com.qrzen.app.ui.main.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class Widget4x2Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .appBlockDao()
        val blocks = runBlocking { dao.getAll() }
        val now = System.currentTimeMillis()
        val active = blocks.filter { it.isEnabled && it.pausedUntil < now }

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = when (active.size) {
            0 -> "No blocks active"
            1 -> "1 block active"
            else -> "${active.size} blocks active"
        }

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_4x2)
            views.setTextViewText(R.id.tvStatus, statusText)

            val rowIds = listOf(R.id.tvBlock1, R.id.tvBlock2, R.id.tvBlock3)
            rowIds.forEachIndexed { index, viewId ->
                val block = active.getOrNull(index)
                if (block != null) {
                    val paused = block.pausedUntil > now
                    val label = "• ${block.title}${if (paused) " (paused)" else ""}"
                    views.setTextViewText(viewId, label)
                    views.setViewVisibility(viewId, View.VISIBLE)
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }

            val showEmpty = active.isEmpty()
            views.setViewVisibility(R.id.tvNoBlocks, if (showEmpty) View.VISIBLE else View.GONE)

            views.setOnClickPendingIntent(R.id.root, pendingIntent)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
