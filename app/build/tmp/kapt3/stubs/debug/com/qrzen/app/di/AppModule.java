package com.qrzen.app.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\t\u001a\u00020\u00062\b\b\u0001\u0010\n\u001a\u00020\u000bH\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000e"}, d2 = {"Lcom/qrzen/app/di/AppModule;", "", "()V", "provideAppBlockDao", "Lcom/qrzen/app/data/db/AppBlockDao;", "db", "Lcom/qrzen/app/data/db/AppDatabase;", "provideBlockEventDao", "Lcom/qrzen/app/data/db/BlockEventDao;", "provideDatabase", "ctx", "Landroid/content/Context;", "provideTimeBlockDao", "Lcom/qrzen/app/data/db/TimeBlockDao;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.qrzen.app.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.AppBlockDao provideAppBlockDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.BlockEventDao provideBlockEventDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.qrzen.app.data.db.TimeBlockDao provideTimeBlockDao(@org.jetbrains.annotations.NotNull()
    com.qrzen.app.data.db.AppDatabase db) {
        return null;
    }
}