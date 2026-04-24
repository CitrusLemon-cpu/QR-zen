package com.qrzen.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000f2\u0006\u0010\u0010\u001a\u00020\u0005H\'\u00a8\u0006\u0011"}, d2 = {"Lcom/qrzen/app/data/db/BlockEventDao;", "", "deleteOlderThan", "", "before", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecent", "", "Lcom/qrzen/app/data/model/BlockEvent;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "event", "(Lcom/qrzen/app/data/model/BlockEvent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeSince", "Lkotlinx/coroutines/flow/Flow;", "since", "app_debug"})
@androidx.room.Dao()
public abstract interface BlockEventDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.BlockEvent event, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM block_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.qrzen.app.data.model.BlockEvent>> observeSince(long since);
    
    @androidx.room.Query(value = "SELECT * FROM block_events ORDER BY timestamp DESC LIMIT 50")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getRecent(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.qrzen.app.data.model.BlockEvent>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM block_events WHERE timestamp < :before")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOlderThan(long before, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}