package com.qrzen.app.ui.main;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000p\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001:\u00015B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u000bH\u0002J\u0010\u0010\u0019\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u001dH\u0002J\b\u0010\u001e\u001a\u00020\u001fH\u0002J$\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010%2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J\b\u0010(\u001a\u00020\u0017H\u0016J\b\u0010)\u001a\u00020\u0017H\u0016J\u001a\u0010*\u001a\u00020\u00172\u0006\u0010+\u001a\u00020!2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J)\u0010,\u001a\u00020\u001d2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010-\u001a\u00020.2\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u001dH\u0002\u00a2\u0006\u0002\u00100J\'\u00101\u001a\u00020\u001d2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u00010\u001dH\u0002\u00a2\u0006\u0002\u00100J\u0010\u00102\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0010\u00103\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u00104\u001a\u00020\u0017H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\f\u001a\u0010\u0012\f\u0012\n \u000f*\u0004\u0018\u00010\u000e0\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013\u00a8\u00066"}, d2 = {"Lcom/qrzen/app/ui/main/HomeFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/qrzen/app/databinding/FragmentHomeBinding;", "adapter", "Lcom/qrzen/app/ui/main/BlockAdapter;", "binding", "getBinding", "()Lcom/qrzen/app/databinding/FragmentHomeBinding;", "pendingUnlockAction", "Lcom/qrzen/app/ui/main/HomeFragment$PendingUnlockAction;", "unlockChallengeLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "viewModel", "Lcom/qrzen/app/ui/main/HomeViewModel;", "getViewModel", "()Lcom/qrzen/app/ui/main/HomeViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "completePendingUnlockAction", "", "pending", "goToHomeIfBlockActive", "block", "Lcom/qrzen/app/data/model/AppBlock;", "isAccessibilityServiceEnabled", "", "millisUntilMidnight", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onResume", "onViewCreated", "view", "requestUnlock", "action", "", "toggleEnabledState", "(Lcom/qrzen/app/data/model/AppBlock;Ljava/lang/String;Ljava/lang/Boolean;)Z", "shouldSkipUnlock", "showBlockNowDurationPicker", "showPauseDurationPicker", "updateServiceWarning", "PendingUnlockAction", "app_debug"})
public final class HomeFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.databinding.FragmentHomeBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.qrzen.app.ui.main.BlockAdapter adapter;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.ui.main.HomeFragment.PendingUnlockAction pendingUnlockAction;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> unlockChallengeLauncher = null;
    
    public HomeFragment() {
        super();
    }
    
    private final com.qrzen.app.databinding.FragmentHomeBinding getBinding() {
        return null;
    }
    
    private final com.qrzen.app.ui.main.HomeViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    private final boolean requestUnlock(com.qrzen.app.data.model.AppBlock block, java.lang.String action, java.lang.Boolean toggleEnabledState) {
        return false;
    }
    
    private final boolean shouldSkipUnlock(com.qrzen.app.data.model.AppBlock block, java.lang.String action, java.lang.Boolean toggleEnabledState) {
        return false;
    }
    
    private final void completePendingUnlockAction(com.qrzen.app.ui.main.HomeFragment.PendingUnlockAction pending) {
    }
    
    private final void showPauseDurationPicker(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void showBlockNowDurationPicker(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final long millisUntilMidnight() {
        return 0L;
    }
    
    private final void goToHomeIfBlockActive(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final boolean isAccessibilityServiceEnabled() {
        return false;
    }
    
    private final void updateServiceWarning() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000eJ.\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0014J\u0013\u0010\u0015\u001a\u00020\u00072\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u001a"}, d2 = {"Lcom/qrzen/app/ui/main/HomeFragment$PendingUnlockAction;", "", "block", "Lcom/qrzen/app/data/model/AppBlock;", "action", "", "toggleEnabledState", "", "(Lcom/qrzen/app/data/model/AppBlock;Ljava/lang/String;Ljava/lang/Boolean;)V", "getAction", "()Ljava/lang/String;", "getBlock", "()Lcom/qrzen/app/data/model/AppBlock;", "getToggleEnabledState", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "component1", "component2", "component3", "copy", "(Lcom/qrzen/app/data/model/AppBlock;Ljava/lang/String;Ljava/lang/Boolean;)Lcom/qrzen/app/ui/main/HomeFragment$PendingUnlockAction;", "equals", "other", "hashCode", "", "toString", "app_debug"})
    static final class PendingUnlockAction {
        @org.jetbrains.annotations.NotNull()
        private final com.qrzen.app.data.model.AppBlock block = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String action = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Boolean toggleEnabledState = null;
        
        public PendingUnlockAction(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
        java.lang.String action, @org.jetbrains.annotations.Nullable()
        java.lang.Boolean toggleEnabledState) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.data.model.AppBlock getBlock() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAction() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Boolean getToggleEnabledState() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.data.model.AppBlock component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Boolean component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.ui.main.HomeFragment.PendingUnlockAction copy(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
        java.lang.String action, @org.jetbrains.annotations.Nullable()
        java.lang.Boolean toggleEnabledState) {
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