package com.qrzen.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent

@Database(entities = [AppBlock::class, BlockEvent::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockDao(): AppBlockDao
    abstract fun blockEventDao(): BlockEventDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN unlockMethod TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN delayMinutes INTEGER NOT NULL DEFAULT 5")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN blockPassword TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN typeOverText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN typeOverIsRandom INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN editWindowStart TEXT NOT NULL DEFAULT '09:00'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN editWindowEnd TEXT NOT NULL DEFAULT '10:00'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN editWindowDays TEXT NOT NULL DEFAULT '1111111'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN lockUntil INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
