package com.qrzen.app.service;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010#\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u0000 F2\u00020\u0001:\u0001FB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010*\u001a\u00020+H\u0002J\b\u0010,\u001a\u00020-H\u0002J\u000e\u0010.\u001a\u00020+H\u0082@\u00a2\u0006\u0002\u0010/J\u000e\u00100\u001a\u00020+H\u0082@\u00a2\u0006\u0002\u0010/J\n\u00101\u001a\u0004\u0018\u00010\rH\u0002J\u0010\u00102\u001a\u00020&2\u0006\u00103\u001a\u000204H\u0002J\b\u00105\u001a\u00020&H\u0002J\u0010\u00106\u001a\u00020&2\u0006\u00107\u001a\u00020\rH\u0002J\u0018\u00108\u001a\u00020+2\u0006\u00109\u001a\u00020\r2\u0006\u00103\u001a\u000204H\u0002J\u0018\u0010:\u001a\u00020+2\u0006\u00109\u001a\u00020\r2\u0006\u00103\u001a\u000204H\u0002J\u0014\u0010;\u001a\u0004\u0018\u00010<2\b\u0010=\u001a\u0004\u0018\u00010>H\u0016J\b\u0010?\u001a\u00020+H\u0016J\"\u0010@\u001a\u00020\u001f2\b\u0010=\u001a\u0004\u0018\u00010>2\u0006\u0010A\u001a\u00020\u001f2\u0006\u0010B\u001a\u00020\u001fH\u0016J\b\u0010C\u001a\u00020+H\u0002J\b\u0010D\u001a\u00020+H\u0002J\b\u0010E\u001a\u00020+H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR!\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R!\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\u0011\u001a\u0004\b\u0015\u0010\u000fR\u0010\u0010\u0017\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R!\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\u0011\u001a\u0004\b\u001b\u0010\u000fR\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020!X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\'\u001a\b\u0018\u00010(R\u00020)X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006G"}, d2 = {"Lcom/qrzen/app/service/BackgroundService;", "Landroid/app/Service;", "()V", "checkRunnable", "Ljava/lang/Runnable;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "dialerPackages", "", "", "getDialerPackages", "()Ljava/util/Set;", "dialerPackages$delegate", "Lkotlin/Lazy;", "handler", "Landroid/os/Handler;", "imePackages", "getImePackages", "imePackages$delegate", "lastBlockedPkg", "lastBlockedTime", "", "launcherPackages", "getLauncherPackages", "launcherPackages$delegate", "previouslyActiveBlockIds", "", "", "scope", "Lkotlinx/coroutines/CoroutineScope;", "systemExemptPackages", "usageCheckRunnable", "usageHandler", "usagePollingActive", "", "wakeLock", "Landroid/os/PowerManager$WakeLock;", "Landroid/os/PowerManager;", "acquireWakeLock", "", "buildNotification", "Landroid/app/Notification;", "checkExpiredPauses", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkForegroundApp", "getForegroundPackage", "isBlockActive", "block", "Lcom/qrzen/app/data/model/AppBlock;", "isDeviceLocked", "isExemptPackage", "pkg", "launchAllowlistOverlay", "blockedPkg", "launchLockScreen", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onDestroy", "onStartCommand", "flags", "startId", "sendToHome", "startUsagePolling", "stopUsagePolling", "Companion", "app_debug"})
public final class BackgroundService extends android.app.Service {
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler usageHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.Integer> previouslyActiveBlockIds = null;
    private boolean usagePollingActive = false;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastBlockedPkg;
    private long lastBlockedTime = 0L;
    @org.jetbrains.annotations.Nullable()
    private android.os.PowerManager.WakeLock wakeLock;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> systemExemptPackages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy launcherPackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy imePackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy dialerPackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable checkRunnable = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable usageCheckRunnable = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String NOTIF_CHANNEL_ID = "qrzen_bg";
    private static final int NOTIF_ID = 1001;
    private static final long CHECK_INTERVAL_MS = 60000L;
    private static final long USAGE_POLL_INTERVAL_MS = 2000L;
    private static final long BLOCK_COOLDOWN_MS = 3000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.service.BackgroundService.Companion Companion = null;
    
    public BackgroundService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppBlockDao getDao() {
        return null;
    }
    
    public final void setDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppBlockDao p0) {
    }
    
    private final java.util.Set<java.lang.String> getLauncherPackages() {
        return null;
    }
    
    private final java.util.Set<java.lang.String> getImePackages() {
        return null;
    }
    
    private final java.util.Set<java.lang.String> getDialerPackages() {
        return null;
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final java.lang.Object checkExpiredPauses(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final boolean isBlockActive(com.qrzen.app.data.model.AppBlock block) {
        return false;
    }
    
    private final android.app.Notification buildNotification() {
        return null;
    }
    
    private final boolean isExemptPackage(java.lang.String pkg) {
        return false;
    }
    
    private final boolean isDeviceLocked() {
        return false;
    }
    
    private final java.lang.String getForegroundPackage() {
        return null;
    }
    
    private final java.lang.Object checkForegroundApp(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void launchLockScreen(java.lang.String blockedPkg, com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void launchAllowlistOverlay(java.lang.String blockedPkg, com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void sendToHome() {
    }
    
    private final void startUsagePolling() {
    }
    
    private final void stopUsagePolling() {
    }
    
    private final void acquireWakeLock() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/qrzen/app/service/BackgroundService$Companion;", "", "()V", "BLOCK_COOLDOWN_MS", "", "CHECK_INTERVAL_MS", "NOTIF_CHANNEL_ID", "", "NOTIF_ID", "", "USAGE_POLL_INTERVAL_MS", "start", "", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void start(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}