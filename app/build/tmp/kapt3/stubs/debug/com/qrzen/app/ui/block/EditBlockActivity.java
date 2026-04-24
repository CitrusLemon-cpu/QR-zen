package com.qrzen.app.ui.block;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00bc\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0011\b\u0007\u0018\u0000 \u0099\u00012\u00020\u0001:\u0006\u0099\u0001\u009a\u0001\u009b\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010H\u001a\u00020IH\u0002J\u0016\u0010J\u001a\u00020\u00042\f\u0010K\u001a\b\u0012\u0004\u0012\u00020L0\nH\u0002J*\u0010M\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00190\u000b0\n2\u0006\u0010N\u001a\u00020\u00042\u0006\u0010O\u001a\u00020\u0004H\u0002J\b\u0010P\u001a\u00020IH\u0002J\b\u0010Q\u001a\u00020\u0019H\u0002J\u000e\u0010R\u001a\b\u0012\u0004\u0012\u00020L0\nH\u0002J\u0010\u0010S\u001a\u00020\u00042\u0006\u0010T\u001a\u00020#H\u0002J\u0010\u0010U\u001a\u00020\u00042\u0006\u0010V\u001a\u00020#H\u0002J\u0010\u0010W\u001a\u00020\u00042\u0006\u0010X\u001a\u00020\u0004H\u0002J\u000e\u0010Y\u001a\b\u0012\u0004\u0012\u00020\u00040\nH\u0002J\u0010\u0010Z\u001a\u00020\u00042\u0006\u0010[\u001a\u00020\u0004H\u0002J1\u0010\\\u001a\u00020 2\u0006\u0010N\u001a\u00020\u00042\u0006\u0010O\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00042\n\b\u0002\u0010]\u001a\u0004\u0018\u00010\u0019H\u0002\u00a2\u0006\u0002\u0010^J\b\u0010_\u001a\u00020IH\u0002J\u0010\u0010`\u001a\u00020I2\u0006\u0010\u0003\u001a\u00020\u0004H\u0002J\u0010\u0010a\u001a\u00020 2\u0006\u0010b\u001a\u00020\u001eH\u0002J\u0010\u0010c\u001a\u00020 2\u0006\u0010d\u001a\u00020\u0011H\u0002J\b\u0010e\u001a\u00020IH\u0002J\"\u0010f\u001a\u00020I2\u0006\u0010g\u001a\u00020\u00192\u0006\u0010h\u001a\u00020\u00192\b\u0010i\u001a\u0004\u0018\u00010\'H\u0015J\u0012\u0010j\u001a\u00020I2\b\u0010k\u001a\u0004\u0018\u00010lH\u0014J\u0010\u0010m\u001a\u00020n2\u0006\u0010o\u001a\u00020\u0004H\u0002J\u0010\u0010p\u001a\u00020\u00192\u0006\u0010o\u001a\u00020\u0004H\u0002J\u0010\u0010q\u001a\u00020I2\u0006\u0010b\u001a\u00020\u001eH\u0002J<\u0010r\u001a\u00020 2\u0018\u0010s\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00190\u000b0\n2\u0018\u0010t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00190\u000b0\nH\u0002J\b\u0010u\u001a\u00020IH\u0002J\u0010\u0010v\u001a\u00020I2\u0006\u0010w\u001a\u00020\u0011H\u0002J\b\u0010x\u001a\u00020IH\u0002J\u001e\u0010y\u001a\u00020I2\f\u0010K\u001a\b\u0012\u0004\u0012\u00020L0\n2\u0006\u0010z\u001a\u00020\u0004H\u0002J\b\u0010{\u001a\u00020IH\u0002J\b\u0010|\u001a\u00020IH\u0002J\b\u0010}\u001a\u00020IH\u0002J\b\u0010~\u001a\u00020IH\u0002J\u0012\u0010\u007f\u001a\u00020I2\b\u0010\u0080\u0001\u001a\u00030\u0081\u0001H\u0002J\t\u0010\u0082\u0001\u001a\u00020IH\u0002J\t\u0010\u0083\u0001\u001a\u00020IH\u0002J\u0012\u0010\u0084\u0001\u001a\u00020I2\u0007\u0010\u0085\u0001\u001a\u00020#H\u0002J\u0011\u0010\u0086\u0001\u001a\u00020I2\u0006\u0010d\u001a\u00020\u0011H\u0002J1\u0010\u0087\u0001\u001a\u00020I2\u0007\u0010\u0088\u0001\u001a\u00020\u00042\u0007\u0010\u0089\u0001\u001a\u00020\u00042\u0014\u0010\u008a\u0001\u001a\u000f\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020I0\u008b\u0001H\u0002J\u001b\u0010\u008c\u0001\u001a\u00020I2\u0007\u0010\u008d\u0001\u001a\u0002022\u0007\u0010\u008e\u0001\u001a\u00020 H\u0002J\t\u0010\u008f\u0001\u001a\u00020IH\u0002J\u0011\u0010\u0090\u0001\u001a\u00020#2\u0006\u0010T\u001a\u00020#H\u0002J\t\u0010\u0091\u0001\u001a\u00020IH\u0002J\t\u0010\u0092\u0001\u001a\u00020IH\u0002J\t\u0010\u0093\u0001\u001a\u00020IH\u0002J\t\u0010\u0094\u0001\u001a\u00020IH\u0002J\t\u0010\u0095\u0001\u001a\u00020IH\u0002J\t\u0010\u0096\u0001\u001a\u00020IH\u0002J\u000f\u0010\u0097\u0001\u001a\b\u0012\u0004\u0012\u00020L0\nH\u0002J\u000f\u0010\u0098\u0001\u001a\b\u0012\u0004\u0012\u00020L0\nH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R&\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u000b0\n8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0012\u001a\u00020\u00138\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010%\u001a\u0010\u0012\f\u0012\n (*\u0004\u0018\u00010\'0\'0&X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010)\u001a\u0004\u0018\u00010*X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020,X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020.X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u00020,X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u00100\u001a\u00020,X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u00101\u001a\b\u0012\u0004\u0012\u0002020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u00103\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u00104\u001a\u0002058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b6\u00107\"\u0004\b8\u00109R\u000e\u0010:\u001a\u00020;X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010=\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010>\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010?\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R-\u0010@\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u000b0\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\bB\u0010C\u001a\u0004\bA\u0010\rR\u000e\u0010D\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010E\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010F\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010G\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u009c\u0001"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "activeDays", "", "binding", "Lcom/qrzen/app/databinding/ActivityEditBlockBinding;", "blockPassword", "blockingStyle", "blockingStyles", "", "Lkotlin/Pair;", "getBlockingStyles", "()Ljava/util/List;", "currentQrSecret", "currentTimeBlocks", "", "Lcom/qrzen/app/data/model/TimeBlock;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "delayMinutes", "", "editWindowDays", "editWindowEnd", "editWindowStart", "existingBlock", "Lcom/qrzen/app/data/model/AppBlock;", "isAllowlistMode", "", "isUpdatingTimerBreakPresets", "lockUntil", "", "nextTempId", "qrScanForSetLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "selectedAppsLoadJob", "Lkotlinx/coroutines/Job;", "selectedBlockDeleteButton", "Lcom/google/android/material/button/MaterialButton;", "selectedBlockDetailView", "Landroid/view/View;", "selectedBlockEndButton", "selectedBlockStartButton", "selectedDayViews", "Landroid/widget/TextView;", "selectedPackages", "timeBlockDao", "Lcom/qrzen/app/data/db/TimeBlockDao;", "getTimeBlockDao", "()Lcom/qrzen/app/data/db/TimeBlockDao;", "setTimeBlockDao", "(Lcom/qrzen/app/data/db/TimeBlockDao;)V", "timeFormatter", "Ljava/time/format/DateTimeFormatter;", "timerBreakMinutes", "typeOverIsRandom", "typeOverText", "unlockMethod", "unlockMethods", "getUnlockMethods", "unlockMethods$delegate", "Lkotlin/Lazy;", "usageLimitMinutes", "usageLimitPeriod", "waitTimerUseMinutes", "waitTimerWaitMinutes", "applyCurrentStateToUi", "", "buildDaysString", "toggles", "Landroid/widget/ToggleButton;", "buildSegments", "startTime", "endTime", "clearTimerBreakPresetSelection", "currentDayIndex", "editWindowDayToggles", "formatDateTime", "epochMillis", "formatTimerBadge", "millis", "getBlockingStyleLabel", "style", "getSelectedPackageList", "getUnlockMethodLabel", "method", "hasTimeBlockOverlap", "ignoreBlockId", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)Z", "hideSelectedBlockDetail", "highlightSelectedDays", "isBlockCurrentlyActive", "block", "isTimeBlockCurrentlyActive", "timeBlock", "normalizeBlockingStyle", "onActivityResult", "requestCode", "resultCode", "data", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "parseLocalTime", "Ljava/time/LocalTime;", "value", "parseMinutes", "populateForm", "rangesOverlap", "first", "second", "refreshBlockingStyleDropdown", "replaceTimeBlock", "updated", "saveBlock", "setToggleStates", "days", "setupBlockingStyleDropdown", "setupUi", "setupUnlockMethodDropdown", "showAddTimeBlockDialog", "showAppTimerDialog", "appItem", "Lcom/qrzen/app/ui/block/EditBlockActivity$SelectedAppIcon;", "showCustomBreakDurationPicker", "showLockUntilPicker", "showLockUntilTimePicker", "selectedDateUtcMillis", "showSelectedBlockDetail", "showTimePicker", "title", "current", "onPicked", "Lkotlin/Function1;", "styleSelectedDay", "textView", "isActive", "syncTimerBreakPresetSelection", "toUtcDateSelection", "updateBlockingStyleUi", "updateEditWindowButtons", "updateLockUntilDisplay", "updateSelectedAppsDisplay", "updateTypeOverUi", "updateUnlockMethodUi", "usageLimitDayToggles", "waitTimerDayToggles", "Companion", "SelectedAppIcon", "SelectedAppsAdapter", "app_debug"})
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
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UNLOCK_WHILE_ACTIVE = "WHILE_ACTIVE";
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.TimeBlockDao timeBlockDao;
    private com.qrzen.app.databinding.ActivityEditBlockBinding binding;
    private android.view.View selectedBlockDetailView;
    private com.google.android.material.button.MaterialButton selectedBlockStartButton;
    private com.google.android.material.button.MaterialButton selectedBlockEndButton;
    private com.google.android.material.button.MaterialButton selectedBlockDeleteButton;
    private java.util.List<? extends android.widget.TextView> selectedDayViews;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock existingBlock;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentQrSecret = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String selectedPackages = "";
    private boolean isAllowlistMode = false;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String blockingStyle = "MANUAL";
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
    @org.jetbrains.annotations.NotNull()
    private java.lang.String activeDays = "1111111";
    private int usageLimitMinutes = 30;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String usageLimitPeriod = "DAILY";
    private int waitTimerWaitMinutes = 30;
    private int waitTimerUseMinutes = 5;
    private int timerBreakMinutes = 0;
    private long lockUntil = 0L;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.qrzen.app.data.model.TimeBlock> currentTimeBlocks;
    private int nextTempId = -1;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job selectedAppsLoadJob;
    private boolean isUpdatingTimerBreakPresets = false;
    @org.jetbrains.annotations.NotNull()
    private final java.time.format.DateTimeFormatter timeFormatter = null;
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
    
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.TimeBlockDao getTimeBlockDao() {
        return null;
    }
    
    public final void setTimeBlockDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.TimeBlockDao p0) {
    }
    
    private final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> getBlockingStyles() {
        return null;
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
    
    private final void setupBlockingStyleDropdown() {
    }
    
    private final void refreshBlockingStyleDropdown() {
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
    
    private final void updateEditWindowButtons() {
    }
    
    private final void updateBlockingStyleUi() {
    }
    
    private final void updateUnlockMethodUi() {
    }
    
    private final void updateTypeOverUi() {
    }
    
    private final void updateLockUntilDisplay() {
    }
    
    private final void showAddTimeBlockDialog() {
    }
    
    private final void showSelectedBlockDetail(com.qrzen.app.data.model.TimeBlock timeBlock) {
    }
    
    private final void hideSelectedBlockDetail() {
    }
    
    private final void highlightSelectedDays(java.lang.String activeDays) {
    }
    
    private final void styleSelectedDay(android.widget.TextView textView, boolean isActive) {
    }
    
    private final void replaceTimeBlock(com.qrzen.app.data.model.TimeBlock updated) {
    }
    
    private final boolean hasTimeBlockOverlap(java.lang.String startTime, java.lang.String endTime, java.lang.String activeDays, java.lang.Integer ignoreBlockId) {
        return false;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> buildSegments(java.lang.String startTime, java.lang.String endTime) {
        return null;
    }
    
    private final boolean rangesOverlap(java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> first, java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> second) {
        return false;
    }
    
    private final void showCustomBreakDurationPicker() {
    }
    
    private final void syncTimerBreakPresetSelection() {
    }
    
    private final void clearTimerBreakPresetSelection() {
    }
    
    private final void saveBlock() {
    }
    
    private final boolean isBlockCurrentlyActive(com.qrzen.app.data.model.AppBlock block) {
        return false;
    }
    
    private final boolean isTimeBlockCurrentlyActive(com.qrzen.app.data.model.TimeBlock timeBlock) {
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
    
    private final java.util.List<android.widget.ToggleButton> editWindowDayToggles() {
        return null;
    }
    
    private final java.util.List<android.widget.ToggleButton> usageLimitDayToggles() {
        return null;
    }
    
    private final java.util.List<android.widget.ToggleButton> waitTimerDayToggles() {
        return null;
    }
    
    private final void normalizeBlockingStyle() {
    }
    
    private final java.lang.String getBlockingStyleLabel(java.lang.String style) {
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
    
    private final int currentDayIndex() {
        return 0;
    }
    
    private final int parseMinutes(java.lang.String value) {
        return 0;
    }
    
    private final java.time.LocalTime parseLocalTime(java.lang.String value) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/qrzen/app/ui/block/EditBlockActivity$Companion;", "", "()V", "EXTRA_BLOCK_ID", "", "EXTRA_IS_ALLOWLIST", "UNLOCK_DELAY", "UNLOCK_EDIT_WINDOW", "UNLOCK_NONE", "UNLOCK_PASSWORD", "UNLOCK_QR_CODE", "UNLOCK_TIMER", "UNLOCK_TYPE_OVER_TEXT", "UNLOCK_WHILE_ACTIVE", "app_debug"})
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