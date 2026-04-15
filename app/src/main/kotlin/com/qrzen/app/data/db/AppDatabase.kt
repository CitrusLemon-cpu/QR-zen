package com.qrzen.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qrzen.app.data.model.AppBlock

@Database(entities = [AppBlock::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockDao(): AppBlockDao
}
