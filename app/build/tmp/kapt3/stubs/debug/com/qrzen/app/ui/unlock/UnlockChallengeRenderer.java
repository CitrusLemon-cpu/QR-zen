package com.qrzen.app.ui.unlock;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0012\u001a\u00020\rJ\u0010\u0010\u0013\u001a\u00020\r2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0011J\b\u0010\u0015\u001a\u00020\rH\u0002JD\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0010\b\u0002\u0010\u001c\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fJ\u0010\u0010\u001d\u001a\u00020\r2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u001e\u0010 \u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J7\u0010!\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\b\u0010\"\u001a\u0004\u0018\u00010#2\u0006\u0010\u0018\u001a\u00020\u00192\u000e\u0010\u001c\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fH\u0002\u00a2\u0006\u0002\u0010$J\u0010\u0010%\u001a\u00020\r2\u0006\u0010&\u001a\u00020\u0011H\u0002J\u0016\u0010\'\u001a\u00020\r2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J\u001e\u0010(\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J,\u0010)\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J(\u0010*\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u00192\u000e\u0010\u001c\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fH\u0002J\u001e\u0010+\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\n2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J\u0018\u0010,\u001a\u00020\r2\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020#H\u0002J\u0018\u00100\u001a\u00020\r2\u0006\u0010-\u001a\u0002012\u0006\u00102\u001a\u00020#H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00063"}, d2 = {"Lcom/qrzen/app/ui/unlock/UnlockChallengeRenderer;", "", "activity", "Landroidx/fragment/app/FragmentActivity;", "container", "Landroid/widget/FrameLayout;", "errorView", "Landroid/widget/TextView;", "(Landroidx/fragment/app/FragmentActivity;Landroid/widget/FrameLayout;Landroid/widget/TextView;)V", "currentQrBlock", "Lcom/qrzen/app/data/model/AppBlock;", "onQrSuccess", "Lkotlin/Function0;", "", "timer", "Landroid/os/CountDownTimer;", "typeOverSessionText", "", "clear", "handleQrScanResult", "scannedText", "hideError", "render", "block", "showGoBackButton", "", "onRequestQrScan", "onUnlocked", "onGoBack", "setupErrorClearing", "editText", "Lcom/google/android/material/textfield/TextInputEditText;", "showDelay", "showEditWindowInfo", "nextAvailableMillis", "", "(Lcom/qrzen/app/data/model/AppBlock;Ljava/lang/Long;ZLkotlin/jvm/functions/Function0;)V", "showError", "message", "showNoneChallenge", "showPassword", "showQr", "showTimerInfo", "showTypeOverText", "updateDelayViews", "binding", "Lcom/qrzen/app/databinding/ViewUnlockDelayBinding;", "millis", "updateTimerMessage", "Lcom/qrzen/app/databinding/ViewUnlockInfoBinding;", "millisUntilFinished", "app_debug"})
public final class UnlockChallengeRenderer {
    @org.jetbrains.annotations.NotNull()
    private final androidx.fragment.app.FragmentActivity activity = null;
    @org.jetbrains.annotations.NotNull()
    private final android.widget.FrameLayout container = null;
    @org.jetbrains.annotations.NotNull()
    private final android.widget.TextView errorView = null;
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer timer;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock currentQrBlock;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onQrSuccess;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String typeOverSessionText;
    
    public UnlockChallengeRenderer(@org.jetbrains.annotations.NotNull()
    androidx.fragment.app.FragmentActivity activity, @org.jetbrains.annotations.NotNull()
    android.widget.FrameLayout container, @org.jetbrains.annotations.NotNull()
    android.widget.TextView errorView) {
        super();
    }
    
    public final void render(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, boolean showGoBackButton, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRequestQrScan, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> onGoBack) {
    }
    
    private final void showNoneChallenge(kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked) {
    }
    
    public final void handleQrScanResult(@org.jetbrains.annotations.Nullable()
    java.lang.String scannedText) {
    }
    
    public final void clear() {
    }
    
    private final void showDelay(com.qrzen.app.data.model.AppBlock block, kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked) {
    }
    
    private final void updateDelayViews(com.qrzen.app.databinding.ViewUnlockDelayBinding binding, long millis) {
    }
    
    private final void showPassword(com.qrzen.app.data.model.AppBlock block, kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked) {
    }
    
    private final void showTypeOverText(com.qrzen.app.data.model.AppBlock block, kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked) {
    }
    
    private final void showQr(com.qrzen.app.data.model.AppBlock block, kotlin.jvm.functions.Function0<kotlin.Unit> onRequestQrScan, kotlin.jvm.functions.Function0<kotlin.Unit> onUnlocked) {
    }
    
    private final void showEditWindowInfo(com.qrzen.app.data.model.AppBlock block, java.lang.Long nextAvailableMillis, boolean showGoBackButton, kotlin.jvm.functions.Function0<kotlin.Unit> onGoBack) {
    }
    
    private final void showTimerInfo(com.qrzen.app.data.model.AppBlock block, boolean showGoBackButton, kotlin.jvm.functions.Function0<kotlin.Unit> onGoBack) {
    }
    
    private final void updateTimerMessage(com.qrzen.app.databinding.ViewUnlockInfoBinding binding, long millisUntilFinished) {
    }
    
    private final void setupErrorClearing(com.google.android.material.textfield.TextInputEditText editText) {
    }
    
    private final void showError(java.lang.String message) {
    }
    
    private final void hideError() {
    }
}