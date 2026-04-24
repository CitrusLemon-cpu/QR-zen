package com.qrzen.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_blocks",
    foreignKeys = [ForeignKey(
        entity = AppBlock::class,
        parentColumns = ["id"],
        childColumns = ["blockId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("blockId")]
)
data class TimeBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockId: Int,
    val startTime: String,
    val endTime: String,
    val activeDays: String
)
