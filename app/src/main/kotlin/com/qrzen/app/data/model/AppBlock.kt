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
    /** Break type for scheduled blocks. NONE means continuous blocking during schedule. */
    val scheduleBreakType: String = "NONE",
    /** Minutes of usage allowed per schedule window when scheduleBreakType = SCHEDULED_ALLOWANCE */
    val scheduledAllowanceMinutes: Int = 10,
    val usageLimitMinutes: Int = 30,
    val usageLimitPeriod: String = "DAILY",
    val waitTimerWaitMinutes: Int = 30,
    val waitTimerUseMinutes: Int = 5,
    val waitTimerAdaptive: Boolean = false,
    val timerBreakMinutes: Int = 0,
    val showTimer: Boolean = false,
    /** Epoch millis until which the toggle is locked (can't turn off). 0 = not locked. */
    val toggleLockUntil: Long = 0L,
    /** If true, auto-set isEnabled=false when toggleLockUntil expires */
    val autoDisableOnToggleLockExpiry: Boolean = false,
    /** Epoch millis until which this block auto-expires (for allowlist mandatory timer). 0 = no auto-expiry. */
    val activeUntil: Long = 0L,
    /** Total rounds for current pomodoro session. 0 = no active session. */
    val pomodoroRoundsTotal: Int = 0,
    /** Epoch millis when the current pomodoro session started. 0 = no session. */
    val pomodoroSessionStartMillis: Long = 0L,
    /** Default setting: lock editing during pomodoro sessions */
    val pomodoroLockEditing: Boolean = false,
    /** If true, newly installed apps are automatically added to this block's appPackages list */
    val autoAddNewApps: Boolean = false,
    val blockAudio: Boolean = false,
    @Deprecated("Replaced by global Prefs.masterPasswordOverrideMode")
    val ignoreMasterPassword: Boolean = false,
    /** ID of the folder this block belongs to, or null if at root level */
    val folderId: Int? = null
)
