package com.qrzen.app.service

/** Distinguishes an exempt Home surface from Recents hosted by the same launcher package. */
object ForegroundSurfacePolicy {
    fun isExemptFromBlocking(
        normallyExempt: Boolean,
        confirmedRecents: Boolean
    ): Boolean = normallyExempt && !confirmedRecents
}
