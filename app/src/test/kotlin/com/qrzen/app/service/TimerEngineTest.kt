package com.qrzen.app.service

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerEngineTest {
    @Test
    fun firstForegroundSampleStartsTrackedSession() {
        val state = TimerEngine.initializeRemaining(
            maximumMs = 60_000L,
            nowElapsed = 15_000L,
            isTrackedNow = true
        )

        assertEquals(60_000L, state.remainingMs)
        assertEquals(15_000L, state.lastTickElapsed)
        assertEquals(true, state.wasTracked)
    }

    @Test
    fun interruptionConsumesFullTrackedInterval() {
        val remaining = TimerEngine.updateRemaining(
            remainingMs = 60_000L,
            maximumMs = 60_000L,
            elapsedMs = 45_000L,
            wasTracked = true
        )

        assertEquals(15_000L, remaining)
    }

    @Test
    fun rebootLikeElapsedClockResetDoesNotCreateNegativeDelta() {
        assertEquals(0L, TimerEngine.elapsedSince(90_000L, 5_000L))
    }

    @Test
    fun adaptiveWaitRefillsOnlyWhilePreviouslyOutsideTrackedApp() {
        val remaining = TimerEngine.updateRemaining(
            remainingMs = 10_000L,
            maximumMs = 60_000L,
            elapsedMs = 20_000L,
            wasTracked = false,
            adaptive = true,
            useMinutes = 5,
            waitMinutes = 10
        )

        assertEquals(20_000L, remaining)
    }

    @Test
    fun scheduledUsageIsCappedAtBudget() {
        val used = TimerEngine.updateUsed(
            usedMs = 50_000L,
            maximumMs = 60_000L,
            elapsedMs = 30_000L,
            wasTracked = true
        )

        assertEquals(60_000L, used)
    }
}
