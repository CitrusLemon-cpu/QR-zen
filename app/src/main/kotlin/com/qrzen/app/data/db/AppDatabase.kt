package com.qrzen.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent

@Database(entities = [AppBlock::class, BlockEvent::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockDao(): AppBlockDao
    abstract fun blockEventDao(): BlockEventDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
