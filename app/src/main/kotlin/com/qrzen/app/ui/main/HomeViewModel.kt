package com.qrzen.app.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: AppBlockDao,
    private val timeBlockDao: TimeBlockDao,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    val blocks: StateFlow<List<AppBlock>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(block: AppBlock) = viewModelScope.launch {
        dao.delete(block)
        WidgetRefresh.refresh(ctx)
    }

    fun setEnabled(block: AppBlock, enabled: Boolean) = viewModelScope.launch {
        dao.update(block.copy(isEnabled = enabled))
        WidgetRefresh.refresh(ctx)
    }

    fun pause(block: AppBlock, durationMs: Long) = viewModelScope.launch {
        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
        else System.currentTimeMillis() + durationMs
        dao.setPausedUntil(block.id, until)
        WidgetRefresh.refresh(ctx)
    }

    fun unpause(block: AppBlock) = viewModelScope.launch {
        dao.setPausedUntil(block.id, 0L)
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

    suspend fun isBlockCurrentlyActive(block: AppBlock): Boolean {
        val timeBlocks = timeBlockDao.getByBlockId(block.id)
        return UnlockMethodUtils.isBlockCurrentlyActive(block, timeBlocks)
    }
}
