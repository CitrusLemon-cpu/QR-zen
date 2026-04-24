package com.qrzen.app.data.model;

/**
 * Core entity representing an app-blocking rule.
 *
 * QR unlock: each block has a [qrSecret] UUID. The user exports/prints a QR code
 * encoding this secret. When the block fires, the user scans that QR code to pause it.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010\t\n\u0002\bS\b\u0087\b\u0018\u00002\u00020\u0001B\u00bb\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\r\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0011\u001a\u00020\b\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\b\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u001a\u001a\u00020\b\u0012\b\b\u0002\u0010\u001b\u001a\u00020\b\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001e\u001a\u00020\b\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u0005\u0012\b\b\u0002\u0010 \u001a\u00020\u0003\u0012\b\b\u0002\u0010!\u001a\u00020\u0005\u0012\b\b\u0002\u0010\"\u001a\u00020\u0003\u0012\b\b\u0002\u0010#\u001a\u00020\u0003\u0012\b\b\u0002\u0010$\u001a\u00020\u0003\u00a2\u0006\u0002\u0010%J\t\u0010E\u001a\u00020\u0003H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u0005H\u00c6\u0003J\t\u0010H\u001a\u00020\u0005H\u00c6\u0003J\t\u0010I\u001a\u00020\bH\u00c6\u0003J\t\u0010J\u001a\u00020\u0005H\u00c6\u0003J\t\u0010K\u001a\u00020\u0005H\u00c6\u0003J\t\u0010L\u001a\u00020\u0005H\u00c6\u0003J\t\u0010M\u001a\u00020\u0016H\u00c6\u0003J\t\u0010N\u001a\u00020\bH\u00c6\u0003J\t\u0010O\u001a\u00020\u0016H\u00c6\u0003J\t\u0010P\u001a\u00020\u0005H\u00c6\u0003J\t\u0010Q\u001a\u00020\u0016H\u00c6\u0003J\t\u0010R\u001a\u00020\bH\u00c6\u0003J\t\u0010S\u001a\u00020\bH\u00c6\u0003J\t\u0010T\u001a\u00020\u0003H\u00c6\u0003J\t\u0010U\u001a\u00020\u0003H\u00c6\u0003J\t\u0010V\u001a\u00020\bH\u00c6\u0003J\t\u0010W\u001a\u00020\u0005H\u00c6\u0003J\t\u0010X\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Y\u001a\u00020\u0005H\u00c6\u0003J\t\u0010Z\u001a\u00020\u0003H\u00c6\u0003J\t\u0010[\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\\\u001a\u00020\u0003H\u00c6\u0003J\t\u0010]\u001a\u00020\u0003H\u00c6\u0003J\t\u0010^\u001a\u00020\bH\u00c6\u0003J\t\u0010_\u001a\u00020\u0005H\u00c6\u0003J\t\u0010`\u001a\u00020\u0005H\u00c6\u0003J\t\u0010a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010c\u001a\u00020\u0005H\u00c6\u0003J\u00bf\u0002\u0010d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u00052\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u00052\b\b\u0002\u0010\u0010\u001a\u00020\u00052\b\b\u0002\u0010\u0011\u001a\u00020\b2\b\b\u0002\u0010\u0012\u001a\u00020\u00052\b\b\u0002\u0010\u0013\u001a\u00020\u00052\b\b\u0002\u0010\u0014\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\b2\b\b\u0002\u0010\u0018\u001a\u00020\u00162\b\b\u0002\u0010\u0019\u001a\u00020\u00162\b\b\u0002\u0010\u001a\u001a\u00020\b2\b\b\u0002\u0010\u001b\u001a\u00020\b2\b\b\u0002\u0010\u001c\u001a\u00020\u00032\b\b\u0002\u0010\u001d\u001a\u00020\u00032\b\b\u0002\u0010\u001e\u001a\u00020\b2\b\b\u0002\u0010\u001f\u001a\u00020\u00052\b\b\u0002\u0010 \u001a\u00020\u00032\b\b\u0002\u0010!\u001a\u00020\u00052\b\b\u0002\u0010\"\u001a\u00020\u00032\b\b\u0002\u0010#\u001a\u00020\u00032\b\b\u0002\u0010$\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010e\u001a\u00020\b2\b\u0010f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010g\u001a\u00020\u0003H\u00d6\u0001J\t\u0010h\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\'R\u0011\u0010\u0019\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0011\u0010\u000f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\'R\u0011\u0010\u001f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\'R\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\u0014\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\'R\u0011\u0010\u0013\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\'R\u0011\u0010\u0012\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\'R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\'R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010.R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u00104R\u0011\u0010\u001e\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u00104R\u0011\u0010\u001a\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u00104R\u0011\u0010\u001b\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u00104R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010*R\u0011\u0010\u0017\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00104R\u0011\u0010\u0018\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010*R\u0011\u0010\u001d\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010.R\u0011\u0010\u001c\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010.R\u0011\u0010\f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010\'R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010\'R\u0011\u0010$\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010.R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010\'R\u0011\u0010\u0011\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u00104R\u0011\u0010\u0010\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010\'R\u0011\u0010\r\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010\'R\u0011\u0010 \u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010.R\u0011\u0010!\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010\'R\u0011\u0010#\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010.R\u0011\u0010\"\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010.\u00a8\u0006i"}, d2 = {"Lcom/qrzen/app/data/model/AppBlock;", "", "id", "", "title", "", "appPackages", "isAllowlistMode", "", "startTime", "endTime", "activeDays", "qrSecret", "unlockMethod", "delayMinutes", "blockPassword", "typeOverText", "typeOverIsRandom", "editWindowStart", "editWindowEnd", "editWindowDays", "lockUntil", "", "masterPasswordEnabled", "pausedUntil", "blockNowUntil", "isEnabled", "isPomodoroBlock", "pomodoroDurationMin", "pomodoroBreakMin", "isArchived", "blockingStyle", "usageLimitMinutes", "usageLimitPeriod", "waitTimerWaitMinutes", "waitTimerUseMinutes", "timerBreakMinutes", "(ILjava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;JZJJZZIIZLjava/lang/String;ILjava/lang/String;III)V", "getActiveDays", "()Ljava/lang/String;", "getAppPackages", "getBlockNowUntil", "()J", "getBlockPassword", "getBlockingStyle", "getDelayMinutes", "()I", "getEditWindowDays", "getEditWindowEnd", "getEditWindowStart", "getEndTime", "getId", "()Z", "getLockUntil", "getMasterPasswordEnabled", "getPausedUntil", "getPomodoroBreakMin", "getPomodoroDurationMin", "getQrSecret", "getStartTime", "getTimerBreakMinutes", "getTitle", "getTypeOverIsRandom", "getTypeOverText", "getUnlockMethod", "getUsageLimitMinutes", "getUsageLimitPeriod", "getWaitTimerUseMinutes", "getWaitTimerWaitMinutes", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "app_blocks")
public final class AppBlock {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final int id = 0;
    
    /**
     * User-facing name for this block (e.g. "Social Media")
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    
    /**
     * Comma-separated package names of blocked or allowed apps
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String appPackages = null;
    
    /**
     * When true, appPackages lists allowed apps; everything else is blocked
     */
    private final boolean isAllowlistMode = false;
    
    /**
     * Active time window start in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String startTime = null;
    
    /**
     * Active time window end in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String endTime = null;
    
    /**
     * Active days as 7-char binary string, index 0 = Monday.
     * "1111111" = every day, "1111100" = Mon–Fri only.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String activeDays = null;
    
    /**
     * UUID secret encoded in the physical/digital QR code for this block.
     * Generated once when the block is created, never changes.
     * Scanning the correct QR presents the pause-duration picker.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String qrSecret = null;
    
    /**
     * Which unlock method is configured. Default NONE = freely editable.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String unlockMethod = null;
    
    /**
     * Delay method: how many minutes the user must wait
     */
    private final int delayMinutes = 0;
    
    /**
     * Password method: per-block password
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String blockPassword = null;
    
    /**
     * Type-over text method: the challenge text (custom or template for random)
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String typeOverText = null;
    
    /**
     * Type-over text method: if true, generate random text each time instead of using typeOverText
     */
    private final boolean typeOverIsRandom = false;
    
    /**
     * Edit window method: start time in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String editWindowStart = null;
    
    /**
     * Edit window method: end time in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String editWindowEnd = null;
    
    /**
     * Edit window method: active days as 7-char binary string (same format as activeDays)
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String editWindowDays = null;
    
    /**
     * Timer method: epoch millis until which editing is locked
     */
    private final long lockUntil = 0L;
    
    /**
     * Whether the master-password fallback is enabled for this specific block
     */
    private final boolean masterPasswordEnabled = false;
    
    /**
     * Epoch millis until which this block is paused.
     * 0 = not paused. Long.MAX_VALUE = paused indefinitely (until reboot/restart).
     */
    private final long pausedUntil = 0L;
    
    /**
     * Epoch millis until which this block is forced active regardless of schedule. 0 = not forced.
     */
    private final long blockNowUntil = 0L;
    
    /**
     * Whether this block rule is active
     */
    private final boolean isEnabled = false;
    
    /**
     * Pomodoro: treat this block as a Pomodoro focus timer
     */
    private final boolean isPomodoroBlock = false;
    private final int pomodoroDurationMin = 0;
    private final int pomodoroBreakMin = 0;
    
    /**
     * Whether this block is archived (hidden from main list)
     */
    private final boolean isArchived = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String blockingStyle = null;
    private final int usageLimitMinutes = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String usageLimitPeriod = null;
    private final int waitTimerWaitMinutes = 0;
    private final int waitTimerUseMinutes = 0;
    private final int timerBreakMinutes = 0;
    
    public AppBlock(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String appPackages, boolean isAllowlistMode, @org.jetbrains.annotations.NotNull()
    java.lang.String startTime, @org.jetbrains.annotations.NotNull()
    java.lang.String endTime, @org.jetbrains.annotations.NotNull()
    java.lang.String activeDays, @org.jetbrains.annotations.NotNull()
    java.lang.String qrSecret, @org.jetbrains.annotations.NotNull()
    java.lang.String unlockMethod, int delayMinutes, @org.jetbrains.annotations.NotNull()
    java.lang.String blockPassword, @org.jetbrains.annotations.NotNull()
    java.lang.String typeOverText, boolean typeOverIsRandom, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowStart, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowEnd, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowDays, long lockUntil, boolean masterPasswordEnabled, long pausedUntil, long blockNowUntil, boolean isEnabled, boolean isPomodoroBlock, int pomodoroDurationMin, int pomodoroBreakMin, boolean isArchived, @org.jetbrains.annotations.NotNull()
    java.lang.String blockingStyle, int usageLimitMinutes, @org.jetbrains.annotations.NotNull()
    java.lang.String usageLimitPeriod, int waitTimerWaitMinutes, int waitTimerUseMinutes, int timerBreakMinutes) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    /**
     * User-facing name for this block (e.g. "Social Media")
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    /**
     * Comma-separated package names of blocked or allowed apps
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAppPackages() {
        return null;
    }
    
    /**
     * When true, appPackages lists allowed apps; everything else is blocked
     */
    public final boolean isAllowlistMode() {
        return false;
    }
    
    /**
     * Active time window start in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStartTime() {
        return null;
    }
    
    /**
     * Active time window end in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEndTime() {
        return null;
    }
    
    /**
     * Active days as 7-char binary string, index 0 = Monday.
     * "1111111" = every day, "1111100" = Mon–Fri only.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getActiveDays() {
        return null;
    }
    
    /**
     * UUID secret encoded in the physical/digital QR code for this block.
     * Generated once when the block is created, never changes.
     * Scanning the correct QR presents the pause-duration picker.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getQrSecret() {
        return null;
    }
    
    /**
     * Which unlock method is configured. Default NONE = freely editable.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUnlockMethod() {
        return null;
    }
    
    /**
     * Delay method: how many minutes the user must wait
     */
    public final int getDelayMinutes() {
        return 0;
    }
    
    /**
     * Password method: per-block password
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBlockPassword() {
        return null;
    }
    
    /**
     * Type-over text method: the challenge text (custom or template for random)
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTypeOverText() {
        return null;
    }
    
    /**
     * Type-over text method: if true, generate random text each time instead of using typeOverText
     */
    public final boolean getTypeOverIsRandom() {
        return false;
    }
    
    /**
     * Edit window method: start time in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEditWindowStart() {
        return null;
    }
    
    /**
     * Edit window method: end time in "HH:mm" format
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEditWindowEnd() {
        return null;
    }
    
    /**
     * Edit window method: active days as 7-char binary string (same format as activeDays)
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEditWindowDays() {
        return null;
    }
    
    /**
     * Timer method: epoch millis until which editing is locked
     */
    public final long getLockUntil() {
        return 0L;
    }
    
    /**
     * Whether the master-password fallback is enabled for this specific block
     */
    public final boolean getMasterPasswordEnabled() {
        return false;
    }
    
    /**
     * Epoch millis until which this block is paused.
     * 0 = not paused. Long.MAX_VALUE = paused indefinitely (until reboot/restart).
     */
    public final long getPausedUntil() {
        return 0L;
    }
    
    /**
     * Epoch millis until which this block is forced active regardless of schedule. 0 = not forced.
     */
    public final long getBlockNowUntil() {
        return 0L;
    }
    
    /**
     * Whether this block rule is active
     */
    public final boolean isEnabled() {
        return false;
    }
    
    /**
     * Pomodoro: treat this block as a Pomodoro focus timer
     */
    public final boolean isPomodoroBlock() {
        return false;
    }
    
    public final int getPomodoroDurationMin() {
        return 0;
    }
    
    public final int getPomodoroBreakMin() {
        return 0;
    }
    
    /**
     * Whether this block is archived (hidden from main list)
     */
    public final boolean isArchived() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBlockingStyle() {
        return null;
    }
    
    public final int getUsageLimitMinutes() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsageLimitPeriod() {
        return null;
    }
    
    public final int getWaitTimerWaitMinutes() {
        return 0;
    }
    
    public final int getWaitTimerUseMinutes() {
        return 0;
    }
    
    public final int getTimerBreakMinutes() {
        return 0;
    }
    
    public AppBlock() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int component10() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component12() {
        return null;
    }
    
    public final boolean component13() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component16() {
        return null;
    }
    
    public final long component17() {
        return 0L;
    }
    
    public final boolean component18() {
        return false;
    }
    
    public final long component19() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final long component20() {
        return 0L;
    }
    
    public final boolean component21() {
        return false;
    }
    
    public final boolean component22() {
        return false;
    }
    
    public final int component23() {
        return 0;
    }
    
    public final int component24() {
        return 0;
    }
    
    public final boolean component25() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component26() {
        return null;
    }
    
    public final int component27() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component28() {
        return null;
    }
    
    public final int component29() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component30() {
        return 0;
    }
    
    public final int component31() {
        return 0;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.model.AppBlock copy(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String appPackages, boolean isAllowlistMode, @org.jetbrains.annotations.NotNull()
    java.lang.String startTime, @org.jetbrains.annotations.NotNull()
    java.lang.String endTime, @org.jetbrains.annotations.NotNull()
    java.lang.String activeDays, @org.jetbrains.annotations.NotNull()
    java.lang.String qrSecret, @org.jetbrains.annotations.NotNull()
    java.lang.String unlockMethod, int delayMinutes, @org.jetbrains.annotations.NotNull()
    java.lang.String blockPassword, @org.jetbrains.annotations.NotNull()
    java.lang.String typeOverText, boolean typeOverIsRandom, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowStart, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowEnd, @org.jetbrains.annotations.NotNull()
    java.lang.String editWindowDays, long lockUntil, boolean masterPasswordEnabled, long pausedUntil, long blockNowUntil, boolean isEnabled, boolean isPomodoroBlock, int pomodoroDurationMin, int pomodoroBreakMin, boolean isArchived, @org.jetbrains.annotations.NotNull()
    java.lang.String blockingStyle, int usageLimitMinutes, @org.jetbrains.annotations.NotNull()
    java.lang.String usageLimitPeriod, int waitTimerWaitMinutes, int waitTimerUseMinutes, int timerBreakMinutes) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}