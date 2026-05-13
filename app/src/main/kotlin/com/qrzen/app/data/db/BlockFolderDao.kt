package com.qrzen.app.data.db

import androidx.room.*
import com.qrzen.app.data.model.BlockFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockFolderDao {
    @Query("SELECT * FROM block_folders ORDER BY sortOrder ASC, title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<BlockFolder>>

    @Query("SELECT * FROM block_folders WHERE id = :id")
    suspend fun getById(id: Int): BlockFolder?

    @Query("SELECT * FROM block_folders")
    suspend fun getAll(): List<BlockFolder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: BlockFolder): Long

    @Update
    suspend fun update(folder: BlockFolder)

    @Delete
    suspend fun delete(folder: BlockFolder)

    @Query("UPDATE block_folders SET isCollapsed = :collapsed WHERE id = :id")
    suspend fun setCollapsed(id: Int, collapsed: Boolean)

    @Query("UPDATE block_folders SET pausedUntil = :until WHERE id = :id")
    suspend fun setPausedUntil(id: Int, until: Long)

    @Query("UPDATE block_folders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean)
}
