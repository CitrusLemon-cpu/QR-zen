package com.qrzen.app.ui.pomodoro;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0017\u001a\u00020\u0018H\u0002J\b\u0010\u0019\u001a\u00020\u0018H\u0002J\u0012\u0010\u001a\u001a\u00020\u00182\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0014J\b\u0010\u001d\u001a\u00020\u0018H\u0014J\u0010\u0010\u001e\u001a\u00020\u00182\u0006\u0010\u001f\u001a\u00020 H\u0014J\u0010\u0010!\u001a\u00020\u00182\u0006\u0010\"\u001a\u00020#H\u0002J\u0018\u0010$\u001a\u00020\u00182\u0006\u0010%\u001a\u00020\u00062\u0006\u0010&\u001a\u00020\u000eH\u0002J\u0018\u0010\'\u001a\u00020\u00182\u0006\u0010%\u001a\u00020\u00062\u0006\u0010(\u001a\u00020\u0011H\u0002J\b\u0010)\u001a\u00020\u0018H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/qrzen/app/ui/pomodoro/PomodoroActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/qrzen/app/databinding/ActivityPomodoroBinding;", "currentBlock", "Lcom/qrzen/app/data/model/AppBlock;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "getDao", "()Lcom/qrzen/app/data/db/AppBlockDao;", "setDao", "(Lcom/qrzen/app/data/db/AppBlockDao;)V", "isFocusPhase", "", "isPaused", "remainingMs", "", "sessionCount", "", "timer", "Landroid/os/CountDownTimer;", "totalPhaseMs", "cancelNotification", "", "createNotificationChannel", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onNewIntent", "intent", "Landroid/content/Intent;", "showNotification", "text", "", "startPhase", "block", "focusPhase", "startTimer", "durationMs", "vibrate", "Companion", "app_debug"})
public final class PomodoroActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_BLOCK_ID = "extra_block_id";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "qrzen_pomodoro";
    private static final int NOTIF_ID = 2001;
    @javax.inject.Inject()
    public com.qrzen.app.data.db.AppBlockDao dao;
    private com.qrzen.app.databinding.ActivityPomodoroBinding binding;
    @org.jetbrains.annotations.Nullable()
    private com.qrzen.app.data.model.AppBlock currentBlock;
    private boolean isFocusPhase = true;
    private int sessionCount = 0;
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer timer;
    private boolean isPaused = false;
    private long remainingMs = 0L;
    private long totalPhaseMs = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.ui.pomodoro.PomodoroActivity.Companion Companion = null;
    
    public PomodoroActivity() {
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
    
    private final void startPhase(com.qrzen.app.data.model.AppBlock block, boolean focusPhase) {
    }
    
    private final void startTimer(com.qrzen.app.data.model.AppBlock block, long durationMs) {
    }
    
    private final void vibrate() {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final void showNotification(java.lang.String text) {
    }
    
    private final void cancelNotification() {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/qrzen/app/ui/pomodoro/PomodoroActivity$Companion;", "", "()V", "CHANNEL_ID", "", "EXTRA_BLOCK_ID", "NOTIF_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}