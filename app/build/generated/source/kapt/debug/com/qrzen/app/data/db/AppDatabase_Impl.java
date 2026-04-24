package com.qrzen.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile AppBlockDao _appBlockDao;

  private volatile BlockEventDao _blockEventDao;

  private volatile TimeBlockDao _timeBlockDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(8) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_blocks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `appPackages` TEXT NOT NULL, `isAllowlistMode` INTEGER NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `activeDays` TEXT NOT NULL, `qrSecret` TEXT NOT NULL, `unlockMethod` TEXT NOT NULL, `delayMinutes` INTEGER NOT NULL, `blockPassword` TEXT NOT NULL, `typeOverText` TEXT NOT NULL, `typeOverIsRandom` INTEGER NOT NULL, `editWindowStart` TEXT NOT NULL, `editWindowEnd` TEXT NOT NULL, `editWindowDays` TEXT NOT NULL, `lockUntil` INTEGER NOT NULL, `masterPasswordEnabled` INTEGER NOT NULL, `pausedUntil` INTEGER NOT NULL, `blockNowUntil` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `isPomodoroBlock` INTEGER NOT NULL, `pomodoroDurationMin` INTEGER NOT NULL, `pomodoroBreakMin` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, `blockingStyle` TEXT NOT NULL, `usageLimitMinutes` INTEGER NOT NULL, `usageLimitPeriod` TEXT NOT NULL, `waitTimerWaitMinutes` INTEGER NOT NULL, `waitTimerUseMinutes` INTEGER NOT NULL, `timerBreakMinutes` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `block_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `blockId` INTEGER NOT NULL, `blockTitle` TEXT NOT NULL, `packageName` TEXT NOT NULL, `eventType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `time_blocks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `blockId` INTEGER NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `activeDays` TEXT NOT NULL, FOREIGN KEY(`blockId`) REFERENCES `app_blocks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_blockId` ON `time_blocks` (`blockId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ca80be3f9cc02a269bbcb9fc0861979c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `app_blocks`");
        db.execSQL("DROP TABLE IF EXISTS `block_events`");
        db.execSQL("DROP TABLE IF EXISTS `time_blocks`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAppBlocks = new HashMap<String, TableInfo.Column>(31);
        _columnsAppBlocks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("appPackages", new TableInfo.Column("appPackages", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("isAllowlistMode", new TableInfo.Column("isAllowlistMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("endTime", new TableInfo.Column("endTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("activeDays", new TableInfo.Column("activeDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("qrSecret", new TableInfo.Column("qrSecret", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("unlockMethod", new TableInfo.Column("unlockMethod", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("delayMinutes", new TableInfo.Column("delayMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("blockPassword", new TableInfo.Column("blockPassword", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("typeOverText", new TableInfo.Column("typeOverText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("typeOverIsRandom", new TableInfo.Column("typeOverIsRandom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("editWindowStart", new TableInfo.Column("editWindowStart", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("editWindowEnd", new TableInfo.Column("editWindowEnd", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("editWindowDays", new TableInfo.Column("editWindowDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("lockUntil", new TableInfo.Column("lockUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("masterPasswordEnabled", new TableInfo.Column("masterPasswordEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("pausedUntil", new TableInfo.Column("pausedUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("blockNowUntil", new TableInfo.Column("blockNowUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("isPomodoroBlock", new TableInfo.Column("isPomodoroBlock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("pomodoroDurationMin", new TableInfo.Column("pomodoroDurationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("pomodoroBreakMin", new TableInfo.Column("pomodoroBreakMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("isArchived", new TableInfo.Column("isArchived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("blockingStyle", new TableInfo.Column("blockingStyle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("usageLimitMinutes", new TableInfo.Column("usageLimitMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("usageLimitPeriod", new TableInfo.Column("usageLimitPeriod", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("waitTimerWaitMinutes", new TableInfo.Column("waitTimerWaitMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("waitTimerUseMinutes", new TableInfo.Column("waitTimerUseMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppBlocks.put("timerBreakMinutes", new TableInfo.Column("timerBreakMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppBlocks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppBlocks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppBlocks = new TableInfo("app_blocks", _columnsAppBlocks, _foreignKeysAppBlocks, _indicesAppBlocks);
        final TableInfo _existingAppBlocks = TableInfo.read(db, "app_blocks");
        if (!_infoAppBlocks.equals(_existingAppBlocks)) {
          return new RoomOpenHelper.ValidationResult(false, "app_blocks(com.qrzen.app.data.model.AppBlock).\n"
                  + " Expected:\n" + _infoAppBlocks + "\n"
                  + " Found:\n" + _existingAppBlocks);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockEvents = new HashMap<String, TableInfo.Column>(6);
        _columnsBlockEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockEvents.put("blockId", new TableInfo.Column("blockId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockEvents.put("blockTitle", new TableInfo.Column("blockTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockEvents.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockEvents.put("eventType", new TableInfo.Column("eventType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockEvents = new TableInfo("block_events", _columnsBlockEvents, _foreignKeysBlockEvents, _indicesBlockEvents);
        final TableInfo _existingBlockEvents = TableInfo.read(db, "block_events");
        if (!_infoBlockEvents.equals(_existingBlockEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "block_events(com.qrzen.app.data.model.BlockEvent).\n"
                  + " Expected:\n" + _infoBlockEvents + "\n"
                  + " Found:\n" + _existingBlockEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsTimeBlocks = new HashMap<String, TableInfo.Column>(5);
        _columnsTimeBlocks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("blockId", new TableInfo.Column("blockId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("endTime", new TableInfo.Column("endTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("activeDays", new TableInfo.Column("activeDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTimeBlocks = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTimeBlocks.add(new TableInfo.ForeignKey("app_blocks", "CASCADE", "NO ACTION", Arrays.asList("blockId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTimeBlocks = new HashSet<TableInfo.Index>(1);
        _indicesTimeBlocks.add(new TableInfo.Index("index_time_blocks_blockId", false, Arrays.asList("blockId"), Arrays.asList("ASC")));
        final TableInfo _infoTimeBlocks = new TableInfo("time_blocks", _columnsTimeBlocks, _foreignKeysTimeBlocks, _indicesTimeBlocks);
        final TableInfo _existingTimeBlocks = TableInfo.read(db, "time_blocks");
        if (!_infoTimeBlocks.equals(_existingTimeBlocks)) {
          return new RoomOpenHelper.ValidationResult(false, "time_blocks(com.qrzen.app.data.model.TimeBlock).\n"
                  + " Expected:\n" + _infoTimeBlocks + "\n"
                  + " Found:\n" + _existingTimeBlocks);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ca80be3f9cc02a269bbcb9fc0861979c", "69b6a47062f09f975d6bab07a4a71046");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "app_blocks","block_events","time_blocks");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `app_blocks`");
      _db.execSQL("DELETE FROM `block_events`");
      _db.execSQL("DELETE FROM `time_blocks`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AppBlockDao.class, AppBlockDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BlockEventDao.class, BlockEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TimeBlockDao.class, TimeBlockDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AppBlockDao appBlockDao() {
    if (_appBlockDao != null) {
      return _appBlockDao;
    } else {
      synchronized(this) {
        if(_appBlockDao == null) {
          _appBlockDao = new AppBlockDao_Impl(this);
        }
        return _appBlockDao;
      }
    }
  }

  @Override
  public BlockEventDao blockEventDao() {
    if (_blockEventDao != null) {
      return _blockEventDao;
    } else {
      synchronized(this) {
        if(_blockEventDao == null) {
          _blockEventDao = new BlockEventDao_Impl(this);
        }
        return _blockEventDao;
      }
    }
  }

  @Override
  public TimeBlockDao timeBlockDao() {
    if (_timeBlockDao != null) {
      return _timeBlockDao;
    } else {
      synchronized(this) {
        if(_timeBlockDao == null) {
          _timeBlockDao = new TimeBlockDao_Impl(this);
        }
        return _timeBlockDao;
      }
    }
  }
}
