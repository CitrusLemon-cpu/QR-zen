package com.qrzen.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class QrZenNotificationListener : NotificationListenerService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    @Volatile private var cachedPackages: Set<String> = emptySet()
    @Volatile private var cachedAllowedPackages: Set<String> = emptySet()
    @Volatile private var cachedHasAllowlist: Boolean = false
    @Volatile private var cachedAllowlistBlockIds: List<Int> = emptyList()
    @Volatile private var lastFetchTime = 0L
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")
    private val systemNonLauncherCache = mutableMapOf<String, Boolean>()

    private fun isSystemNonLauncherApp(pkg: String): Boolean {
        systemNonLauncherCache[pkg]?.let { return it }
        val result = try {
            val appInfo = applicationContext.packageManager.getApplicationInfo(pkg, 0)
            val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            isSystem && applicationContext.packageManager.getLaunchIntentForPackage(pkg) == null
        } catch (_: Exception) {
            false
        }
        systemNonLauncherCache[pkg] = result
        return result
    }

    private suspend fun refreshCacheIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastFetchTime <= CACHE_TTL_MS) return

        val dao = EntryPointAccessors.fromApplication(
            applicationContext,
            WidgetEntryPoint::class.java
        ).appBlockDao()

        val activeBlocks = dao.getAll().filter { it.isEnabled && it.pausedUntil < now && isBlockActive(it) }
        cachedPackages = activeBlocks
            .filter { !it.isAllowlistMode }
            .flatMap { it.appPackages.split(",").map(String::trim) }
            .filter(String::isNotEmpty)
            .toSet()
        val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode }
        cachedHasAllowlist = allowlistBlocks.isNotEmpty()
        cachedAllowlistBlockIds = allowlistBlocks.map { it.id }
        cachedAllowedPackages = if (allowlistBlocks.isEmpty()) {
            emptySet()
        } else {
            allowlistBlocks
                .map { block ->
                    block.appPackages
                        .split(",")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .filterNot { Prefs.isAppTimerExpired(block.id, it) }
                        .toSet()
                }
                .reduce { acc, allowed -> acc.intersect(allowed) }
        }
        lastFetchTime = now
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (!Prefs.removeNotifications) return
        val notification = sbn ?: return
        val pkg = notification.packageName
        if (pkg == packageName) return
        scope.launch {
            refreshCacheIfNeeded()
            val blocked = cachedPackages
            val hasAllowlist = cachedHasAllowlist
            val allowed = cachedAllowedPackages

            if (pkg in blocked) {
                cancelNotification(notification.key)
                return@launch
            }
            if (hasAllowlist &&
                pkg !in allowed &&
                pkg !in SYSTEM_EXEMPT_PACKAGES &&
                !isSystemNonLauncherApp(pkg)
            ) {
                cancelNotification(notification.key)
                return@launch
            }
            if (hasAllowlist &&
                cachedAllowlistBlockIds.any { blockId -> Prefs.isAppTimerExpired(blockId, pkg) } &&
                pkg !in SYSTEM_EXEMPT_PACKAGES &&
                !isSystemNonLauncherApp(pkg)
            ) {
                cancelNotification(notification.key)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
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
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.intentresolver",
            "com.android.documentsui",
            "com.google.android.documentsui",
            "com.miui.securitycenter",
            "com.miui.securitycore",
            "com.miui.guardprovider",
            "com.miui.systemui.plugin",
            "com.miui.mishare",
            "com.miui.volume",
            "com.miui.securityinputmethod",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.samsung.android.app.sharelive",
            "com.android.emergency"
        )
    }
}
