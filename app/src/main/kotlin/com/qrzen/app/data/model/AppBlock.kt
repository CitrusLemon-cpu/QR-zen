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
    /** Comma-separated package names of blocked or allowed apps */
    val appPackages: String = "",
    /** When true, appPackages lists allowed apps; everything else is blocked */
    val isAllowlistMode: Boolean = false,
    /** Active time window start in "HH:mm" format */
    val startTime: String = "00:00",
    /** Active time window end in "HH:mm" format */
    val endTime: String = "23:59",
    /**
     * Active days as 7-char binary string, index 0 = Monday.
     * "1111111" = every day, "1111100" = Mon–Fri only.
     */
    val activeDays: String = "1111111",
    /**
     * UUID secret encoded in the physical/digital QR code for this block.
     * Generated once when the block is created, never changes.
     * Scanning the correct QR presents the pause-duration picker.
     */
    val qrSecret: String = "",
    /** Which unlock method is configured. Default NONE = freely editable. */
    val unlockMethod: String = "NONE",
    /** Delay method: how many minutes the user must wait */
    val delayMinutes: Int = 5,
    /** Password method: per-block password */
    val blockPassword: String = "",
    /** Type-over text method: the challenge text (custom or template for random) */
    val typeOverText: String = "",
    /** Type-over text method: if true, generate random text each time instead of using typeOverText */
    val typeOverIsRandom: Boolean = false,
    /** Edit window method: start time in "HH:mm" format */
    val editWindowStart: String = "09:00",
    /** Edit window method: end time in "HH:mm" format */
    val editWindowEnd: String = "10:00",
    /** Edit window method: active days as 7-char binary string (same format as activeDays) */
    val editWindowDays: String = "1111111",
    /** Timer method: epoch millis until which editing is locked */
    val lockUntil: Long = 0L,
    /** Whether the master-password fallback is enabled for this specific block */
    val masterPasswordEnabled: Boolean = false,
    /**
     * Epoch millis until which this block is paused.
     * 0 = not paused. Long.MAX_VALUE = paused indefinitely (until reboot/restart).
     */
    val pausedUntil: Long = 0L,
    /** Epoch millis until which this block is forced active regardless of schedule. 0 = not forced. */
    val blockNowUntil: Long = 0L,
    /** Whether this block rule is active */
    val isEnabled: Boolean = true,
    /** Pomodoro: treat this block as a Pomodoro focus timer */
    val isPomodoroBlock: Boolean = false,
    val pomodoroDurationMin: Int = 25,
    val pomodoroBreakMin: Int = 5,
    /** Whether this block is archived (hidden from main list) */
    val isArchived: Boolean = false,
    val blockingStyle: String = "MANUAL",
    val usageLimitMinutes: Int = 30,
    val usageLimitPeriod: String = "DAILY",
    val waitTimerWaitMinutes: Int = 30,
    val waitTimerUseMinutes: Int = 5,
    val timerBreakMinutes: Int = 0
)
