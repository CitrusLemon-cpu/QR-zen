package com.qrzen.app.service

/** Pure selection logic for the shared timer overlay. */
object TimerOverlayResolver {
    data class Candidate(
        val blockId: Int,
        val label: String,
        val remainingMs: Long,
        val shouldBeVisible: Boolean
    )

    data class Entry(val blockId: Int, val label: String, val remainingMs: Long)

    fun resolve(candidates: List<Candidate>): List<Entry> = candidates
        .asSequence()
        .filter { it.shouldBeVisible && it.remainingMs > 0L }
        .map { Entry(it.blockId, it.label, it.remainingMs) }
        .toList()
}
