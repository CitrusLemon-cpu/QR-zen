package com.qrzen.app.data.prefs;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010*\u001a\u00020+J\u000e\u0010,\u001a\u00020\u001e2\u0006\u0010-\u001a\u00020\u0004J\u000e\u0010.\u001a\u00020\u00152\u0006\u0010-\u001a\u00020\u0004J\u0016\u0010/\u001a\u00020+2\u0006\u0010-\u001a\u00020\u00042\u0006\u00100\u001a\u00020\u001eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\u00020\f8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000eR$\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00048F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u000f\u001a\u00020\u00158F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR$\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u000f\u001a\u00020\u00158F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001c\u0010\u0018\"\u0004\b\u001d\u0010\u001aR$\u0010\u001f\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u001e8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b \u0010!\"\u0004\b\"\u0010#R$\u0010$\u001a\u00020\u00152\u0006\u0010\u000f\u001a\u00020\u00158F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b%\u0010\u0018\"\u0004\b&\u0010\u001aR$\u0010\'\u001a\u00020\u00152\u0006\u0010\u000f\u001a\u00020\u00158F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b(\u0010\u0018\"\u0004\b)\u0010\u001a\u00a8\u00061"}, d2 = {"Lcom/qrzen/app/data/prefs/Prefs;", "", "()V", "KEY_APP_TIMER_PREFIX", "", "KEY_MASTER_PASSWORD", "KEY_MASTER_PWD_ENABLED", "KEY_ONBOARDING_DONE", "KEY_PAUSE_ALL_UNTIL", "KEY_REMOVE_NOTIF", "KEY_SILENT", "kv", "Lcom/tencent/mmkv/MMKV;", "getKv", "()Lcom/tencent/mmkv/MMKV;", "v", "masterPassword", "getMasterPassword", "()Ljava/lang/String;", "setMasterPassword", "(Ljava/lang/String;)V", "", "masterPasswordEnabled", "getMasterPasswordEnabled", "()Z", "setMasterPasswordEnabled", "(Z)V", "onboardingComplete", "getOnboardingComplete", "setOnboardingComplete", "", "pauseAllUntil", "getPauseAllUntil", "()J", "setPauseAllUntil", "(J)V", "removeNotifications", "getRemoveNotifications", "setRemoveNotifications", "silentMode", "getSilentMode", "setSilentMode", "clearAllAppTimers", "", "getAppTimerExpiry", "packageName", "isAppTimerExpired", "setAppTimerExpiry", "expiryMillis", "app_debug"})
public final class Prefs {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MASTER_PASSWORD = "qrzen_master_pwd";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MASTER_PWD_ENABLED = "qrzen_master_pwd_enabled";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PAUSE_ALL_UNTIL = "qrzen_pause_all_until";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ONBOARDING_DONE = "qrzen_onboarding_done";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_REMOVE_NOTIF = "qrzen_remove_notif";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SILENT = "qrzen_silent";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_APP_TIMER_PREFIX = "qrzen_app_timer_";
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.data.prefs.Prefs INSTANCE = null;
    
    private Prefs() {
        super();
    }
    
    private final com.tencent.mmkv.MMKV getKv() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMasterPassword() {
        return null;
    }
    
    public final void setMasterPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final boolean getMasterPasswordEnabled() {
        return false;
    }
    
    public final void setMasterPasswordEnabled(boolean v) {
    }
    
    public final long getPauseAllUntil() {
        return 0L;
    }
    
    public final void setPauseAllUntil(long v) {
    }
    
    public final boolean getOnboardingComplete() {
        return false;
    }
    
    public final void setOnboardingComplete(boolean v) {
    }
    
    public final boolean getRemoveNotifications() {
        return false;
    }
    
    public final void setRemoveNotifications(boolean v) {
    }
    
    public final boolean getSilentMode() {
        return false;
    }
    
    public final void setSilentMode(boolean v) {
    }
    
    public final long getAppTimerExpiry(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return 0L;
    }
    
    public final void setAppTimerExpiry(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName, long expiryMillis) {
    }
    
    public final boolean isAppTimerExpired(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return false;
    }
    
    public final void clearAllAppTimers() {
    }
}