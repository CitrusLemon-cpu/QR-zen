package com.qrzen.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Logs each time a block fires or a pause is applied.
 * Used by StatsFragment and widgets to show usage data.
 */
@Entity(tableName = "block_events")
data class BlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockId: Int,
    val blockTitle: String,
    val packageName: String,
    /** "BLOCKED" when the lock screen is shown, "PAUSED" when the user scans QR/enters password */
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis()
)
