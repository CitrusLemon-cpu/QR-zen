package com.qrzen.app.ui.block;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u0000 [2\u00020\u0001:\u0003[\\]B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010,\u001a\u00020-H\u0002J\u0016\u0010.\u001a\u00020\u00062\f\u0010/\u001a\b\u0012\u0004\u0012\u0002000&H\u0002J\u000e\u00101\u001a\b\u0012\u0004\u0012\u0002000&H\u0002J\u0010\u00102\u001a\u00020\u00062\u0006\u00103\u001a\u00020\u0019H\u0002J\u0010\u00104\u001a\u00020\u00062\u0006\u00105\u001a\u00020\u0019H\u0002J\u000e\u00106\u001a\b\u0012\u0004\u0012\u00020\u00060&H\u0002J\u0010\u00107\u001a\u00020\u00062\u0006\u00108\u001a\u00020\u0006H\u0002J\u0010\u00109\u001a\u00020\u00172\u0006\u0010:\u001a\u00020\u0015H\u0002J\"\u0010;\u001a\u00020-2\u0006\u0010<\u001a\u00020\u000f2\u0006\u0010=\u001a\u00020\u000f2\b\u0010>\u001a\u0004\u0018\u00010\u001cH\u0015J\u0012\u0010?\u001a\u00020-2\b\u0010@\u001a\u0004\u0018\u00010AH\u0014J\u0010\u0010B\u001a\u00020-2\u0006\u0010:\u001a\u00020\u0015H\u0002J\b\u0010C\u001a\u00020-H\u0002J\u000e\u0010D\u001a\b\u0012\u0004\u0012\u0002000&H\u0002J\u001e\u0010E\u001a\u00020-2\f\u0010/\u001a\b\u0012\u0004\u0012\u0002000&2\u0006\u0010F\u001a\u00020\u0006H\u0002J\b\u0010G\u001a\u00020-H\u0002J\b\u0010H\u001a\u00020-H\u0002J\u0010\u0010I\u001a\u00020-2\u0006\u0010J\u001a\u00020KH\u0002J\b\u0010L\u001a\u00020-H\u0002J\u0010\u0010M\u001a\u00020-2\u0006\u0010N\u001a\u00020\u0019H\u0002J,\u0010O\u001a\u00020-2\u0006\u0010P\u001a\u00020\u00062\u0006\u0010Q\u001a\u00020\u00062\u0012\u0010R\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020-0SH\u0002J\u0010\u0010T\u001a\u00020\u00192\u0006\u00103\u001a\u00020\u0019H\u0002J\b\u0010U\u001a\u00020-H\u0002J\b\u0010V\u001a\u00020-H\u0002J\b\u0010W\u001a\u00020-H\u0002J\b\u0010X\u001a\u00020-H\u0002J\b\u0010Y\u001a\u00020-H\u0002J\b\u0010Z\u001a\u00020-H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\b\u001a\u00020\t8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u001a\u001a\u0010\u0012\f\u0012\n \u001d*\u0004\u0018\u00010\u001c0\u001c0\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R-\u0010%\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\'0&8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b*\u0010+\u001a\u0004\b(\u0010)\u00a8\u0006^"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/qrzen/app/databinding/ActivityEditBlockBinding;", "blockPassword", "", "currentQrSecret", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "delayMinutes", "", "editWindowDays", "editWindowEnd", "editWindowStart", "endTime", "existingBlock", "Lcom/qrzen/app/data/model/AppBlock;", "isAllowlistMode", "", "lockUntil", "", "qrScanForSetLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "selectedAppsLoadJob", "Lkotlinx/coroutines/Job;", "selectedPackages", "startTime", "typeOverIsRandom", "typeOverText", "unlockMethod", "unlockMethods", "", "Lkotlin/Pair;", "getUnlockMethods", "()Ljava/util/List;", "unlockMethods$delegate", "Lkotlin/Lazy;", "applyCurrentStateToUi", "", "buildDaysString", "toggles", "Landroid/widget/ToggleButton;", "editWindowDayToggles", "formatDateTime", "epochMillis", "formatTimerBadge", "millis", "getSelectedPackageList", "getUnlockMethodLabel", "method", "isBlockCurrentlyActive", "block", "onActivityResult", "requestCode", "resultCode", "data", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "populateForm", "saveBlock", "scheduleDayToggles", "setToggleStates", "days", "setupUi", "setupUnlockMethodDropdown", "showAppTimerDialog", "appItem", "Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppIcon;", "showLockUntilPicker", "showLockUntilTimePicker", "selectedDateUtcMillis", "showTimePicker", "title", "current", "onPicked", "Lkotlin/Function1;", "toUtcDateSelection", "updateEditWindowButtons", "updateLockUntilDisplay", "updateScheduleButtons", "updateSelectedAppsDisplay", "updateTypeOverUi", "updateUnlockMethodUi", "Companion", "SelectedAppIcon", "SelectedAppsAdapter", "app_debug"})
public final class EditBlockActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCK_ID = "extra_block_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_IS_ALLOWLIST = "extra_is_allowlist";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_NONE = "NONE";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_DELAY = "DELAY";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_PASSWORD = "PASSWORD";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_TYPE_OVER_TEXT = "TYPE_OVER_TEXT";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_QR_CODE = "QR_CODE";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_EDIT_WINDOW = "EDIT_WINDOW";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_TIMER = "TIMER";
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    private com.qrzen.app.databinding.ActivityEditBlockBinding binding;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock existingBlock;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentQrSecret = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String selectedPackages = "";
    private boolean isAllowlistMode = false;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String startTime = "00:00";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String endTime = "23:59";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String unlockMethod = "NONE";
    private int delayMinutes = 5;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String blockPassword = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String typeOverText = "";
    private boolean typeOverIsRandom = true;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String editWindowStart = "09:00";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String editWindowEnd = "10:00";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String editWindowDays = "1111111";
    private long lockUntil = 0L;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job selectedAppsLoadJob;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> qrScanForSetLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy unlockMethods$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.block.EditBlockActivity.Companion Companion = null;
    
    public EditBlockActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppBlockDao getDao() {
        return null;
    }
    
    public final void setDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppBlockDao p0) {
    }
    
    private final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> getUnlockMethods() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupUi() {
    }
    
    private final void setupUnlockMethodDropdown() {
    }
    
    private final void populateForm(com.qrzen.app.data.model.AppBlock block) {
    }
    
    private final void applyCurrentStateToUi() {
    }
    
    @java.lang.Override()
    @java.lang.Deprecated()
    protected void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    private final void updateSelectedAppsDisplay() {
    }
    
    private final java.util.List<java.lang.String> getSelectedPackageList() {
        return null;
    }
    
    private final void showAppTimerDialog(com.qrzen.app.ui.block.EditBlockActivity.SelectedAppIcon appItem) {
    }
    
    private final java.lang.String formatTimerBadge(long millis) {
        return null;
    }
    
    private final void updateScheduleButtons() {
    }
    
    private final void updateEditWindowButtons() {
    }
    
    private final void updateUnlockMethodUi() {
    }
    
    private final void updateTypeOverUi() {
    }
    
    private final void updateLockUntilDisplay() {
    }
    
    private final void saveBlock() {
    }
    
    private final boolean isBlockCurrentlyActive(com.qrzen.app.data.model.AppBlock block) {
        return false;
    }
    
    private final void showTimePicker(java.lang.String title, java.lang.String current, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPicked) {
    }
    
    private final void showLockUntilPicker() {
    }
    
    private final void showLockUntilTimePicker(long selectedDateUtcMillis) {
    }
    
    private final java.lang.String buildDaysString(java.util.List<? extends android.widget.ToggleButton> toggles) {
        return null;
    }
    
    private final void setToggleStates(java.util.List<? extends android.widget.ToggleButton> toggles, java.lang.String days) {
    }
    
    private final java.util.List<android.widget.ToggleButton> scheduleDayToggles() {
        return null;
    }
    
    private final java.util.List<android.widget.ToggleButton> editWindowDayToggles() {
        return null;
    }
    
    private final java.lang.String getUnlockMethodLabel(java.lang.String method) {
        return null;
    }
    
    private final java.lang.String formatDateTime(long epochMillis) {
        return null;
    }
    
    private final long toUtcDateSelection(long epochMillis) {
        return 0L;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity$Companion;", "", "()V", "EXTRA_BLOCK_ID", "", "EXTRA_IS_ALLOWLIST", "UNLOCK_DELAY", "UNLOCK_EDIT_WINDOW", "UNLOCK_NONE", "UNLOCK_PASSWORD", "UNLOCK_QR_CODE", "UNLOCK_TIMER", "UNLOCK_TYPE_OVER_TEXT", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\bH\u00c6\u0003J1\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\u001c"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppIcon;", "", "packageName", "", "label", "icon", "Landroid/graphics/drawable/Drawable;", "timerExpiry", "", "(Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;J)V", "getIcon", "()Landroid/graphics/drawable/Drawable;", "getLabel", "()Ljava/lang/String;", "getPackageName", "getTimerExpiry", "()J", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class SelectedAppIcon {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String packageName = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        @org.jetbrains.annotations.NotNull()
        private final android.graphics.drawable.Drawable icon = null;
        private final long timerExpiry = 0L;
        
        public SelectedAppIcon(@org.jetbrains.annotations.NotNull()
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
        public final com.qrzen.app.ui.block.EditBlockActivity.SelectedAppIcon copy(@org.jetbrains.annotations.NotNull()
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0082\u0004\u0018\u00002\u0010\u0012\f\u0012\n0\u0002R\u00060\u0000R\u00020\u00030\u0001:\u0001\u0012B\u0013\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J \u0010\n\u001a\u00020\u000b2\u000e\u0010\f\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\r\u001a\u00020\tH\u0016J \u0010\u000e\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\tH\u0016R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppsAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppsAdapter$ViewHolder;", "Lcom/qrzen/app/ui/block/EditBlockActivity;", "apps", "", "Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppIcon;", "(Lcom/qrzen/app/ui/block/EditBlockActivity;Ljava/util/List;)V", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ViewHolder", "app_debug"})
    final class SelectedAppsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.qrzen.app.ui.block.EditBlockActivity.SelectedAppsAdapter.ViewHolder> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.qrzen.app.ui.block.EditBlockActivity.SelectedAppIcon> apps = null;
        
        public SelectedAppsAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.qrzen.app.ui.block.EditBlockActivity.SelectedAppIcon> apps) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.qrzen.app.ui.block.EditBlockActivity.SelectedAppsAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.qrzen.app.ui.block.EditBlockActivity.SelectedAppsAdapter.ViewHolder holder, int position) {
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppsAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/qrzen/app/databinding/ItemEditAppGridBinding;", "(Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppsAdapter;Lcom/qrzen/app/databinding/ItemEditAppGridBinding;)V", "getBinding", "()Lcom/qrzen/app/databinding/ItemEditAppGridBinding;", "bind", "", "item", "Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppIcon;", "app_debug"})
        public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final com.qrzen.app.databinding.ItemEditAppGridBinding binding = null;
            
            public ViewHolder(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.databinding.ItemEditAppGridBinding binding) {
                super(null);
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.qrzen.app.databinding.ItemEditAppGridBinding getBinding() {
                return null;
            }
            
            public final void bind(@org.jetbrains.annotations.NotNull()
            com.qrzen.app.ui.block.EditBlockActivity.SelectedAppIcon item) {
            }
        }
    }
}