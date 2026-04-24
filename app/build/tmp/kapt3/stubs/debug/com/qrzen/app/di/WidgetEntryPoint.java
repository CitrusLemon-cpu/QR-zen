package com.qrzen.app.di;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lcom/qrzen/app/di/WidgetEntryPoint;", "", "appBlockDao", "Lcom/qrzen/app/data/db/AppBlockDao;", "blockEventDao", "Lcom/qrzen/app/data/db/BlockEventDao;", "app_debug"})
@dagger.hilt.EntryPoint()
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract interface WidgetEntryPoint {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.qrzen.app.data.db.AppBlockDao appBlockDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.qrzen.app.data.db.BlockEventDao blockEventDao();
}