package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.di.WidgetEntryPoint
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
import com.qrzen.app.ui.lock.LockScreenActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class BlockAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QrZenAccessibility"

        /** System packages that must never be blocked by allowlist mode. */
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
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.android.emergency"
        )
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error in accessibility service", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            WidgetEntryPoint::class.java
        )
    }

    /** All packages that declare themselves as launchers (ACTION_MAIN + CATEGORY_HOME). */
    private val launcherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val dialerPackages: Set<String> by lazy {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:")
        }
        packageManager.queryIntentActivities(dialIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private fun isExemptFromAllowlist(pkg: String): Boolean {
        return pkg in SYSTEM_EXEMPT_PACKAGES || pkg in launcherPackages || pkg in dialerPackages
    }

    private fun isDeviceLocked(): Boolean {
        val km = getSystemService(KeyguardManager::class.java) ?: return false
        return km.isKeyguardLocked
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        scope.launch {
            val dao = entryPoint.appBlockDao()
            val now = System.currentTimeMillis()
            if (Prefs.pauseAllUntil > now) return@launch
            val activeBlocks = dao.getAll().filter { it.isEnabled && now > it.pausedUntil && isBlockActive(it) }

            val blocklistMatch = activeBlocks
                .filter { !it.isAllowlistMode }
                .firstOrNull { block ->
                    block.appPackages.split(",").map { it.trim() }.contains(pkg)
                }
            if (blocklistMatch != null) {
                launchLockScreen(pkg, blocklistMatch)
                return@launch
            }

            // Skip allowlist blocking for exempt packages and locked device
            if (isExemptFromAllowlist(pkg) || isDeviceLocked()) return@launch

            val allowlistMatch = activeBlocks
                .filter { it.isAllowlistMode }
                .firstOrNull { block ->
                    val allowed = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                    !allowed.contains(pkg)
                }
            if (allowlistMatch != null) {
                launchAllowlistOverlay(pkg, allowlistMatch)
            }
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

    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
        logBlockEvent(block, blockedPkg)
    }

    private fun launchAllowlistOverlay(blockedPkg: String, block: AppBlock) {
        startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(AllowlistOverlayActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        })
        logBlockEvent(block, blockedPkg)
    }

    private fun logBlockEvent(block: AppBlock, blockedPkg: String) {
        scope.launch {
            entryPoint.blockEventDao().insert(
                BlockEvent(
                    blockId = block.id,
                    blockTitle = block.title,
                    packageName = blockedPkg,
                    eventType = "BLOCKED"
                )
            )
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
