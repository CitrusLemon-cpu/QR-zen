package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.ui.lock.LockScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class BlockAccessibilityService : AccessibilityService() {

    @Inject lateinit var dao: AppBlockDao
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        scope.launch {
            val now = System.currentTimeMillis()
            val activeBlocks = dao.getAll().filter { block ->
                block.isEnabled &&
                now > block.pausedUntil &&
                isBlockActive(block) &&
                block.appPackages.split(",").map { it.trim() }.contains(pkg)
            }
            if (activeBlocks.isNotEmpty()) {
                launchLockScreen(pkg, activeBlocks.first())
            }
        }
    }

    private fun isBlockActive(block: AppBlock): Boolean {
        val now = LocalTime.now()
        val start = LocalTime.parse(block.startTime, fmt)
        val end = LocalTime.parse(block.endTime, fmt)
        val timeOk = if (end.isAfter(start)) {
            now.isAfter(start) && now.isBefore(end)
        } else {
            now.isAfter(start) || now.isBefore(end)
        }
        if (!timeOk) return false

        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return block.activeDays.getOrNull(dayIndex) == '1'
    }

    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
