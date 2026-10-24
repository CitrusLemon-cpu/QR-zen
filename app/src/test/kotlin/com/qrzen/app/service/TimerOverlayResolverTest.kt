package com.qrzen.app.service

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerOverlayResolverTest {
    @Test
    fun combinedWaitAndUsageTimersAreVisibleForSameForegroundApp() {
        val entries = TimerOverlayResolver.resolve(
            listOf(
                TimerOverlayResolver.Candidate(1, "Wait timer", 45_000L, true),
                TimerOverlayResolver.Candidate(2, "Total usage", 240_000L, true)
            )
        )

        assertEquals(listOf(1, 2), entries.map { it.blockId })
    }

    @Test
    fun expiredUsageTimerDoesNotRemoveLiveWaitTimer() {
        val entries = TimerOverlayResolver.resolve(
            listOf(
                TimerOverlayResolver.Candidate(1, "Wait timer", 45_000L, true),
                TimerOverlayResolver.Candidate(2, "Total usage", 0L, true)
            )
        )

        assertEquals(listOf(1), entries.map { it.blockId })
    }

    @Test
    fun timerIsHiddenAfterForegroundAppChanges() {
        val entries = TimerOverlayResolver.resolve(
            listOf(TimerOverlayResolver.Candidate(1, "Wait timer", 45_000L, false))
        )

        assertEquals(emptyList<TimerOverlayResolver.Entry>(), entries)
    }
}
