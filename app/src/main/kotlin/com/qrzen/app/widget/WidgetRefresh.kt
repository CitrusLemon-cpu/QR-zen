package com.qrzen.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetRefresh {
    fun refresh(context: Context) {
        val awm = AppWidgetManager.getInstance(context)
        listOf(
            Widget1x1Provider::class.java,
            Widget2x1Provider::class.java,
            Widget4x1Provider::class.java,
            Widget4x2Provider::class.java
        ).forEach { cls ->
            val ids = awm.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, cls).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        }
    }
}
