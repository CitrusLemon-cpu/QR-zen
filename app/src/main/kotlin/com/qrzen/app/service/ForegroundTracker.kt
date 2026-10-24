package com.qrzen.app.service

/** Keeps short gaps in Android's foreground-app signals from stopping active timers. */
class ForegroundTracker(private val unknownGraceMs: Long) {
    data class Snapshot(val packageName: String, val isGraceValue: Boolean)

    private var lastPackage: String? = null
    private var lastConfirmedElapsed = 0L

    fun observe(candidate: String?, nowElapsed: Long): Snapshot? {
        if (!candidate.isNullOrBlank()) {
            lastPackage = candidate
            lastConfirmedElapsed = nowElapsed
            return Snapshot(candidate, false)
        }

        val previous = lastPackage ?: return null
        val age = TimerEngine.elapsedSince(lastConfirmedElapsed, nowElapsed)
        return if (age <= unknownGraceMs) Snapshot(previous, true) else null
    }

    fun clear() {
        lastPackage = null
        lastConfirmedElapsed = 0L
    }
}
