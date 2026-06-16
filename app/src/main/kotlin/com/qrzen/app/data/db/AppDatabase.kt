package com.qrzen.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.data.model.TimeBlock

@Database(entities = [AppBlock::class, BlockEvent::class, BlockFolder::class, TimeBlock::class], version = 17, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockDao(): AppBlockDao
    abstract fun blockEventDao(): BlockEventDao
    abstract fun blockFolderDao(): BlockFolderDao
    abstract fun timeBlockDao(): TimeBlockDao

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN blockNowUntil INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS time_blocks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        blockId INTEGER NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        activeDays TEXT NOT NULL,
                        FOREIGN KEY (blockId) REFERENCES app_blocks(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_time_blocks_blockId ON time_blocks(blockId)")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN blockingStyle TEXT NOT NULL DEFAULT 'SCHEDULE'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN usageLimitMinutes INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN usageLimitPeriod TEXT NOT NULL DEFAULT 'DAILY'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN waitTimerWaitMinutes INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN waitTimerUseMinutes INTEGER NOT NULL DEFAULT 5")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN timerBreakMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    INSERT INTO time_blocks (blockId, startTime, endTime, activeDays)
                    SELECT id, startTime, endTime, activeDays FROM app_blocks
                    WHERE isArchived = 0
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN waitTimerAdaptive INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN showTimer INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN toggleLockUntil INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN autoDisableOnToggleLockExpiry INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN activeUntil INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN pomodoroRoundsTotal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN pomodoroSessionStartMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN pomodoroLockEditing INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN scheduleBreakType TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN scheduledAllowanceMinutes INTEGER NOT NULL DEFAULT 10")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN autoAddNewApps INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS block_folders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL DEFAULT '',
                        isEnabled INTEGER NOT NULL DEFAULT 1,
                        pausedUntil INTEGER NOT NULL DEFAULT 0,
                        isCollapsed INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        unlockMethod TEXT NOT NULL DEFAULT 'NONE',
                        delayMinutes INTEGER NOT NULL DEFAULT 5,
                        blockPassword TEXT NOT NULL DEFAULT '',
                        typeOverText TEXT NOT NULL DEFAULT '',
                        typeOverIsRandom INTEGER NOT NULL DEFAULT 0,
                        editWindowStart TEXT NOT NULL DEFAULT '09:00',
                        editWindowEnd TEXT NOT NULL DEFAULT '10:00',
                        editWindowDays TEXT NOT NULL DEFAULT '1111111',
                        lockUntil INTEGER NOT NULL DEFAULT 0,
                        qrSecret TEXT NOT NULL DEFAULT '',
                        masterPasswordEnabled INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN folderId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN ignoreMasterPassword INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE block_folders ADD COLUMN ignoreMasterPassword INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_blocks ADD COLUMN blockAudio INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
