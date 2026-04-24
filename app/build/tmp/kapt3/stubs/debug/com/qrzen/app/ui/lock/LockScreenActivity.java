package com.qrzen.app.ui.lock;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u0000 /2\u00020\u0001:\u0001/B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u001cH\u0002J\u0010\u0010!\u001a\u00020\u001c2\u0006\u0010\"\u001a\u00020#H\u0002J\b\u0010$\u001a\u00020\u001fH\u0002J\b\u0010%\u001a\u00020\u001cH\u0017J\u0012\u0010&\u001a\u00020\u001c2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0014J\b\u0010)\u001a\u00020\u001cH\u0014J\u0010\u0010*\u001a\u00020\u001c2\u0006\u0010+\u001a\u00020\u0017H\u0014J\u0010\u0010,\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\fH\u0002J\u0010\u0010-\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\fH\u0002J\u0010\u0010.\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\r\u001a\u00020\u000e8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0015\u001a\u0010\u0012\f\u0012\n \u0018*\u0004\u0018\u00010\u00170\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u00060"}, d2 = {"Lcom/qrzen/app/ui/lock/LockScreenActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/qrzen/app/databinding/ActivityLockScreenBinding;", "blockEventDao", "Lcom/qrzen/app/data/db/BlockEventDao;", "getBlockEventDao", "()Lcom/qrzen/app/data/db/BlockEventDao;", "setBlockEventDao", "(Lcom/qrzen/app/data/db/BlockEventDao;)V", "currentBlock", "Lcom/qrzen/app/data/model/AppBlock;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "pauseSheetShown", "", "qrScanLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "unlockRenderer", "Lcom/qrzen/app/ui/unlock/UnlockChallengeRenderer;", "applyPause", "", "block", "durationMs", "", "goToLauncher", "loadBlock", "blockId", "", "millisUntilMidnight", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onNewIntent", "intent", "setupUi", "showMasterPasswordDialog", "showPauseDurationSheet", "Companion", "app_debug"})
public final class LockScreenActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCK_ID = "extra_block_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCKED_PKG = "extra_blocked_pkg";
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.BlockEventDao blockEventDao;
    private com.qrzen.app.databinding.ActivityLockScreenBinding binding;
    private com.qrzen.app.ui.unlock.UnlockChallengeRenderer unlockRenderer;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock currentBlock;
    private boolean pauseSheetShown = false;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> qrScanLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.lock.LockScreenActivity.Companion Companion = null;
    
    public LockScreenActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppBlockDao getDao() {
        return null;
    }
    
    public final void setDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppBlockDao p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.BlockEventDao getBlockEventDao() {
        return null;
    }
    
    public final void setBlockEventDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.BlockEventDao p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void loadBlock(int blockId) {
    }
    
    private final void setupUi(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void showPauseDurationSheet(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void applyPause(com.qrzen.app.data.model.AppBlock block, long durationMs) {
    }
    
    private final long millisUntilMidnight() {
        return 0L;
    }
    
    private final void showMasterPasswordDialog(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void goToLauncher() {
    }
    
    @java.lang.Override()
    @java.lang.Deprecated()
    public void onBackPressed() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/qrzen/app/ui/lock/LockScreenActivity$Companion;", "", "()V", "EXTRA_BLOCKED_PKG", "", "EXTRA_BLOCK_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}