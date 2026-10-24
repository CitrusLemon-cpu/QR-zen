package com.qrzen.app.service

/**
 * Pure timer-accounting helpers shared by the background timer loops.
 *
 * A persisted tick is an elapsed-realtime value. Elapsed realtime survives process
 * recreation during the same boot and cannot be changed by editing the device clock.
 */
object TimerEngine {
    data class InitialRemainingState(
        val remainingMs: Long,
        val lastTickElapsed: Long,
        val wasTracked: Boolean
    )

    fun initializeRemaining(
        maximumMs: Long,
        nowElapsed: Long,
        isTrackedNow: Boolean
    ): InitialRemainingState = InitialRemainingState(
        remainingMs = maximumMs.coerceAtLeast(0L),
        lastTickElapsed = nowElapsed.coerceAtLeast(0L),
        wasTracked = isTrackedNow
    )

    fun elapsedSince(lastTickElapsed: Long, nowElapsed: Long): Long {
        if (lastTickElapsed <= 0L || nowElapsed < lastTickElapsed) return 0L
        return nowElapsed - lastTickElapsed
    }

    fun updateRemaining(
        remainingMs: Long,
        maximumMs: Long,
        elapsedMs: Long,
        wasTracked: Boolean,
        adaptive: Boolean = false,
        useMinutes: Int = 0,
        waitMinutes: Int = 0
    ): Long {
        if (elapsedMs <= 0L) return remainingMs.coerceIn(0L, maximumMs)
        if (wasTracked) return (remainingMs - elapsedMs).coerceAtLeast(0L)
        if (!adaptive || waitMinutes <= 0) return remainingMs.coerceIn(0L, maximumMs)

        val refillRate = useMinutes.toDouble() / waitMinutes.toDouble()
        val refillMs = (elapsedMs * refillRate).toLong().coerceAtLeast(0L)
        return (remainingMs + refillMs).coerceAtMost(maximumMs)
    }

    fun updateUsed(
        usedMs: Long,
        maximumMs: Long,
        elapsedMs: Long,
        wasTracked: Boolean,
        adaptive: Boolean = false,
        useMinutes: Int = 0,
        waitMinutes: Int = 0
    ): Long {
        if (elapsedMs <= 0L) return usedMs.coerceIn(0L, maximumMs)
        if (wasTracked) return (usedMs + elapsedMs).coerceAtMost(maximumMs)
        if (!adaptive || waitMinutes <= 0) return usedMs.coerceIn(0L, maximumMs)

        val refillRate = useMinutes.toDouble() / waitMinutes.toDouble()
        val refillMs = (elapsedMs * refillRate).toLong().coerceAtLeast(0L)
        return (usedMs - refillMs).coerceAtLeast(0L)
    }
}
