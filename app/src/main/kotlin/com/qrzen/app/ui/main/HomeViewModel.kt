package com.qrzen.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: AppBlockDao
) : ViewModel() {

    val blocks: StateFlow<List<AppBlock>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(block: AppBlock) = viewModelScope.launch {
        dao.delete(block)
    }

    fun setEnabled(block: AppBlock, enabled: Boolean) = viewModelScope.launch {
        dao.update(block.copy(isEnabled = enabled))
    }
}
