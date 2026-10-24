package com.qrzen.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundSurfacePolicyTest {

    @Test
    fun `normal Home surface keeps its exemption`() {
        assertTrue(
            ForegroundSurfacePolicy.isExemptFromBlocking(
                normallyExempt = true,
                confirmedRecents = false
            )
        )
    }

    @Test
    fun `confirmed Recents overrides launcher exemption`() {
        assertFalse(
            ForegroundSurfacePolicy.isExemptFromBlocking(
                normallyExempt = true,
                confirmedRecents = true
            )
        )
    }

    @Test
    fun `ordinary apps remain subject to blocking`() {
        assertFalse(
            ForegroundSurfacePolicy.isExemptFromBlocking(
                normallyExempt = false,
                confirmedRecents = false
            )
        )
    }
}
