package com.qrzen.app.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.os.UserManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

enum class ProtectionMode {
    PRIMARY,
    FALLBACK,
    LIMITED,
    UNPROTECTED
}

data class ProtectionSnapshot(
    val accessibilityConfigured: Boolean,
    val accessibilityConnected: Boolean,
    val usageAccess: Boolean,
    val overlayAccess: Boolean,
    val userUnlocked: Boolean,
    val mode: ProtectionMode
)

object ProtectionModeResolver {
    fun resolve(
        accessibilityConnected: Boolean,
        usageAccess: Boolean,
        overlayAccess: Boolean,
        userUnlocked: Boolean
    ): ProtectionMode = when {
        accessibilityConnected -> ProtectionMode.PRIMARY
        usageAccess && overlayAccess && userUnlocked -> ProtectionMode.FALLBACK
        usageAccess && userUnlocked -> ProtectionMode.LIMITED
        else -> ProtectionMode.UNPROTECTED
    }
}

/** Process-local proof that Android actually bound the accessibility service. */
object AccessibilityHealthMonitor {
    @Volatile private var connected = false
    @Volatile private var connectedAtElapsed = 0L
    @Volatile private var lastEventElapsed = 0L

    fun markConnected(nowElapsed: Long = SystemClock.elapsedRealtime()) {
        connected = true
        connectedAtElapsed = nowElapsed
        lastEventElapsed = 0L
    }

    fun markEvent(nowElapsed: Long = SystemClock.elapsedRealtime()) {
        connected = true
        lastEventElapsed = nowElapsed
    }

    fun markDisconnected() {
        connected = false
        connectedAtElapsed = 0L
        lastEventElapsed = 0L
    }

    fun isConnected(): Boolean = connected
    fun connectedAtElapsed(): Long = connectedAtElapsed
    fun lastEventElapsed(): Long = lastEventElapsed
}

object ProtectionStatus {
    fun snapshot(context: Context): ProtectionSnapshot {
        val configured = isAccessibilityConfigured(context)
        val connected = AccessibilityHealthMonitor.isConnected() &&
            BlockAccessibilityService.isRunning
        val usageAccess = hasUsageAccess(context)
        val overlayAccess = Settings.canDrawOverlays(context)
        val userUnlocked = context.getSystemService(UserManager::class.java)?.isUserUnlocked != false
        return ProtectionSnapshot(
            accessibilityConfigured = configured,
            accessibilityConnected = connected,
            usageAccess = usageAccess,
            overlayAccess = overlayAccess,
            userUnlocked = userUnlocked,
            mode = ProtectionModeResolver.resolve(
                accessibilityConnected = connected,
                usageAccess = usageAccess,
                overlayAccess = overlayAccess,
                userUnlocked = userUnlocked
            )
        )
    }

    fun isAccessibilityConfigured(context: Context): Boolean {
        val target = ComponentName(context, BlockAccessibilityService::class.java)
        val managerMatch = context.getSystemService(AccessibilityManager::class.java)
            ?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any { info ->
                val serviceInfo = info.resolveInfo?.serviceInfo ?: return@any false
                serviceInfo.packageName == target.packageName &&
                    normalizeClassName(serviceInfo.packageName, serviceInfo.name) == target.className
            } == true
        if (managerMatch) return true

        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        return enabled.split(':').any { entry ->
            entry.equals(target.flattenToString(), ignoreCase = true) ||
                entry.equals(target.flattenToShortString(), ignoreCase = true)
        }
    }

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
        }.getOrElse { return false }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun normalizeClassName(packageName: String, className: String): String =
        when {
            className.startsWith('.') -> packageName + className
            '.' !in className -> "$packageName.$className"
            else -> className
        }
}
