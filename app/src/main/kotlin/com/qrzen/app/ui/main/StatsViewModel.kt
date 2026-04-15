package com.qrzen.app.ui.main

import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.qrzen.app.data.db.BlockEventDao
import com.qrzen.app.data.model.BlockEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted; import kotlinx.coroutines.flow.StateFlow; import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(private val dao: BlockEventDao) : ViewModel() {
    val recentEvents: StateFlow<List<BlockEvent>> = dao.observeSince(
        System.currentTimeMillis() - 7 * 24 * 60 * 60_000L
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
