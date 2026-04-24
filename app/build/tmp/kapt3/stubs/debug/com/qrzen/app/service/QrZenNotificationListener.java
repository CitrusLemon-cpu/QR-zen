package com.qrzen.app.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00182\u00020\u0001:\u0001\u0018B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\u0012\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016R\u001a\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n \u000e*\u0004\u0018\u00010\r0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/qrzen/app/service/QrZenNotificationListener;", "Landroid/service/notification/NotificationListenerService;", "()V", "blockedPackages", "", "", "getBlockedPackages", "()Ljava/util/Set;", "cachedAllowedPackages", "cachedHasAllowlist", "", "cachedPackages", "fmt", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "lastFetchTime", "", "isBlockActive", "block", "Lcom/qrzen/app/data/model/AppBlock;", "onNotificationPosted", "", "sbn", "Landroid/service/notification/StatusBarNotification;", "Companion", "app_debug"})
public final class QrZenNotificationListener extends android.service.notification.NotificationListenerService {
    @org.jetbrains.annotations.NotNull()
    private java.util.Set<java.lang.String> cachedPackages;
    @org.jetbrains.annotations.NotNull()
    private java.util.Set<java.lang.String> cachedAllowedPackages;
    private boolean cachedHasAllowlist = false;
    private long lastFetchTime = 0L;
    private final java.time.format.DateTimeFormatter fmt = null;
    private static final long CACHE_TTL_MS = 5000L;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> SYSTEM_EXEMPT_PACKAGES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.service.QrZenNotificationListener.Companion Companion = null;
    
    public QrZenNotificationListener() {
        super();
    }
    
    private final java.util.Set<java.lang.String> getBlockedPackages() {
        return null;
    }
    
    @java.lang.Override()
    public void onNotificationPosted(@org.jetbrains.annotations.Nullable()
    android.service.notification.StatusBarNotification sbn) {
    }
    
    private final boolean isBlockActive(com.qrzen.app.data.model.AppBlock block) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/qrzen/app/service/QrZenNotificationListener$Companion;", "", "()V", "CACHE_TTL_MS", "", "SYSTEM_EXEMPT_PACKAGES", "", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}