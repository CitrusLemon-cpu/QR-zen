package com.qrzen.app.ui.allowlist;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000f\b\u0007\u0018\u0000 G2\u00020\u0001:\u0003EFGB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u000e2\u0006\u0010%\u001a\u00020&H\u0002J\u001c\u0010\'\u001a\b\u0012\u0004\u0012\u00020)0(2\u0006\u0010$\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010*J\u0010\u0010+\u001a\u00020&2\u0006\u0010,\u001a\u00020\u001dH\u0002J\u0010\u0010-\u001a\u00020\u001d2\u0006\u0010.\u001a\u00020&H\u0002J\b\u0010/\u001a\u00020#H\u0002J\u0010\u00100\u001a\u00020#2\u0006\u00101\u001a\u00020\u001dH\u0002J\u0010\u00102\u001a\u00020#2\u0006\u00103\u001a\u000204H\u0002J\b\u00105\u001a\u00020&H\u0002J\b\u00106\u001a\u00020#H\u0017J\u0012\u00107\u001a\u00020#2\b\u00108\u001a\u0004\u0018\u000109H\u0014J\b\u0010:\u001a\u00020#H\u0014J\u0010\u0010;\u001a\u00020#2\u0006\u0010<\u001a\u00020\u0019H\u0014J\b\u0010=\u001a\u00020#H\u0002J\u001e\u0010>\u001a\u00020#2\u0006\u0010$\u001a\u00020\u000e2\f\u0010?\u001a\b\u0012\u0004\u0012\u00020)0(H\u0002J\u0010\u0010@\u001a\u00020#2\u0006\u0010$\u001a\u00020\u000eH\u0002J\u0010\u0010A\u001a\u00020#2\u0006\u0010$\u001a\u00020\u000eH\u0002J\u0010\u0010B\u001a\u00020#2\u0006\u0010C\u001a\u00020)H\u0002J\u0010\u0010D\u001a\u00020#2\u0006\u0010$\u001a\u00020\u000eH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0017\u001a\u0010\u0012\f\u0012\n \u001a*\u0004\u0018\u00010\u00190\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001e\u001a\n \u001a*\u0004\u0018\u00010\u001f0\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020!X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006H"}, d2 = {"Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/qrzen/app/databinding/ActivityAllowlistOverlayBinding;", "blockEventDao", "Lcom/qrzen/app/data/db/BlockEventDao;", "getBlockEventDao", "()Lcom/qrzen/app/data/db/BlockEventDao;", "setBlockEventDao", "(Lcom/qrzen/app/data/db/BlockEventDao;)V", "countDownTimer", "Landroid/os/CountDownTimer;", "currentBlock", "Lcom/qrzen/app/data/model/AppBlock;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "pauseSheetShown", "", "qrScanLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "sessionRemovedApps", "", "", "timeFormatter", "Ljava/time/format/DateTimeFormatter;", "unlockRenderer", "Lcom/qrzen/app/ui/unlock/UnlockChallengeRenderer;", "applyPause", "", "block", "durationMs", "", "buildAllowedApps", "", "Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppItem;", "(Lcom/qrzen/app/data/model/AppBlock;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "calculateMillisUntilEnd", "endTime", "formatCountdown", "millis", "goToLauncher", "launchAllowedApp", "packageName", "loadBlock", "blockId", "", "millisUntilMidnight", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onNewIntent", "intent", "refreshAllowedApps", "setupUi", "allowedApps", "showMasterPasswordDialog", "showPauseDurationSheet", "showRemoveAppDialog", "appItem", "startCountdown", "AllowedAppAdapter", "AllowedAppItem", "Companion", "app_debug"})
public final class AllowlistOverlayActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCK_ID = "extra_block_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCKED_PKG = "extra_blocked_pkg";
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.BlockEventDao blockEventDao;
    private com.qrzen.app.databinding.ActivityAllowlistOverlayBinding binding;
    private com.qrzen.app.ui.unlock.UnlockChallengeRenderer unlockRenderer;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock currentBlock;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> sessionRemovedApps = null;
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer countDownTimer;
    private boolean pauseSheetShown = false;
    private final java.time.format.DateTimeFormatter timeFormatter = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> qrScanLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.Companion Companion = null;
    
    public AllowlistOverlayActivity() {
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
    
    private final void setupUi(com.qrzen.app.data.model.AppBlock block, java.util.List<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem> allowedApps) {
    }
    
    private final java.lang.Object buildAllowedApps(com.qrzen.app.data.model.AppBlock block, kotlin.coroutines.Continuation<? super java.util.List<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem>> $completion) {
        return null;
    }
    
    private final void startCountdown(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final long calculateMillisUntilEnd(java.lang.String endTime) {
        return 0L;
    }
    
    private final java.lang.String formatCountdown(long millis) {
        return null;
    }
    
    private final void launchAllowedApp(java.lang.String packageName) {
    }
    
    private final void showRemoveAppDialog(com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem appItem) {
    }
    
    private final void refreshAllowedApps() {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u001fBI\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007\u0012\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\t0\u0007\u0012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\f\u00a2\u0006\u0002\u0010\rJ\u0006\u0010\u0011\u001a\u00020\tJ\u0010\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0016J\u001c\u0010\u0017\u001a\u00020\t2\n\u0010\u0018\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0019\u001a\u00020\u0016H\u0016J\u001c\u0010\u001a\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0016H\u0016J\u0014\u0010\u001e\u001a\u00020\t2\n\u0010\u0018\u001a\u00060\u0002R\u00020\u0000H\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppAdapter$ViewHolder;", "apps", "", "Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppItem;", "onClick", "Lkotlin/Function1;", "", "", "onLongPress", "onTimerExpired", "Lkotlin/Function0;", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;)V", "timerHandlers", "", "Landroid/os/CountDownTimer;", "cancelAllTimers", "formatTimerOverlay", "millis", "", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "onViewRecycled", "ViewHolder", "app_debug"})
    public static final class AllowedAppAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppAdapter.ViewHolder> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem> apps = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onClick = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem, kotlin.Unit> onLongPress = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function0<kotlin.Unit> onTimerExpired = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Map<java.lang.String, android.os.CountDownTimer> timerHandlers = null;
        
        public AllowedAppAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem> apps, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem, kotlin.Unit> onLongPress, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<kotlin.Unit> onTimerExpired) {
            super();
        }
        
        private final java.lang.String formatTimerOverlay(long millis) {
            return null;
        }
        
        public final void cancelAllTimers() {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppAdapter.ViewHolder holder, int position) {
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @java.lang.Override()
        public void onViewRecycled(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppAdapter.ViewHolder holder) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\n\u00a8\u0006\u000f"}, d2 = {"Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/qrzen/app/databinding/ItemAllowedAppBinding;", "(Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppAdapter;Lcom/qrzen/app/databinding/ItemAllowedAppBinding;)V", "boundPackageName", "", "getBoundPackageName", "()Ljava/lang/String;", "setBoundPackageName", "(Ljava/lang/String;)V", "bind", "", "item", "Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppItem;", "app_debug"})
        public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final com.qrzen.app.databinding.ItemAllowedAppBinding binding = null;
            @org.jetbrains.annotations.Nullable()
            private java.lang.String boundPackageName;
            
            public ViewHolder(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.databinding.ItemAllowedAppBinding binding) {
                super(null);
            }
            
            @org.jetbrains.annotations.Nullable()
            public final java.lang.String getBoundPackageName() {
                return null;
            }
            
            public final void setBoundPackageName(@org.jetbrains.annotations.Nullable()
            java.lang.String p0) {
            }
            
            public final void bind(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem item) {
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\bH\u00c6\u0003J1\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\u001c"}, d2 = {"Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$AllowedAppItem;", "", "packageName", "", "label", "icon", "Landroid/graphics/drawable/Drawable;", "timerExpiry", "", "(Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;J)V", "getIcon", "()Landroid/graphics/drawable/Drawable;", "getLabel", "()Ljava/lang/String;", "getPackageName", "getTimerExpiry", "()J", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class AllowedAppItem {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String packageName = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        @org.jetbrains.annotations.NotNull()
        private final android.graphics.drawable.Drawable icon = null;
        private final long timerExpiry = 0L;
        
        public AllowedAppItem(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String label, @org.jetbrains.annotations.NotNull()
        android.graphics.drawable.Drawable icon, long timerExpiry) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPackageName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLabel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.graphics.drawable.Drawable getIcon() {
            return null;
        }
        
        public final long getTimerExpiry() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.graphics.drawable.Drawable component3() {
            return null;
        }
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.AllowedAppItem copy(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String label, @org.jetbrains.annotations.NotNull()
        android.graphics.drawable.Drawable icon, long timerExpiry) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/qrzen/app/ui/allowlist/AllowlistOverlayActivity$Companion;", "", "()V", "EXTRA_BLOCKED_PKG", "", "EXTRA_BLOCK_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}