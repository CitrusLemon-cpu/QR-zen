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

class Widget1x1Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .appBlockDao()
        val blocks = runBlocking { dao.getAll() }
        val activeCount = blocks.count { it.isEnabled && it.pausedUntil < System.currentTimeMillis() }

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_1x1)
            views.setTextViewText(R.id.tvCount, activeCount.toString())
            views.setOnClickPendingIntent(R.id.root, pendingIntent)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
