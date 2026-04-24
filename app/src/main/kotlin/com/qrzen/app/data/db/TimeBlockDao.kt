package com.qrzen.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.qrzen.app.data.model.TimeBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_blocks WHERE blockId = :blockId ORDER BY id ASC")
    fun observeByBlockId(blockId: Int): Flow<List<TimeBlock>>

    @Query("SELECT * FROM time_blocks WHERE blockId = :blockId ORDER BY id ASC")
    suspend fun getByBlockId(blockId: Int): List<TimeBlock>

    @Query("SELECT * FROM time_blocks")
    suspend fun getAll(): List<TimeBlock>

    @Insert
    suspend fun insert(block: TimeBlock): Long

    @Insert
    suspend fun insertAll(blocks: List<TimeBlock>)

    @Delete
    suspend fun delete(block: TimeBlock)

    @Query("DELETE FROM time_blocks WHERE blockId = :blockId")
    suspend fun deleteByBlockId(blockId: Int)

    @Query("DELETE FROM time_blocks WHERE id = :id")
    suspend fun deleteById(id: Int)
}
