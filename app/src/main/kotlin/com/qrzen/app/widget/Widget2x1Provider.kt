package com.qrzen.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.qrzen.app.R
import com.qrzen.app.di.WidgetEntryPoint
import com.qrzen.app.ui.main.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class Widget2x1Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .appBlockDao()
        val blocks = runBlocking { dao.getAll() }
        val now = System.currentTimeMillis()
        val activeCount = blocks.count { it.isEnabled && it.pausedUntil < now }

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = when (activeCount) {
            0 -> "No blocks active"
            1 -> "1 block active"
            else -> "$activeCount blocks active"
        }

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_2x1)
            views.setTextViewText(R.id.tvCount, activeCount.toString())
            views.setTextViewText(R.id.tvStatus, statusText)
            views.setOnClickPendingIntent(R.id.root, pendingIntent)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
