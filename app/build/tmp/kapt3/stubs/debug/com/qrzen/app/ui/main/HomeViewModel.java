package com.qrzen.app.ui.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\fJ\u0016\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\fJ\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0011\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014J\u0016\u0010\u001a\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\u0017J\u000e\u0010\u001c\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\fR\u001d\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/qrzen/app/ui/main/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "dao", "Lcom/qrzen/app/data/db/AppBlockDao;", "timeBlockDao", "Lcom/qrzen/app/data/db/TimeBlockDao;", "ctx", "Landroid/content/Context;", "(Lcom/qrzen/app/data/db/AppBlockDao;Lcom/qrzen/app/data/db/TimeBlockDao;Landroid/content/Context;)V", "blocks", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/qrzen/app/data/model/AppBlock;", "getBlocks", "()Lkotlinx/coroutines/flow/StateFlow;", "archive", "Lkotlinx/coroutines/Job;", "block", "blockNow", "durationMs", "", "delete", "isBlockCurrentlyActive", "", "(Lcom/qrzen/app/data/model/AppBlock;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pause", "setEnabled", "enabled", "unpause", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.qrzen.app.data.db.AppBlockDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.qrzen.app.data.db.TimeBlockDao timeBlockDao = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.qrzen.app.data.model.AppBlock>> blocks = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppBlockDao dao, @org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.TimeBlockDao timeBlockDao, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.qrzen.app.data.model.AppBlock>> getBlocks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job delete(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job setEnabled(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, boolean enabled) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job pause(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, long durationMs) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job unpause(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job blockNow(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, long durationMs) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job archive(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object isBlockCurrentlyActive(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}