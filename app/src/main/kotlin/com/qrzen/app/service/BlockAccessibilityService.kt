package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        scope.launch {
            val dao = entryPoint.appBlockDao()
            val now = System.currentTimeMillis()
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
