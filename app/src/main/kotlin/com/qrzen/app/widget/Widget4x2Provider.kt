package com.qrzen.app.widget
import android.app.PendingIntent; import android.appwidget.AppWidgetManager; import android.appwidget.AppWidgetProvider; import android.content.Context; import android.content.Intent; import android.view.View; import android.widget.RemoteViews
import com.qrzen.app.R; import com.qrzen.app.di.WidgetEntryPoint; import com.qrzen.app.ui.main.MainActivity
import dagger.hilt.android.EntryPointAccessors; import kotlinx.coroutines.runBlocking
class Widget4x2Provider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dao = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java).appBlockDao()
        val blocks = runBlocking { dao.getAll() }; val now = System.currentTimeMillis()
        val active = blocks.filter { it.isEnabled && it.pausedUntil < now }
        val status = when(active.size) { 0->"No blocks active"; 1->"1 block active"; else->"${active.size} blocks active" }
        val pi = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        for (id in appWidgetIds) {
            val v = RemoteViews(context.packageName, R.layout.widget_4x2)
            v.setTextViewText(R.id.tvStatus, status)
            listOf(R.id.tvBlock1, R.id.tvBlock2, R.id.tvBlock3).forEachIndexed { i, vid ->
                val b = active.getOrNull(i)
                if (b != null) { v.setTextViewText(vid, "• ${b.title}"); v.setViewVisibility(vid, View.VISIBLE) }
                else v.setViewVisibility(vid, View.GONE)
            }
            v.setViewVisibility(R.id.tvNoBlocks, if (active.isEmpty()) View.VISIBLE else View.GONE)
            v.setOnClickPendingIntent(R.id.root, pi); appWidgetManager.updateAppWidget(id, v)
        }
    }
}
