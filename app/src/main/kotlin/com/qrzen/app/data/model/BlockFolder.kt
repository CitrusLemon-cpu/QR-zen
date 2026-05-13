package com.qrzen.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_folders")
data class BlockFolder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val isEnabled: Boolean = true,
    /** Same semantics as AppBlock.pausedUntil: 0=not paused, Long.MAX_VALUE=indefinite, timestamp=until */
    val pausedUntil: Long = 0L,
    /** UI state: whether folder is collapsed in the home list */
    val isCollapsed: Boolean = false,
    val sortOrder: Int = 0,
    val unlockMethod: String = "NONE",
    val delayMinutes: Int = 5,
    val blockPassword: String = "",
    val typeOverText: String = "",
    val typeOverIsRandom: Boolean = false,
    val editWindowStart: String = "09:00",
    val editWindowEnd: String = "10:00",
    val editWindowDays: String = "1111111",
    val lockUntil: Long = 0L,
    val qrSecret: String = "",
    val masterPasswordEnabled: Boolean = false,
    @Deprecated("Replaced by global Prefs.masterPasswordOverrideMode")
    val ignoreMasterPassword: Boolean = false
)
