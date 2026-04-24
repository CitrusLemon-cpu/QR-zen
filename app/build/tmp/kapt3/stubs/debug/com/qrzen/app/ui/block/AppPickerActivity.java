package com.qrzen.app.ui.block;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0003\u0019\u001a\u001bB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\b\u0010\u0014\u001a\u00020\u0011H\u0002J\u0012\u0010\u0015\u001a\u00020\u00112\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0014J\b\u0010\u0018\u001a\u00020\u0011H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001c"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "adapter", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter;", "allApps", "", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppItem;", "binding", "Lcom/qrzen/app/databinding/ActivityAppPickerBinding;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "filterApps", "", "query", "", "loadApps", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "showImportFromBlockDialog", "AppItem", "AppPickerAdapter", "Companion", "app_debug"})
public final class AppPickerActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_PRESELECTED = "extra_preselected";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_RESULT = "extra_result";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_IS_ALLOWLIST = "extra_is_allowlist";
    public static final int REQ_CODE = 2001;
    private com.qrzen.app.databinding.ActivityAppPickerBinding binding;
    private com.qrzen.app.ui.block.AppPickerActivity.AppPickerAdapter adapter;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.qrzen.app.ui.block.AppPickerActivity.AppItem> allApps;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.block.AppPickerActivity.Companion Companion = null;
    
    public AppPickerActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppBlockDao getDao() {
        return null;
    }
    
    public final void setDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppBlockDao p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void loadApps() {
    }
    
    private final void filterApps(java.lang.String query) {
    }
    
    private final void showImportFromBlockDialog() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0006H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000b\u00a8\u0006\u0017"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity$AppItem;", "", "packageName", "", "label", "icon", "Landroid/graphics/drawable/Drawable;", "(Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;)V", "getIcon", "()Landroid/graphics/drawable/Drawable;", "getLabel", "()Ljava/lang/String;", "getPackageName", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class AppItem {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String packageName = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        @org.jetbrains.annotations.NotNull()
        private final android.graphics.drawable.Drawable icon = null;
        
        public AppItem(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String label, @org.jetbrains.annotations.NotNull()
        android.graphics.drawable.Drawable icon) {
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
        
        @org.jetbrains.annotations.NotNull()
        public final com.qrzen.app.ui.block.AppPickerActivity.AppItem copy(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String label, @org.jetbrains.annotations.NotNull()
        android.graphics.drawable.Drawable icon) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00152\u0012\u0012\u0004\u0012\u00020\u0002\u0012\b\u0012\u00060\u0003R\u00020\u00000\u0001:\u0002\u0015\u0016B\u0013\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\u0010\u0007J\u0014\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bJ\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bJ\u001c\u0010\r\u001a\u00020\t2\n\u0010\u000e\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000f\u001a\u00020\u0010H\u0016J\u001c\u0010\u0011\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0010H\u0016R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppItem;", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter$ViewHolder;", "selected", "", "", "(Ljava/util/Set;)V", "addPackages", "", "packages", "", "getSelectedPackages", "onBindViewHolder", "holder", "position", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "Companion", "ViewHolder", "app_debug"})
    public static final class AppPickerAdapter extends androidx.recyclerview.widget.ListAdapter<com.qrzen.app.ui.block.AppPickerActivity.AppItem, com.qrzen.app.ui.block.AppPickerActivity.AppPickerAdapter.ViewHolder> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.Set<java.lang.String> selected = null;
        @org.jetbrains.annotations.NotNull()
        private static final androidx.recyclerview.widget.DiffUtil.ItemCallback<com.qrzen.app.ui.block.AppPickerActivity.AppItem> DIFF = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.qrzen.app.ui.block.AppPickerActivity.AppPickerAdapter.Companion Companion = null;
        
        public AppPickerAdapter(@org.jetbrains.annotations.NotNull()
        java.util.Set<java.lang.String> selected) {
            super(null);
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.qrzen.app.ui.block.AppPickerActivity.AppPickerAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.ui.block.AppPickerActivity.AppPickerAdapter.ViewHolder holder, int position) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Set<java.lang.String> getSelectedPackages() {
            return null;
        }
        
        public final void addPackages(@org.jetbrains.annotations.NotNull()
        java.util.Set<java.lang.String> packages) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter$Companion;", "", "()V", "DIFF", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppItem;", "getDIFF", "()Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final androidx.recyclerview.widget.DiffUtil.ItemCallback<com.qrzen.app.ui.block.AppPickerActivity.AppItem> getDIFF() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/qrzen/app/databinding/ItemAppPickerBinding;", "(Lcom/qrzen/app/ui/block/AppPickerActivity$AppPickerAdapter;Lcom/qrzen/app/databinding/ItemAppPickerBinding;)V", "getBinding", "()Lcom/qrzen/app/databinding/ItemAppPickerBinding;", "bind", "", "item", "Lcom/qrzen/app/ui/block/AppPickerActivity$AppItem;", "app_debug"})
        public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final com.qrzen.app.databinding.ItemAppPickerBinding binding = null;
            
            public ViewHolder(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.databinding.ItemAppPickerBinding binding) {
                super(null);
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.qrzen.app.databinding.ItemAppPickerBinding getBinding() {
                return null;
            }
            
            public final void bind(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.ui.block.AppPickerActivity.AppItem item) {
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/qrzen/app/ui/block/AppPickerActivity$Companion;", "", "()V", "EXTRA_IS_ALLOWLIST", "", "EXTRA_PRESELECTED", "EXTRA_RESULT", "REQ_CODE", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}