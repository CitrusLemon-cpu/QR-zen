package com.qrzen.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.qrzen.app.data.model.AppBlock;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppBlockDao_Impl implements AppBlockDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppBlock> __insertionAdapterOfAppBlock;

  private final EntityDeletionOrUpdateAdapter<AppBlock> __deletionAdapterOfAppBlock;

  private final EntityDeletionOrUpdateAdapter<AppBlock> __updateAdapterOfAppBlock;

  private final SharedSQLiteStatement __preparedStmtOfSetPausedUntil;

  private final SharedSQLiteStatement __preparedStmtOfSetArchived;

  public AppBlockDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppBlock = new EntityInsertionAdapter<AppBlock>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_blocks` (`id`,`title`,`appPackages`,`isAllowlistMode`,`startTime`,`endTime`,`activeDays`,`qrSecret`,`unlockMethod`,`delayMinutes`,`blockPassword`,`typeOverText`,`typeOverIsRandom`,`editWindowStart`,`editWindowEnd`,`editWindowDays`,`lockUntil`,`masterPasswordEnabled`,`pausedUntil`,`blockNowUntil`,`isEnabled`,`isPomodoroBlock`,`pomodoroDurationMin`,`pomodoroBreakMin`,`isArchived`,`blockingStyle`,`usageLimitMinutes`,`usageLimitPeriod`,`waitTimerWaitMinutes`,`waitTimerUseMinutes`,`timerBreakMinutes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppBlock entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getAppPackages() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAppPackages());
        }
        final int _tmp = entity.isAllowlistMode() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getStartTime() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEndTime());
        }
        if (entity.getActiveDays() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getActiveDays());
        }
        if (entity.getQrSecret() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getQrSecret());
        }
        if (entity.getUnlockMethod() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUnlockMethod());
        }
        statement.bindLong(10, entity.getDelayMinutes());
        if (entity.getBlockPassword() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getBlockPassword());
        }
        if (entity.getTypeOverText() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getTypeOverText());
        }
        final int _tmp_1 = entity.getTypeOverIsRandom() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getEditWindowStart() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEditWindowStart());
        }
        if (entity.getEditWindowEnd() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getEditWindowEnd());
        }
        if (entity.getEditWindowDays() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getEditWindowDays());
        }
        statement.bindLong(17, entity.getLockUntil());
        final int _tmp_2 = entity.getMasterPasswordEnabled() ? 1 : 0;
        statement.bindLong(18, _tmp_2);
        statement.bindLong(19, entity.getPausedUntil());
        statement.bindLong(20, entity.getBlockNowUntil());
        final int _tmp_3 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(21, _tmp_3);
        final int _tmp_4 = entity.isPomodoroBlock() ? 1 : 0;
        statement.bindLong(22, _tmp_4);
        statement.bindLong(23, entity.getPomodoroDurationMin());
        statement.bindLong(24, entity.getPomodoroBreakMin());
        final int _tmp_5 = entity.isArchived() ? 1 : 0;
        statement.bindLong(25, _tmp_5);
        if (entity.getBlockingStyle() == null) {
          statement.bindNull(26);
        } else {
          statement.bindString(26, entity.getBlockingStyle());
        }
        statement.bindLong(27, entity.getUsageLimitMinutes());
        if (entity.getUsageLimitPeriod() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getUsageLimitPeriod());
        }
        statement.bindLong(29, entity.getWaitTimerWaitMinutes());
        statement.bindLong(30, entity.getWaitTimerUseMinutes());
        statement.bindLong(31, entity.getTimerBreakMinutes());
      }
    };
    this.__deletionAdapterOfAppBlock = new EntityDeletionOrUpdateAdapter<AppBlock>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `app_blocks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppBlock entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAppBlock = new EntityDeletionOrUpdateAdapter<AppBlock>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `app_blocks` SET `id` = ?,`title` = ?,`appPackages` = ?,`isAllowlistMode` = ?,`startTime` = ?,`endTime` = ?,`activeDays` = ?,`qrSecret` = ?,`unlockMethod` = ?,`delayMinutes` = ?,`blockPassword` = ?,`typeOverText` = ?,`typeOverIsRandom` = ?,`editWindowStart` = ?,`editWindowEnd` = ?,`editWindowDays` = ?,`lockUntil` = ?,`masterPasswordEnabled` = ?,`pausedUntil` = ?,`blockNowUntil` = ?,`isEnabled` = ?,`isPomodoroBlock` = ?,`pomodoroDurationMin` = ?,`pomodoroBreakMin` = ?,`isArchived` = ?,`blockingStyle` = ?,`usageLimitMinutes` = ?,`usageLimitPeriod` = ?,`waitTimerWaitMinutes` = ?,`waitTimerUseMinutes` = ?,`timerBreakMinutes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppBlock entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getAppPackages() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAppPackages());
        }
        final int _tmp = entity.isAllowlistMode() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getStartTime() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEndTime());
        }
        if (entity.getActiveDays() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getActiveDays());
        }
        if (entity.getQrSecret() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getQrSecret());
        }
        if (entity.getUnlockMethod() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUnlockMethod());
        }
        statement.bindLong(10, entity.getDelayMinutes());
        if (entity.getBlockPassword() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getBlockPassword());
        }
        if (entity.getTypeOverText() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getTypeOverText());
        }
        final int _tmp_1 = entity.getTypeOverIsRandom() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getEditWindowStart() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEditWindowStart());
        }
        if (entity.getEditWindowEnd() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getEditWindowEnd());
        }
        if (entity.getEditWindowDays() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getEditWindowDays());
        }
        statement.bindLong(17, entity.getLockUntil());
        final int _tmp_2 = entity.getMasterPasswordEnabled() ? 1 : 0;
        statement.bindLong(18, _tmp_2);
        statement.bindLong(19, entity.getPausedUntil());
        statement.bindLong(20, entity.getBlockNowUntil());
        final int _tmp_3 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(21, _tmp_3);
        final int _tmp_4 = entity.isPomodoroBlock() ? 1 : 0;
        statement.bindLong(22, _tmp_4);
        statement.bindLong(23, entity.getPomodoroDurationMin());
        statement.bindLong(24, entity.getPomodoroBreakMin());
        final int _tmp_5 = entity.isArchived() ? 1 : 0;
        statement.bindLong(25, _tmp_5);
        if (entity.getBlockingStyle() == null) {
          statement.bindNull(26);
        } else {
          statement.bindString(26, entity.getBlockingStyle());
        }
        statement.bindLong(27, entity.getUsageLimitMinutes());
        if (entity.getUsageLimitPeriod() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getUsageLimitPeriod());
        }
        statement.bindLong(29, entity.getWaitTimerWaitMinutes());
        statement.bindLong(30, entity.getWaitTimerUseMinutes());
        statement.bindLong(31, entity.getTimerBreakMinutes());
        statement.bindLong(32, entity.getId());
      }
    };
    this.__preparedStmtOfSetPausedUntil = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_blocks SET pausedUntil = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetArchived = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_blocks SET isArchived = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AppBlock block, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAppBlock.insertAndReturnId(block);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AppBlock block, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAppBlock.handle(block);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AppBlock block, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppBlock.handle(block);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setPausedUntil(final int id, final long until,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetPausedUntil.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, until);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetPausedUntil.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setArchived(final int id, final boolean archived,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetArchived.acquire();
        int _argIndex = 1;
        final int _tmp = archived ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetArchived.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AppBlock>> observeAll() {
    final String _sql = "SELECT * FROM app_blocks WHERE isArchived = 0 ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_blocks"}, new Callable<List<AppBlock>>() {
      @Override
      @NonNull
      public List<AppBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final List<AppBlock> _result = new ArrayList<AppBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _item = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<AppBlock>> observeActive() {
    final String _sql = "SELECT * FROM app_blocks WHERE isEnabled = 1 AND isArchived = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_blocks"}, new Callable<List<AppBlock>>() {
      @Override
      @NonNull
      public List<AppBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final List<AppBlock> _result = new ArrayList<AppBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _item = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<AppBlock>> observeArchived() {
    final String _sql = "SELECT * FROM app_blocks WHERE isArchived = 1 ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_blocks"}, new Callable<List<AppBlock>>() {
      @Override
      @NonNull
      public List<AppBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final List<AppBlock> _result = new ArrayList<AppBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _item = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final int id, final Continuation<? super AppBlock> $completion) {
    final String _sql = "SELECT * FROM app_blocks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppBlock>() {
      @Override
      @Nullable
      public AppBlock call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final AppBlock _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _result = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getByQrSecret(final String secret,
      final Continuation<? super AppBlock> $completion) {
    final String _sql = "SELECT * FROM app_blocks WHERE qrSecret = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (secret == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, secret);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppBlock>() {
      @Override
      @Nullable
      public AppBlock call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final AppBlock _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _result = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<AppBlock>> $completion) {
    final String _sql = "SELECT * FROM app_blocks WHERE isArchived = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppBlock>>() {
      @Override
      @NonNull
      public List<AppBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAppPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackages");
          final int _cursorIndexOfIsAllowlistMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowlistMode");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final int _cursorIndexOfQrSecret = CursorUtil.getColumnIndexOrThrow(_cursor, "qrSecret");
          final int _cursorIndexOfUnlockMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockMethod");
          final int _cursorIndexOfDelayMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "delayMinutes");
          final int _cursorIndexOfBlockPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "blockPassword");
          final int _cursorIndexOfTypeOverText = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverText");
          final int _cursorIndexOfTypeOverIsRandom = CursorUtil.getColumnIndexOrThrow(_cursor, "typeOverIsRandom");
          final int _cursorIndexOfEditWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowStart");
          final int _cursorIndexOfEditWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowEnd");
          final int _cursorIndexOfEditWindowDays = CursorUtil.getColumnIndexOrThrow(_cursor, "editWindowDays");
          final int _cursorIndexOfLockUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "lockUntil");
          final int _cursorIndexOfMasterPasswordEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "masterPasswordEnabled");
          final int _cursorIndexOfPausedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedUntil");
          final int _cursorIndexOfBlockNowUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "blockNowUntil");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPomodoroBlock = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoroBlock");
          final int _cursorIndexOfPomodoroDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroDurationMin");
          final int _cursorIndexOfPomodoroBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "pomodoroBreakMin");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfBlockingStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockingStyle");
          final int _cursorIndexOfUsageLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitMinutes");
          final int _cursorIndexOfUsageLimitPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "usageLimitPeriod");
          final int _cursorIndexOfWaitTimerWaitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerWaitMinutes");
          final int _cursorIndexOfWaitTimerUseMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "waitTimerUseMinutes");
          final int _cursorIndexOfTimerBreakMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timerBreakMinutes");
          final List<AppBlock> _result = new ArrayList<AppBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpAppPackages;
            if (_cursor.isNull(_cursorIndexOfAppPackages)) {
              _tmpAppPackages = null;
            } else {
              _tmpAppPackages = _cursor.getString(_cursorIndexOfAppPackages);
            }
            final boolean _tmpIsAllowlistMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowlistMode);
            _tmpIsAllowlistMode = _tmp != 0;
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            final String _tmpActiveDays;
            if (_cursor.isNull(_cursorIndexOfActiveDays)) {
              _tmpActiveDays = null;
            } else {
              _tmpActiveDays = _cursor.getString(_cursorIndexOfActiveDays);
            }
            final String _tmpQrSecret;
            if (_cursor.isNull(_cursorIndexOfQrSecret)) {
              _tmpQrSecret = null;
            } else {
              _tmpQrSecret = _cursor.getString(_cursorIndexOfQrSecret);
            }
            final String _tmpUnlockMethod;
            if (_cursor.isNull(_cursorIndexOfUnlockMethod)) {
              _tmpUnlockMethod = null;
            } else {
              _tmpUnlockMethod = _cursor.getString(_cursorIndexOfUnlockMethod);
            }
            final int _tmpDelayMinutes;
            _tmpDelayMinutes = _cursor.getInt(_cursorIndexOfDelayMinutes);
            final String _tmpBlockPassword;
            if (_cursor.isNull(_cursorIndexOfBlockPassword)) {
              _tmpBlockPassword = null;
            } else {
              _tmpBlockPassword = _cursor.getString(_cursorIndexOfBlockPassword);
            }
            final String _tmpTypeOverText;
            if (_cursor.isNull(_cursorIndexOfTypeOverText)) {
              _tmpTypeOverText = null;
            } else {
              _tmpTypeOverText = _cursor.getString(_cursorIndexOfTypeOverText);
            }
            final boolean _tmpTypeOverIsRandom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfTypeOverIsRandom);
            _tmpTypeOverIsRandom = _tmp_1 != 0;
            final String _tmpEditWindowStart;
            if (_cursor.isNull(_cursorIndexOfEditWindowStart)) {
              _tmpEditWindowStart = null;
            } else {
              _tmpEditWindowStart = _cursor.getString(_cursorIndexOfEditWindowStart);
            }
            final String _tmpEditWindowEnd;
            if (_cursor.isNull(_cursorIndexOfEditWindowEnd)) {
              _tmpEditWindowEnd = null;
            } else {
              _tmpEditWindowEnd = _cursor.getString(_cursorIndexOfEditWindowEnd);
            }
            final String _tmpEditWindowDays;
            if (_cursor.isNull(_cursorIndexOfEditWindowDays)) {
              _tmpEditWindowDays = null;
            } else {
              _tmpEditWindowDays = _cursor.getString(_cursorIndexOfEditWindowDays);
            }
            final long _tmpLockUntil;
            _tmpLockUntil = _cursor.getLong(_cursorIndexOfLockUntil);
            final boolean _tmpMasterPasswordEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMasterPasswordEnabled);
            _tmpMasterPasswordEnabled = _tmp_2 != 0;
            final long _tmpPausedUntil;
            _tmpPausedUntil = _cursor.getLong(_cursorIndexOfPausedUntil);
            final long _tmpBlockNowUntil;
            _tmpBlockNowUntil = _cursor.getLong(_cursorIndexOfBlockNowUntil);
            final boolean _tmpIsEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_3 != 0;
            final boolean _tmpIsPomodoroBlock;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsPomodoroBlock);
            _tmpIsPomodoroBlock = _tmp_4 != 0;
            final int _tmpPomodoroDurationMin;
            _tmpPomodoroDurationMin = _cursor.getInt(_cursorIndexOfPomodoroDurationMin);
            final int _tmpPomodoroBreakMin;
            _tmpPomodoroBreakMin = _cursor.getInt(_cursorIndexOfPomodoroBreakMin);
            final boolean _tmpIsArchived;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_5 != 0;
            final String _tmpBlockingStyle;
            if (_cursor.isNull(_cursorIndexOfBlockingStyle)) {
              _tmpBlockingStyle = null;
            } else {
              _tmpBlockingStyle = _cursor.getString(_cursorIndexOfBlockingStyle);
            }
            final int _tmpUsageLimitMinutes;
            _tmpUsageLimitMinutes = _cursor.getInt(_cursorIndexOfUsageLimitMinutes);
            final String _tmpUsageLimitPeriod;
            if (_cursor.isNull(_cursorIndexOfUsageLimitPeriod)) {
              _tmpUsageLimitPeriod = null;
            } else {
              _tmpUsageLimitPeriod = _cursor.getString(_cursorIndexOfUsageLimitPeriod);
            }
            final int _tmpWaitTimerWaitMinutes;
            _tmpWaitTimerWaitMinutes = _cursor.getInt(_cursorIndexOfWaitTimerWaitMinutes);
            final int _tmpWaitTimerUseMinutes;
            _tmpWaitTimerUseMinutes = _cursor.getInt(_cursorIndexOfWaitTimerUseMinutes);
            final int _tmpTimerBreakMinutes;
            _tmpTimerBreakMinutes = _cursor.getInt(_cursorIndexOfTimerBreakMinutes);
            _item = new AppBlock(_tmpId,_tmpTitle,_tmpAppPackages,_tmpIsAllowlistMode,_tmpStartTime,_tmpEndTime,_tmpActiveDays,_tmpQrSecret,_tmpUnlockMethod,_tmpDelayMinutes,_tmpBlockPassword,_tmpTypeOverText,_tmpTypeOverIsRandom,_tmpEditWindowStart,_tmpEditWindowEnd,_tmpEditWindowDays,_tmpLockUntil,_tmpMasterPasswordEnabled,_tmpPausedUntil,_tmpBlockNowUntil,_tmpIsEnabled,_tmpIsPomodoroBlock,_tmpPomodoroDurationMin,_tmpPomodoroBreakMin,_tmpIsArchived,_tmpBlockingStyle,_tmpUsageLimitMinutes,_tmpUsageLimitPeriod,_tmpWaitTimerWaitMinutes,_tmpWaitTimerUseMinutes,_tmpTimerBreakMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
