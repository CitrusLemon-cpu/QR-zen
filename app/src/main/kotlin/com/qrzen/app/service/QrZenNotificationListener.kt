package com.qrzen.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class QrZenNotificationListener : NotificationListenerService() {
    private var cachedPackages: Set<String> = emptySet()
    private var lastFetchTime = 0L

    private val blockedPackages: Set<String>
        get() {
            val now = System.currentTimeMillis()
            if (now - lastFetchTime > CACHE_TTL_MS) {
                val dao = EntryPointAccessors.fromApplication(
                    applicationContext,
                    WidgetEntryPoint::class.java
                ).appBlockDao()
                cachedPackages = runBlocking {
                    dao.getAll()
                        .filter { it.isEnabled && it.pausedUntil < now }
                        .flatMap { it.appPackages.split(",").map(String::trim) }
                        .filter(String::isNotEmpty)
                        .toSet()
                }
                lastFetchTime = now
            }
            return cachedPackages
        }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (!Prefs.removeNotifications) return
        val notification = sbn ?: return
        val pkg = notification.packageName
        if (pkg == packageName) return
        if (pkg in blockedPackages) cancelNotification(notification.key)
    }

    companion object {
        private const val CACHE_TTL_MS = 5_000L
    }
}
