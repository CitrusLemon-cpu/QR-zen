package com.qrzen.app.di

import android.content.Context
import androidx.room.Room
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.AppDatabase
import com.qrzen.app.data.db.BlockEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "qrzen.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAppBlockDao(db: AppDatabase): AppBlockDao = db.appBlockDao()

    @Provides
    @Singleton
    fun provideBlockEventDao(db: AppDatabase): BlockEventDao = db.blockEventDao()
}
