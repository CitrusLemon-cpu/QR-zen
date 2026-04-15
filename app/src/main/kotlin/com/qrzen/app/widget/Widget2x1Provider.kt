package com.qrzen.app.widget
import android.app.PendingIntent; import android.appwidget.AppWidgetManager; import android.appwidget.AppWidgetProvider; import android.content.Context; import android.content.Intent; import android.widget.RemoteViews
import com.qrzen.app.R; import com.qrzen.app.di.WidgetEntryPoint; import com.qrzen.app.ui.main.MainActivity
import dagger.hilt.android.EntryPointAccessors; import kotlinx.coroutines.runBlocking
class Widget2x1Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dao = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java).appBlockDao()
        val blocks = runBlocking { dao.getAll() }; val now = System.currentTimeMillis()
        val n = blocks.count { it.isEnabled && it.pausedUntil < now }
        val status = when(n) { 0->"No blocks active"; 1->"1 block active"; else->"$n blocks active" }
        val pi = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        for (id in appWidgetIds) {
            val v = RemoteViews(context.packageName, R.layout.widget_2x1)
            v.setTextViewText(R.id.tvCount, n.toString()); v.setTextViewText(R.id.tvStatus, status); v.setOnClickPendingIntent(R.id.root, pi)
            appWidgetManager.updateAppWidget(id, v)
        }
    }
}
