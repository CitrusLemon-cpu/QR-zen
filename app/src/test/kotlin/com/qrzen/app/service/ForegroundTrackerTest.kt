package com.qrzen.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundTrackerTest {
    @Test
    fun shortUnknownGapKeepsLastConfirmedPackage() {
        val tracker = ForegroundTracker(10_000L)
        tracker.observe("example.app", 1_000L)

        val snapshot = tracker.observe(null, 8_000L)

        assertEquals("example.app", snapshot?.packageName)
        assertTrue(snapshot?.isGraceValue == true)
    }

    @Test
    fun confirmedReplacementEndsGraceState() {
        val tracker = ForegroundTracker(10_000L)
        tracker.observe("first.app", 1_000L)

        val snapshot = tracker.observe("second.app", 2_000L)

        assertEquals("second.app", snapshot?.packageName)
        assertFalse(snapshot?.isGraceValue ?: true)
    }

    @Test
    fun longUnknownGapReturnsUnknown() {
        val tracker = ForegroundTracker(10_000L)
        tracker.observe("example.app", 1_000L)

        assertNull(tracker.observe(null, 12_000L))
    }
}
