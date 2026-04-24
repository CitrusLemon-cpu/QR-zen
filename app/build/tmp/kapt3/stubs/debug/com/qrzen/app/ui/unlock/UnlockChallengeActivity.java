package com.qrzen.app.ui.unlock;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0017\u001a\u00020\u0018H\u0002J\u0012\u0010\u0019\u001a\u00020\u00182\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0014J\b\u0010\u001c\u001a\u00020\u0018H\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001c\u0010\u000b\u001a\u0010\u0012\f\u0012\n \u000e*\u0004\u0018\u00010\r0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0011\u001a\u00020\u00128\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016\u00a8\u0006\u001e"}, d2 = {"Lcom/qrzen/app/ui/unlock/UnlockChallengeActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/qrzen/app/databinding/ActivityUnlockChallengeBinding;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "qrScanLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "renderer", "Lcom/qrzen/app/ui/unlock/UnlockChallengeRenderer;", "timeBlockDao", "Lcom/qrzen/app/data/db/TimeBlockDao;", "getTimeBlockDao", "()Lcom/qrzen/app/data/db/TimeBlockDao;", "setTimeBlockDao", "(Lcom/qrzen/app/data/db/TimeBlockDao;)V", "loadBlock", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "Companion", "app_debug"})
public final class UnlockChallengeActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCK_ID = "extra_block_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ACTION = "extra_action";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_PAUSE = "PAUSE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_EDIT = "EDIT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_TOGGLE = "TOGGLE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_DELETE = "DELETE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_ARCHIVE = "ARCHIVE";
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.TimeBlockDao timeBlockDao;
    private com.qrzen.app.databinding.ActivityUnlockChallengeBinding binding;
    private com.qrzen.app.ui.unlock.UnlockChallengeRenderer renderer;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> qrScanLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.unlock.UnlockChallengeActivity.Companion Companion = null;
    
    public UnlockChallengeActivity() {
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
    public final com.qrzen.app.data.db.TimeBlockDao getTimeBlockDao() {
        return null;
    }
    
    public final void setTimeBlockDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.TimeBlockDao p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void loadBlock() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/qrzen/app/ui/unlock/UnlockChallengeActivity$Companion;", "", "()V", "ACTION_ARCHIVE", "", "ACTION_DELETE", "ACTION_EDIT", "ACTION_PAUSE", "ACTION_TOGGLE", "EXTRA_ACTION", "EXTRA_BLOCK_ID", "createIntent", "Landroid/content/Intent;", "context", "Landroid/content/Context;", "blockId", "", "action", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.content.Intent createIntent(@org.jetbrains.annotations.NotNull()
        android.content.Context context, int blockId, @org.jetbrains.annotations.NotNull()
        java.lang.String action) {
            return null;
        }
    }
}