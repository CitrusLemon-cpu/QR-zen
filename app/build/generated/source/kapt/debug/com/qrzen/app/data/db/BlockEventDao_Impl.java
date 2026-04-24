package com.qrzen.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.qrzen.app.data.model.BlockEvent;
import java.lang.Class;
import java.lang.Exception;
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
public final class BlockEventDao_Impl implements BlockEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BlockEvent> __insertionAdapterOfBlockEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  public BlockEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBlockEvent = new EntityInsertionAdapter<BlockEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `block_events` (`id`,`blockId`,`blockTitle`,`packageName`,`eventType`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockEvent entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBlockId());
        if (entity.getBlockTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBlockTitle());
        }
        if (entity.getPackageName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPackageName());
        }
        if (entity.getEventType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getEventType());
        }
        statement.bindLong(6, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM block_events WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final BlockEvent event, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBlockEvent.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOlderThan(final long before, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, before);
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
          __preparedStmtOfDeleteOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BlockEvent>> observeSince(final long since) {
    final String _sql = "SELECT * FROM block_events WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"block_events"}, new Callable<List<BlockEvent>>() {
      @Override
      @NonNull
      public List<BlockEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBlockId = CursorUtil.getColumnIndexOrThrow(_cursor, "blockId");
          final int _cursorIndexOfBlockTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockTitle");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<BlockEvent> _result = new ArrayList<BlockEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockEvent _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBlockId;
            _tmpBlockId = _cursor.getInt(_cursorIndexOfBlockId);
            final String _tmpBlockTitle;
            if (_cursor.isNull(_cursorIndexOfBlockTitle)) {
              _tmpBlockTitle = null;
            } else {
              _tmpBlockTitle = _cursor.getString(_cursorIndexOfBlockTitle);
            }
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpEventType;
            if (_cursor.isNull(_cursorIndexOfEventType)) {
              _tmpEventType = null;
            } else {
              _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new BlockEvent(_tmpId,_tmpBlockId,_tmpBlockTitle,_tmpPackageName,_tmpEventType,_tmpTimestamp);
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
  public Object getRecent(final Continuation<? super List<BlockEvent>> $completion) {
    final String _sql = "SELECT * FROM block_events ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BlockEvent>>() {
      @Override
      @NonNull
      public List<BlockEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBlockId = CursorUtil.getColumnIndexOrThrow(_cursor, "blockId");
          final int _cursorIndexOfBlockTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "blockTitle");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<BlockEvent> _result = new ArrayList<BlockEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockEvent _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBlockId;
            _tmpBlockId = _cursor.getInt(_cursorIndexOfBlockId);
            final String _tmpBlockTitle;
            if (_cursor.isNull(_cursorIndexOfBlockTitle)) {
              _tmpBlockTitle = null;
            } else {
              _tmpBlockTitle = _cursor.getString(_cursorIndexOfBlockTitle);
            }
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpEventType;
            if (_cursor.isNull(_cursorIndexOfEventType)) {
              _tmpEventType = null;
            } else {
              _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new BlockEvent(_tmpId,_tmpBlockId,_tmpBlockTitle,_tmpPackageName,_tmpEventType,_tmpTimestamp);
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
