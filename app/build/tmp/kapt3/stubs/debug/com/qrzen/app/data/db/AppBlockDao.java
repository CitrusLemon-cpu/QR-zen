package com.qrzen.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0018\u0010\n\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000f\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\b0\u0015H\'J\u0014\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\b0\u0015H\'J\u0014\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\b0\u0015H\'J\u001e\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\u001aH\u00a7@\u00a2\u0006\u0002\u0010\u001bJ\u001e\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u0016\u0010\u001f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006 "}, d2 = {"Lcom/qrzen/app/data/db/AppBlockDao;", "", "delete", "", "block", "Lcom/qrzen/app/data/model/AppBlock;", "(Lcom/qrzen/app/data/model/AppBlock;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getByQrSecret", "secret", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "", "observeActive", "Lkotlinx/coroutines/flow/Flow;", "observeAll", "observeArchived", "setArchived", "archived", "", "(IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setPausedUntil", "until", "(IJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "update", "app_debug"})
@androidx.room.Dao()
public abstract interface AppBlockDao {
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE isArchived = 0 ORDER BY id ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.qrzen.app.data.model.AppBlock>> observeAll();
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE isEnabled = 1 AND isArchived = 0")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.qrzen.app.data.model.AppBlock>> observeActive();
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE isArchived = 1 ORDER BY id ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.qrzen.app.data.model.AppBlock>> observeArchived();
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.qrzen.app.data.model.AppBlock> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE qrSecret = :secret LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getByQrSecret(@org.jetbrains.annotations.NotNull()
    java.lang.String secret, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.qrzen.app.data.model.AppBlock> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.model.AppBlock block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE app_blocks SET pausedUntil = :until WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setPausedUntil(int id, long until, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE app_blocks SET isArchived = :archived WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setArchived(int id, boolean archived, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM app_blocks WHERE isArchived = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.qrzen.app.data.model.AppBlock>> $completion);
}