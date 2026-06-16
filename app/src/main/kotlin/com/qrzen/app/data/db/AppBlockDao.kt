package com.qrzen.app.data.db

import androidx.room.*
import com.qrzen.app.data.model.AppBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface AppBlockDao {
    @Query("SELECT * FROM app_blocks WHERE isArchived = 0 ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<AppBlock>>

    @Query("SELECT * FROM app_blocks WHERE isEnabled = 1 AND isArchived = 0")
    fun observeActive(): Flow<List<AppBlock>>

    @Query("SELECT * FROM app_blocks WHERE isArchived = 1 ORDER BY title COLLATE NOCASE ASC")
    fun observeArchived(): Flow<List<AppBlock>>

    @Query("SELECT * FROM app_blocks WHERE folderId = :folderId AND isArchived = 0 ORDER BY title COLLATE NOCASE ASC")
    fun observeByFolderId(folderId: Int): Flow<List<AppBlock>>

    @Query("SELECT * FROM app_blocks WHERE id = :id")
    suspend fun getById(id: Int): AppBlock?

    @Query("SELECT * FROM app_blocks WHERE qrSecret = :secret LIMIT 1")
    suspend fun getByQrSecret(secret: String): AppBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: AppBlock): Long

    @Update
    suspend fun update(block: AppBlock)

    @Delete
    suspend fun delete(block: AppBlock)

    @Query("UPDATE app_blocks SET pausedUntil = :until WHERE id = :id")
    suspend fun setPausedUntil(id: Int, until: Long)

    @Query("UPDATE app_blocks SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT * FROM app_blocks WHERE isArchived = 0")
    suspend fun getAll(): List<AppBlock>

    @Query("SELECT * FROM app_blocks")
    suspend fun getAllIncludingArchived(): List<AppBlock>

    @Query("SELECT * FROM app_blocks WHERE folderId = :folderId AND isArchived = 0")
    suspend fun getByFolderId(folderId: Int): List<AppBlock>

    @Query("SELECT * FROM app_blocks WHERE autoAddNewApps = 1 AND isArchived = 0")
    suspend fun getAutoAddNewAppsBlocks(): List<AppBlock>

    @Query("UPDATE app_blocks SET folderId = :folderId WHERE id = :id")
    suspend fun setFolderId(id: Int, folderId: Int?)

    @Query("UPDATE app_blocks SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolderId(folderId: Int)

    @Query("UPDATE app_blocks SET pausedUntil = :until WHERE folderId = :folderId")
    suspend fun setPausedUntilByFolderId(folderId: Int, until: Long)

    @Query("UPDATE app_blocks SET isEnabled = :enabled WHERE folderId = :folderId AND isArchived = 0")
    suspend fun setEnabledByFolderId(folderId: Int, enabled: Boolean)
}
