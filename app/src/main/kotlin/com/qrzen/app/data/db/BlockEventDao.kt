package com.qrzen.app.data.db

import androidx.room.*
import com.qrzen.app.data.model.BlockEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockEventDao {

    @Insert
    suspend fun insert(event: BlockEvent)

    @Query("SELECT * FROM block_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun observeSince(since: Long): Flow<List<BlockEvent>>

    @Query("SELECT * FROM block_events ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecent(): List<BlockEvent>

    @Query("DELETE FROM block_events WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
