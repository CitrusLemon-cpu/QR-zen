package com.qrzen.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class QrZenNotificationListener : NotificationListenerService() {
    private var cachedPackages: Set<String> = emptySet()
    private var cachedAllowedPackages: Set<String> = emptySet()
    private var cachedHasAllowlist: Boolean = false
    private var lastFetchTime = 0L
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    private val blockedPackages: Set<String>
        get() {
            val now = System.currentTimeMillis()
            if (now - lastFetchTime > CACHE_TTL_MS) {
                val dao = EntryPointAccessors.fromApplication(
                    applicationContext,
                    WidgetEntryPoint::class.java
                ).appBlockDao()
                runBlocking {
                    val activeBlocks = dao.getAll().filter { it.isEnabled && it.pausedUntil < now && isBlockActive(it) }
                    cachedPackages = activeBlocks
                        .filter { !it.isAllowlistMode }
                        .flatMap { it.appPackages.split(",").map(String::trim) }
                        .filter(String::isNotEmpty)
                        .toSet()
                    val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode }
                    cachedHasAllowlist = allowlistBlocks.isNotEmpty()
                    cachedAllowedPackages = if (allowlistBlocks.isEmpty()) {
                        emptySet()
                    } else {
                        allowlistBlocks
                            .map { block ->
                                block.appPackages.split(",").map(String::trim).filter(String::isNotEmpty).toSet()
                            }
                            .reduce { acc, allowed -> acc.intersect(allowed) }
                    }
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
        if (pkg in blockedPackages) {
            cancelNotification(notification.key)
            return
        }
        if (cachedHasAllowlist && pkg !in cachedAllowedPackages && pkg !in SYSTEM_EXEMPT_PACKAGES) {
            cancelNotification(notification.key)
        }
    }

    private fun isBlockActive(block: AppBlock): Boolean {
        val now = LocalTime.now()
        val start = LocalTime.parse(block.startTime, fmt)
        val end = LocalTime.parse(block.endTime, fmt)
        val timeOk = if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
        else !now.isBefore(start) || !now.isAfter(end)
        if (!timeOk) return false
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return block.activeDays.getOrNull(dayIndex) == '1'
    }

    companion object {
        private const val CACHE_TTL_MS = 5_000L

        private val SYSTEM_EXEMPT_PACKAGES = setOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.android.emergency"
        )
    }
}
