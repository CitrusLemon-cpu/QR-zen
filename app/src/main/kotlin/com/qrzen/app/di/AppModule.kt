package com.qrzen.app.di

import android.content.Context
import androidx.room.Room
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.AppDatabase
import com.qrzen.app.data.db.BlockEventDao
import com.qrzen.app.data.db.TimeBlockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "qrzen.db")
            .addMigrations(
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12
            )
            .fallbackToDestructiveMigration().build()

    @Provides @Singleton
    fun provideAppBlockDao(db: AppDatabase): AppBlockDao = db.appBlockDao()

    @Provides @Singleton
    fun provideBlockEventDao(db: AppDatabase): BlockEventDao = db.blockEventDao()

    @Provides @Singleton
    fun provideTimeBlockDao(db: AppDatabase): TimeBlockDao = db.timeBlockDao()
}
