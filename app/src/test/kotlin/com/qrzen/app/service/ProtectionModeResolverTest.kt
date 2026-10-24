package com.qrzen.app.service

import org.junit.Assert.assertEquals
import org.junit.Test

class ProtectionModeResolverTest {
    @Test fun connectedAccessibilityIsPrimary() {
        assertEquals(ProtectionMode.PRIMARY, ProtectionModeResolver.resolve(true, false, false, false))
    }

    @Test fun usageAndOverlayProvideFallbackAfterUnlock() {
        assertEquals(ProtectionMode.FALLBACK, ProtectionModeResolver.resolve(false, true, true, true))
    }

    @Test fun lockedUserCannotUseUsageFallback() {
        assertEquals(ProtectionMode.UNPROTECTED, ProtectionModeResolver.resolve(false, true, true, false))
    }

    @Test fun missingOverlayIsLimited() {
        assertEquals(ProtectionMode.LIMITED, ProtectionModeResolver.resolve(false, true, false, true))
    }

    @Test fun noForegroundSourceIsUnprotected() {
        assertEquals(ProtectionMode.UNPROTECTED, ProtectionModeResolver.resolve(false, false, true, true))
    }
}
