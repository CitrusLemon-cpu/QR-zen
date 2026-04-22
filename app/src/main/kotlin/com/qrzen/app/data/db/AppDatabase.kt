package com.qrzen.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent

@Database(entities = [AppBlock::class, BlockEvent::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockDao(): AppBlockDao
    abstract fun blockEventDao(): BlockEventDao
}
