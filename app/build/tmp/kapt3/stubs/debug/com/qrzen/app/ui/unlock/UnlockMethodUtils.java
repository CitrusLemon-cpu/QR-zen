package com.qrzen.app.ui.unlock;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u00016B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0015\u001a\u00020\u0016J\u000e\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u0016J\u000e\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u0004J\u000e\u0010\u001b\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u001dJ\b\u0010\u001e\u001a\u00020\u0004H\u0002J\u0018\u0010\u001f\u001a\u00020 2\u0006\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010!\u001a\u00020\"J\u000e\u0010#\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u001dJ\u001a\u0010$\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u001d2\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0004J\"\u0010&\u001a\u0004\u0018\u00010\u00042\u0006\u0010\'\u001a\u00020(2\u0006\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010)\u001a\u00020\u0016J\u001c\u0010*\u001a\u00020+2\u0006\u0010\u001c\u001a\u00020\u001d2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020-0\u0011J\u0018\u0010.\u001a\u00020+2\u0006\u0010\u001a\u001a\u00020\u00042\u0006\u0010/\u001a\u000200H\u0002J\u0018\u00101\u001a\u00020+2\u0006\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010)\u001a\u00020\u0016J\u0018\u00102\u001a\u0002032\u0006\u00104\u001a\u00020\u00042\u0006\u00105\u001a\u000203H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/qrzen/app/ui/unlock/UnlockMethodUtils;", "", "()V", "METHOD_DELAY", "", "METHOD_EDIT_WINDOW", "METHOD_NONE", "METHOD_PASSWORD", "METHOD_QR_CODE", "METHOD_TIMER", "METHOD_TYPE_OVER_TEXT", "METHOD_WHILE_ACTIVE", "STYLE_MANUAL", "STYLE_SCHEDULE", "STYLE_USAGE_LIMIT", "STYLE_WAIT_TIMER", "challengeWords", "", "displayDateTimeFormatter", "Ljava/text/SimpleDateFormat;", "formatCountdown", "millis", "", "formatDateTime", "epochMillis", "formatDays", "days", "formatWindowSchedule", "block", "Lcom/qrzen/app/data/model/AppBlock;", "generateRandomTypeOverText", "getEditWindowAvailability", "Lcom/qrzen/app/ui/unlock/UnlockMethodUtils$EditWindowAvailability;", "now", "Ljava/time/LocalDateTime;", "getNormalizedMethod", "getTypeOverChallengeText", "existingText", "getUnlockMethodSummary", "context", "Landroid/content/Context;", "nowMillis", "isBlockCurrentlyActive", "", "timeBlocks", "Lcom/qrzen/app/data/model/TimeBlock;", "isDayActive", "dayOfWeek", "Ljava/time/DayOfWeek;", "isTimerExpired", "parseTime", "Ljava/time/LocalTime;", "value", "fallback", "EditWindowAvailability", "app_debug"})
public final class UnlockMethodUtils {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_NONE = "NONE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_DELAY = "DELAY";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_PASSWORD = "PASSWORD";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_TYPE_OVER_TEXT = "TYPE_OVER_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_QR_CODE = "QR_CODE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_EDIT_WINDOW = "EDIT_WINDOW";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_TIMER = "TIMER";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METHOD_WHILE_ACTIVE = "WHILE_ACTIVE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String STYLE_MANUAL = "MANUAL";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String STYLE_SCHEDULE = "SCHEDULE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String STYLE_USAGE_LIMIT = "USAGE_LIMIT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String STYLE_WAIT_TIMER = "WAIT_TIMER";
    @org.jetbrains.annotations.NotNull()
    private static final java.text.SimpleDateFormat displayDateTimeFormatter = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> challengeWords = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.unlock.UnlockMethodUtils INSTANCE = null;
    
    private UnlockMethodUtils() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNormalizedMethod(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block) {
        return null;
    }
    
    public final boolean isTimerExpired(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, long nowMillis) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.ui.unlock.UnlockMethodUtils.EditWindowAvailability getEditWindowAvailability(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    java.time.LocalDateTime now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatWindowSchedule(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatDays(@org.jetbrains.annotations.NotNull()
    java.lang.String days) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatDateTime(long epochMillis) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatCountdown(long millis) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTypeOverChallengeText(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.Nullable()
    java.lang.String existingText) {
        return null;
    }
    
    public final boolean isBlockCurrentlyActive(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    java.util.List<com.qrzen.app.data.model.TimeBlock> timeBlocks) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUnlockMethodSummary(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, long nowMillis) {
        return null;
    }
    
    private final java.lang.String generateRandomTypeOverText() {
        return null;
    }
    
    private final boolean isDayActive(java.lang.String days, java.time.DayOfWeek dayOfWeek) {
        return false;
    }
    
    private final java.time.LocalTime parseTime(java.lang.String value, java.time.LocalTime fallback) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\tJ$\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010\u000eJ\u0013\u0010\u000f\u001a\u00020\u00032\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0007R\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\n\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0015"}, d2 = {"Lcom/qrzen/app/ui/unlock/UnlockMethodUtils$EditWindowAvailability;", "", "isAvailable", "", "nextAvailableMillis", "", "(ZLjava/lang/Long;)V", "()Z", "getNextAvailableMillis", "()Ljava/lang/Long;", "Ljava/lang/Long;", "component1", "component2", "copy", "(ZLjava/lang/Long;)Lcom/qrzen/app/ui/unlock/UnlockMethodUtils$EditWindowAvailability;", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class EditWindowAvailability {
        private final boolean isAvailable = false;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Long nextAvailableMillis = null;
        
        public EditWindowAvailability(boolean isAvailable, @org.jetbrains.annotations.Nullable()
        java.lang.Long nextAvailableMillis) {
            super();
        }
        
        public final boolean isAvailable() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long getNextAvailableMillis() {
            return null;
        }
        
        public final boolean component1() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.ui.unlock.UnlockMethodUtils.EditWindowAvailability copy(boolean isAvailable, @org.jetbrains.annotations.Nullable()
        java.lang.Long nextAvailableMillis) {
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
}