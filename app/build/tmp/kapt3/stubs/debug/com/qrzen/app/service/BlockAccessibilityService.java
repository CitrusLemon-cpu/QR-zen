package com.qrzen.app.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 .2\u00020\u0001:\u0001.B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u001dH\u0002J\u0010\u0010!\u001a\u00020\u001d2\u0006\u0010\"\u001a\u00020\u0005H\u0002J\u0018\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0018\u0010&\u001a\u00020$2\u0006\u0010%\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0018\u0010\'\u001a\u00020$2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010%\u001a\u00020\u0005H\u0002J\u0012\u0010(\u001a\u00020$2\b\u0010)\u001a\u0004\u0018\u00010*H\u0016J\b\u0010+\u001a\u00020$H\u0016J\b\u0010,\u001a\u00020$H\u0016J\b\u0010-\u001a\u00020$H\u0014R!\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\t\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n \u0013*\u0004\u0018\u00010\u00120\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R!\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\t\u001a\u0004\b\u0015\u0010\u0007R!\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\t\u001a\u0004\b\u0018\u0010\u0007R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/qrzen/app/service/BlockAccessibilityService;", "Landroid/accessibilityservice/AccessibilityService;", "()V", "dialerPackages", "", "", "getDialerPackages", "()Ljava/util/Set;", "dialerPackages$delegate", "Lkotlin/Lazy;", "entryPoint", "Lcom/qrzen/app/di/WidgetEntryPoint;", "getEntryPoint", "()Lcom/qrzen/app/di/WidgetEntryPoint;", "entryPoint$delegate", "exceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "fmt", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "imePackages", "getImePackages", "imePackages$delegate", "launcherPackages", "getLauncherPackages", "launcherPackages$delegate", "scope", "Lkotlinx/coroutines/CoroutineScope;", "isBlockActive", "", "block", "Lcom/qrzen/app/data/model/AppBlock;", "isDeviceLocked", "isExemptFromAllowlist", "pkg", "launchAllowlistOverlay", "", "blockedPkg", "launchLockScreen", "logBlockEvent", "onAccessibilityEvent", "event", "Landroid/view/accessibility/AccessibilityEvent;", "onDestroy", "onInterrupt", "onServiceConnected", "Companion", "app_debug"})
public final class BlockAccessibilityService extends android.accessibilityservice.AccessibilityService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "QrZenAccessibility";
    @kotlin.jvm.Volatile()
    private static volatile boolean isRunning = false;
    
    /**
     * System packages that must never be blocked by allowlist mode.
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> SYSTEM_EXEMPT_PACKAGES = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineExceptionHandler exceptionHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    private final java.time.format.DateTimeFormatter fmt = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy entryPoint$delegate = null;
    
    /**
     * All packages that declare themselves as launchers (ACTION_MAIN + CATEGORY_HOME).
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy launcherPackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy dialerPackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy imePackages$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.service.BlockAccessibilityService.Companion Companion = null;
    
    public BlockAccessibilityService() {
        super();
    }
    
    private final com.qrzen.app.di.WidgetEntryPoint getEntryPoint() {
        return null;
    }
    
    /**
     * All packages that declare themselves as launchers (ACTION_MAIN + CATEGORY_HOME).
     */
    private final java.util.Set<java.lang.String> getLauncherPackages() {
        return null;
    }
    
    private final java.util.Set<java.lang.String> getDialerPackages() {
        return null;
    }
    
    private final java.util.Set<java.lang.String> getImePackages() {
        return null;
    }
    
    private final boolean isExemptFromAllowlist(java.lang.String pkg) {
        return false;
    }
    
    private final boolean isDeviceLocked() {
        return false;
    }
    
    @java.lang.Override()
    protected void onServiceConnected() {
    }
    
    @java.lang.Override()
    public void onAccessibilityEvent(@org.jetbrains.annotations.Nullable()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final boolean isBlockActive(com.qrzen.app.data.model.AppBlock block) {
        return false;
    }
    
    private final void launchLockScreen(java.lang.String blockedPkg, com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void launchAllowlistOverlay(java.lang.String blockedPkg, com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void logBlockEvent(com.qrzen.app.data.model.AppBlock block, java.lang.String blockedPkg) {
    }
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\t\"\u0004\b\n\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/qrzen/app/service/BlockAccessibilityService$Companion;", "", "()V", "SYSTEM_EXEMPT_PACKAGES", "", "", "TAG", "isRunning", "", "()Z", "setRunning", "(Z)V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isRunning() {
            return false;
        }
        
        public final void setRunning(boolean p0) {
        }
    }
}