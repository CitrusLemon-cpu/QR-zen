package com.qrzen.app.util

import android.os.SystemClock

class BruteForceGuard {
    private var failedAttempts = 0
    private var lockoutUntil = 0L

    fun checkAllowed(): Long? {
        val remaining = lockoutUntil - SystemClock.elapsedRealtime()
        return remaining.takeIf { it > 0L }
    }

    fun recordFailure() {
        failedAttempts += 1
        val lockoutDuration = when (failedAttempts) {
            in 0..2 -> 0L
            in 3..4 -> 30_000L
            in 5..9 -> 2 * 60_000L
            else -> 10 * 60_000L
        }
        lockoutUntil = if (lockoutDuration > 0L) {
            SystemClock.elapsedRealtime() + lockoutDuration
        } else {
            0L
        }
    }

    fun reset() {
        failedAttempts = 0
        lockoutUntil = 0L
    }
}
