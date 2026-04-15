package com.qrzen.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_events")
data class BlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockId: Int,
    val blockTitle: String,
    val packageName: String,
    val eventType: String, // "BLOCKED" or "PAUSED"
    val timestamp: Long = System.currentTimeMillis()
)
