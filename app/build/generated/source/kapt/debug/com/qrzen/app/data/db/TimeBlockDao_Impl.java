package com.qrzen.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.qrzen.app.data.model.TimeBlock;
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
public final class TimeBlockDao_Impl implements TimeBlockDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimeBlock> __insertionAdapterOfTimeBlock;

  private final EntityDeletionOrUpdateAdapter<TimeBlock> __deletionAdapterOfTimeBlock;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByBlockId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public TimeBlockDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimeBlock = new EntityInsertionAdapter<TimeBlock>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `time_blocks` (`id`,`blockId`,`startTime`,`endTime`,`activeDays`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeBlock entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBlockId());
        if (entity.getStartTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEndTime());
        }
        if (entity.getActiveDays() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getActiveDays());
        }
      }
    };
    this.__deletionAdapterOfTimeBlock = new EntityDeletionOrUpdateAdapter<TimeBlock>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `time_blocks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeBlock entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteByBlockId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM time_blocks WHERE blockId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM time_blocks WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TimeBlock block, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTimeBlock.insertAndReturnId(block);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<TimeBlock> blocks,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTimeBlock.insert(blocks);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TimeBlock block, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTimeBlock.handle(block);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByBlockId(final int blockId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByBlockId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, blockId);
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
          __preparedStmtOfDeleteByBlockId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TimeBlock>> observeByBlockId(final int blockId) {
    final String _sql = "SELECT * FROM time_blocks WHERE blockId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, blockId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_blocks"}, new Callable<List<TimeBlock>>() {
      @Override
      @NonNull
      public List<TimeBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBlockId = CursorUtil.getColumnIndexOrThrow(_cursor, "blockId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final List<TimeBlock> _result = new ArrayList<TimeBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBlockId;
            _tmpBlockId = _cursor.getInt(_cursorIndexOfBlockId);
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
            _item = new TimeBlock(_tmpId,_tmpBlockId,_tmpStartTime,_tmpEndTime,_tmpActiveDays);
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
  public Object getByBlockId(final int blockId,
      final Continuation<? super List<TimeBlock>> $completion) {
    final String _sql = "SELECT * FROM time_blocks WHERE blockId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, blockId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TimeBlock>>() {
      @Override
      @NonNull
      public List<TimeBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBlockId = CursorUtil.getColumnIndexOrThrow(_cursor, "blockId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final List<TimeBlock> _result = new ArrayList<TimeBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBlockId;
            _tmpBlockId = _cursor.getInt(_cursorIndexOfBlockId);
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
            _item = new TimeBlock(_tmpId,_tmpBlockId,_tmpStartTime,_tmpEndTime,_tmpActiveDays);
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

  @Override
  public Object getAll(final Continuation<? super List<TimeBlock>> $completion) {
    final String _sql = "SELECT * FROM time_blocks";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TimeBlock>>() {
      @Override
      @NonNull
      public List<TimeBlock> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBlockId = CursorUtil.getColumnIndexOrThrow(_cursor, "blockId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfActiveDays = CursorUtil.getColumnIndexOrThrow(_cursor, "activeDays");
          final List<TimeBlock> _result = new ArrayList<TimeBlock>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlock _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBlockId;
            _tmpBlockId = _cursor.getInt(_cursorIndexOfBlockId);
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
            _item = new TimeBlock(_tmpId,_tmpBlockId,_tmpStartTime,_tmpEndTime,_tmpActiveDays);
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
