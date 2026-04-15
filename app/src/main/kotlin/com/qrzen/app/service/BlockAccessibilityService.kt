package com.qrzen.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Stub — full implementation in Task 2 (block enforcement + QR unlock overlay).
 *
 * Will listen for typeWindowStateChanged events to detect foreground app
 * changes, match against active AppBlock rules, and launch LockScreenActivity
 * when a blocked app is detected.
 */
class BlockAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TODO Task 2: detect foreground app, match AppBlock rules, show overlay
    }

    override fun onInterrupt() {}
}
