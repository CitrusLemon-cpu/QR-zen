package com.qrzen.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core entity representing an app-blocking rule.
 *
 * QR unlock: each block has a [qrSecret] UUID. The user exports/prints a QR code
 * encoding this secret. When the block fires, the user scans that QR code to pause it.
 */
@Entity(tableName = "app_blocks")
data class AppBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** User-facing name for this block (e.g. "Social Media") */
    val title: String = "",
    /** Comma-separated package names of blocked apps */
    val appPackages: String = "",
    /** Active time window start in "HH:mm" format */
    val startTime: String = "00:00",
    /** Active time window end in "HH:mm" format */
    val endTime: String = "23:59",
    /**
     * Active days as 7-char binary string, index 0 = Monday.
     * "1111111" = every day, "1111100" = Mon–Fri only.
     */
    val activeDays: String = "1111111",
    /** Edit window: time when user is allowed to modify this block's settings */
    val editStartTime: String = "06:00",
    val editEndTime: String = "07:00",
    /**
     * UUID secret encoded in the physical/digital QR code for this block.
     * Generated once when the block is created, never changes.
     * Scanning the correct QR presents the pause-duration picker.
     */
    val qrSecret: String = "",
    /** Whether the master-password fallback is enabled for this specific block */
    val masterPasswordEnabled: Boolean = false,
    /**
     * Epoch millis until which this block is paused.
     * 0 = not paused. Long.MAX_VALUE = paused indefinitely (until reboot/restart).
     */
    val pausedUntil: Long = 0L,
    /** Whether this block rule is active */
    val isEnabled: Boolean = true,
    /** Pomodoro: treat this block as a Pomodoro focus timer */
    val isPomodoroBlock: Boolean = false,
    val pomodoroDurationMin: Int = 25,
    val pomodoroBreakMin: Int = 5
)
