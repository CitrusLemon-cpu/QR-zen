package com.qrzen.app.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.AppDatabase
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val db: AppDatabase,
    private val dao: AppBlockDao,
    private val blockFolderDao: BlockFolderDao,
    private val timeBlockDao: TimeBlockDao,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    val blocks: StateFlow<List<AppBlock>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<BlockFolder>> = blockFolderDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeItems: StateFlow<List<HomeListItem>> = combine(blocks, folders) { blocks, folders ->
        buildHomeItems(blocks, folders)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(block: AppBlock) = viewModelScope.launch {
        Prefs.clearAllowlistUsageTimer(block.id)
        Prefs.clearAppTimersForBlock(block.id)
        Prefs.clearWaitTimerState(block.id)
        Prefs.clearScheduleWtState(block.id)
        dao.delete(block)
        WidgetRefresh.refresh(ctx)
    }

    fun setEnabled(block: AppBlock, enabled: Boolean) = viewModelScope.launch {
        if (!enabled) {
            Prefs.clearAllowlistUsageTimer(block.id)
            if (block.isAllowlistMode) {
                Prefs.clearAppTimersForBlock(block.id)
            }
            Prefs.clearWaitTimerState(block.id)
            Prefs.clearScheduleWtState(block.id)
        }
        dao.update(block.copy(isEnabled = enabled))
        WidgetRefresh.refresh(ctx)
    }

    fun enableWithActiveUntil(block: AppBlock, durationMs: Long) = viewModelScope.launch {
        val activeUntil = System.currentTimeMillis() + durationMs
        dao.update(block.copy(isEnabled = true, pausedUntil = 0L, activeUntil = activeUntil))
        WidgetRefresh.refresh(ctx)
    }

    fun lockWithTimer(block: AppBlock, durationMs: Long, autoDisable: Boolean) = viewModelScope.launch {
        val until = System.currentTimeMillis() + durationMs
        dao.update(
            block.copy(
                toggleLockUntil = until,
                autoDisableOnToggleLockExpiry = autoDisable
            )
        )
        WidgetRefresh.refresh(ctx)
    }

    fun disableAndClearTimers(block: AppBlock) = viewModelScope.launch {
        Prefs.clearAllowlistUsageTimer(block.id)
        Prefs.clearAppTimersForBlock(block.id)
        Prefs.clearWaitTimerState(block.id)
        Prefs.clearScheduleWtState(block.id)
        dao.update(
            block.copy(
                isEnabled = false,
                activeUntil = 0L,
                toggleLockUntil = 0L,
                autoDisableOnToggleLockExpiry = false,
                pomodoroRoundsTotal = 0,
                pomodoroSessionStartMillis = 0L
            )
        )
        WidgetRefresh.refresh(ctx)
    }

    fun startPomodoroSession(block: AppBlock, rounds: Int, lockEditing: Boolean) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val focusMs = block.pomodoroDurationMin * 60_000L
        val breakMs = block.pomodoroBreakMin * 60_000L
        val totalSessionMs = focusMs * rounds + breakMs * (rounds - 1)
        val sessionEnd = now + totalSessionMs
        dao.update(
            block.copy(
                isEnabled = true,
                pausedUntil = 0L,
                pomodoroRoundsTotal = rounds,
                pomodoroSessionStartMillis = now,
                pomodoroLockEditing = lockEditing,
                toggleLockUntil = if (lockEditing) sessionEnd else 0L,
                autoDisableOnToggleLockExpiry = if (lockEditing) true else false,
                activeUntil = if (block.isAllowlistMode) sessionEnd else 0L
            )
        )
        WidgetRefresh.refresh(ctx)
    }

    fun pause(block: AppBlock, durationMs: Long) = viewModelScope.launch {
        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
        else System.currentTimeMillis() + durationMs
        dao.setPausedUntil(block.id, until)
        WidgetRefresh.refresh(ctx)
    }

    fun unpause(block: AppBlock) = viewModelScope.launch {
        if (block.isAllowlistMode) {
            Prefs.resetAppTimersForBlock(block.id)
        }
        dao.setPausedUntil(block.id, 0L)
        WidgetRefresh.refresh(ctx)
    }

    fun pauseFolder(folder: BlockFolder, durationMs: Long) = viewModelScope.launch {
        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
        else System.currentTimeMillis() + durationMs
        db.withTransaction {
            blockFolderDao.setPausedUntil(folder.id, until)
            dao.setPausedUntilByFolderId(folder.id, until)
        }
        WidgetRefresh.refresh(ctx)
    }

    fun unpauseFolder(folder: BlockFolder) = viewModelScope.launch {
        db.withTransaction {
            blockFolderDao.setPausedUntil(folder.id, 0L)
            dao.setPausedUntilByFolderId(folder.id, 0L)
        }
        WidgetRefresh.refresh(ctx)
    }

    fun blockNow(block: AppBlock, durationMs: Long) = viewModelScope.launch {
        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
        else System.currentTimeMillis() + durationMs
        dao.update(block.copy(isEnabled = true, pausedUntil = 0L, blockNowUntil = until))
        WidgetRefresh.refresh(ctx)
    }

    fun archive(block: AppBlock) = viewModelScope.launch {
        dao.setArchived(block.id, true)
        WidgetRefresh.refresh(ctx)
    }

    fun setFolderEnabled(folder: BlockFolder, enabled: Boolean) = viewModelScope.launch {
        if (!enabled) {
            dao.getByFolderId(folder.id).forEach { block ->
                Prefs.clearAllowlistUsageTimer(block.id)
                if (block.isAllowlistMode) {
                    Prefs.clearAppTimersForBlock(block.id)
                }
                Prefs.clearWaitTimerState(block.id)
                Prefs.clearScheduleWtState(block.id)
            }
        }
        db.withTransaction {
            blockFolderDao.setEnabled(folder.id, enabled)
            dao.setEnabledByFolderId(folder.id, enabled)
        }
        WidgetRefresh.refresh(ctx)
    }

    fun toggleFolderCollapsed(folder: BlockFolder) = viewModelScope.launch {
        blockFolderDao.setCollapsed(folder.id, !folder.isCollapsed)
    }

    fun deleteFolder(folder: BlockFolder) = viewModelScope.launch {
        db.withTransaction {
            dao.clearFolderId(folder.id)
            blockFolderDao.delete(folder)
        }
        WidgetRefresh.refresh(ctx)
    }

    fun moveBlockToFolder(block: AppBlock, folderId: Int?) = viewModelScope.launch {
        dao.setFolderId(block.id, folderId)
        WidgetRefresh.refresh(ctx)
    }

    suspend fun isBlockCurrentlyActive(block: AppBlock): Boolean {
        val timeBlocks = timeBlockDao.getByBlockId(block.id)
        return UnlockMethodUtils.isBlockCurrentlyActive(block, timeBlocks)
    }

    private fun buildHomeItems(blocks: List<AppBlock>, folders: List<BlockFolder>): List<HomeListItem> {
        val items = mutableListOf<HomeListItem>()
        val folderIds = folders.map { it.id }.toSet()
        val blocksByFolderId = blocks.filter { it.folderId != null && it.folderId in folderIds }
            .groupBy { it.folderId }

        folders.forEach { folder ->
            val folderBlocks = blocksByFolderId[folder.id].orEmpty()
            items += HomeListItem.FolderHeader(folder, folderBlocks.size)
            if (!folder.isCollapsed) {
                folderBlocks.forEach { block ->
                    items += HomeListItem.BlockItem(block, true)
                }
            }
        }

        blocks.filter { it.folderId == null || it.folderId !in folderIds }
            .forEach { block ->
                items += HomeListItem.BlockItem(block, false)
            }

        return items
    }
}
