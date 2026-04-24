package com.qrzen.app.ui.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u000b"}, d2 = {"Lcom/qrzen/app/ui/main/StatsViewModel;", "Landroidx/lifecycle/ViewModel;", "dao", "Lcom/qrzen/app/data/db/BlockEventDao;", "(Lcom/qrzen/app/data/db/BlockEventDao;)V", "recentEvents", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/qrzen/app/data/model/BlockEvent;", "getRecentEvents", "()Lkotlinx/coroutines/flow/StateFlow;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class StatsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.qrzen.app.data.db.BlockEventDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.qrzen.app.data.model.BlockEvent>> recentEvents = null;
    
    @javax.inject.Inject()
    public StatsViewModel(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.BlockEventDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.qrzen.app.data.model.BlockEvent>> getRecentEvents() {
        return null;
    }
}