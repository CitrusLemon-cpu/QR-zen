     1	package com.qrzen.app.ui.main
     2	
     3	import android.content.Context
     4	import androidx.lifecycle.ViewModel
     5	import androidx.lifecycle.viewModelScope
     6	import com.qrzen.app.data.db.AppBlockDao
     7	import com.qrzen.app.data.db.BlockFolderDao
     8	import com.qrzen.app.data.db.TimeBlockDao
     9	import com.qrzen.app.data.model.AppBlock
    10	import com.qrzen.app.data.model.BlockFolder
    11	import com.qrzen.app.data.prefs.Prefs
    12	import com.qrzen.app.ui.unlock.UnlockMethodUtils
    13	import com.qrzen.app.widget.WidgetRefresh
    14	import dagger.hilt.android.lifecycle.HiltViewModel
    15	import dagger.hilt.android.qualifiers.ApplicationContext
    16	import kotlinx.coroutines.flow.SharingStarted
    17	import kotlinx.coroutines.flow.StateFlow
    18	import kotlinx.coroutines.flow.combine
    19	import kotlinx.coroutines.flow.stateIn
    20	import kotlinx.coroutines.launch
    21	import javax.inject.Inject
    22	
    23	@HiltViewModel
    24	class HomeViewModel @Inject constructor(
    25	    private val dao: AppBlockDao,
    26	    private val blockFolderDao: BlockFolderDao,
    27	    private val timeBlockDao: TimeBlockDao,
    28	    @ApplicationContext private val ctx: Context
    29	) : ViewModel() {
    30	
    31	    val blocks: StateFlow<List<AppBlock>> = dao.observeAll()
    32	        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    33	
    34	    val folders: StateFlow<List<BlockFolder>> = blockFolderDao.observeAll()
    35	        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    36	
    37	    val homeItems: StateFlow<List<HomeListItem>> = combine(blocks, folders) { blocks, folders ->
    38	        buildHomeItems(blocks, folders)
    39	    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    40	
    41	    fun delete(block: AppBlock) = viewModelScope.launch {
    42	        Prefs.clearAllowlistUsageTimer(block.id)
    43	        Prefs.clearAppTimersForBlock(block.id)
    44	        Prefs.clearWaitTimerState(block.id)
    45	        Prefs.clearScheduleWtState(block.id)
    46	        dao.delete(block)
    47	        WidgetRefresh.refresh(ctx)
    48	    }
    49	
    50	    fun setEnabled(block: AppBlock, enabled: Boolean) = viewModelScope.launch {
    51	        if (!enabled) {
    52	            Prefs.clearAllowlistUsageTimer(block.id)
    53	            if (block.isAllowlistMode) {
    54	                Prefs.clearAppTimersForBlock(block.id)
    55	            }
    56	            Prefs.clearWaitTimerState(block.id)
    57	            Prefs.clearScheduleWtState(block.id)
    58	        }
    59	        dao.update(block.copy(isEnabled = enabled))
    60	        WidgetRefresh.refresh(ctx)
    61	    }
    62	
    63	    fun enableWithActiveUntil(block: AppBlock, durationMs: Long) = viewModelScope.launch {
    64	        val activeUntil = System.currentTimeMillis() + durationMs
    65	        dao.update(block.copy(isEnabled = true, pausedUntil = 0L, activeUntil = activeUntil))
    66	        WidgetRefresh.refresh(ctx)
    67	    }
    68	
    69	    fun lockWithTimer(block: AppBlock, durationMs: Long, autoDisable: Boolean) = viewModelScope.launch {
    70	        val until = System.currentTimeMillis() + durationMs
    71	        dao.update(
    72	            block.copy(
    73	                toggleLockUntil = until,
    74	                autoDisableOnToggleLockExpiry = autoDisable
    75	            )
    76	        )
    77	        WidgetRefresh.refresh(ctx)
    78	    }
    79	
    80	    fun disableAndClearTimers(block: AppBlock) = viewModelScope.launch {
    81	        Prefs.clearAllowlistUsageTimer(block.id)
    82	        Prefs.clearAppTimersForBlock(block.id)
    83	        Prefs.clearWaitTimerState(block.id)
    84	        Prefs.clearScheduleWtState(block.id)
    85	        dao.update(
    86	            block.copy(
    87	                isEnabled = false,
    88	                activeUntil = 0L,
    89	                toggleLockUntil = 0L,
    90	                autoDisableOnToggleLockExpiry = false,
    91	                pomodoroRoundsTotal = 0,
    92	                pomodoroSessionStartMillis = 0L
    93	            )
    94	        )
    95	        WidgetRefresh.refresh(ctx)
    96	    }
    97	
    98	    fun startPomodoroSession(block: AppBlock, rounds: Int, lockEditing: Boolean) = viewModelScope.launch {
    99	        val now = System.currentTimeMillis()
   100	        val focusMs = block.pomodoroDurationMin * 60_000L
   101	        val breakMs = block.pomodoroBreakMin * 60_000L
   102	        val totalSessionMs = focusMs * rounds + breakMs * (rounds - 1)
   103	        val sessionEnd = now + totalSessionMs
   104	        dao.update(
   105	            block.copy(
   106	                isEnabled = true,
   107	                pausedUntil = 0L,
   108	                pomodoroRoundsTotal = rounds,
   109	                pomodoroSessionStartMillis = now,
   110	                pomodoroLockEditing = lockEditing,
   111	                toggleLockUntil = if (lockEditing) sessionEnd else 0L,
   112	                autoDisableOnToggleLockExpiry = if (lockEditing) true else false,
   113	                activeUntil = if (block.isAllowlistMode) sessionEnd else 0L
   114	            )
   115	        )
   116	        WidgetRefresh.refresh(ctx)
   117	    }
   118	
   119	    fun pause(block: AppBlock, durationMs: Long) = viewModelScope.launch {
   120	        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
   121	        else System.currentTimeMillis() + durationMs
   122	        dao.setPausedUntil(block.id, until)
   123	        WidgetRefresh.refresh(ctx)
   124	    }
   125	
   126	    fun unpause(block: AppBlock) = viewModelScope.launch {
   127	        if (block.isAllowlistMode) {
   128	            Prefs.resetAppTimersForBlock(block.id)
   129	        }
   130	        dao.setPausedUntil(block.id, 0L)
   131	        WidgetRefresh.refresh(ctx)
   132	    }
   133	
   134	    fun blockNow(block: AppBlock, durationMs: Long) = viewModelScope.launch {
   135	        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
   136	        else System.currentTimeMillis() + durationMs
   137	        dao.update(block.copy(isEnabled = true, pausedUntil = 0L, blockNowUntil = until))
   138	        WidgetRefresh.refresh(ctx)
   139	    }
   140	
   141	    fun archive(block: AppBlock) = viewModelScope.launch {
   142	        dao.setArchived(block.id, true)
   143	        WidgetRefresh.refresh(ctx)
   144	    }
   145	
   146	    fun setFolderEnabled(folder: BlockFolder, enabled: Boolean) = viewModelScope.launch {
   147	        if (!enabled) {
   148	            dao.getByFolderId(folder.id).forEach { block ->
   149	                Prefs.clearAllowlistUsageTimer(block.id)
   150	                if (block.isAllowlistMode) {
   151	                    Prefs.clearAppTimersForBlock(block.id)
   152	                }
   153	                Prefs.clearWaitTimerState(block.id)
   154	                Prefs.clearScheduleWtState(block.id)
   155	            }
   156	        }
   157	        blockFolderDao.setEnabled(folder.id, enabled)
   158	        dao.setEnabledByFolderId(folder.id, enabled)
   159	        WidgetRefresh.refresh(ctx)
   160	    }
   161	
   162	    fun toggleFolderCollapsed(folder: BlockFolder) = viewModelScope.launch {
   163	        blockFolderDao.setCollapsed(folder.id, !folder.isCollapsed)
   164	    }
   165	
   166	    fun deleteFolder(folder: BlockFolder) = viewModelScope.launch {
   167	        dao.clearFolderId(folder.id)
   168	        blockFolderDao.delete(folder)
   169	        WidgetRefresh.refresh(ctx)
   170	    }
   171	
   172	    fun moveBlockToFolder(block: AppBlock, folderId: Int?) = viewModelScope.launch {
   173	        dao.setFolderId(block.id, folderId)
   174	        WidgetRefresh.refresh(ctx)
   175	    }
   176	
   177	    suspend fun isBlockCurrentlyActive(block: AppBlock): Boolean {
   178	        val timeBlocks = timeBlockDao.getByBlockId(block.id)
   179	        return UnlockMethodUtils.isBlockCurrentlyActive(block, timeBlocks)
   180	    }
   181	
   182	    private fun buildHomeItems(blocks: List<AppBlock>, folders: List<BlockFolder>): List<HomeListItem> {
   183	        val items = mutableListOf<HomeListItem>()
   184	        val folderIds = folders.map { it.id }.toSet()
   185	        val blocksByFolderId = blocks.filter { it.folderId != null && it.folderId in folderIds }
   186	            .groupBy { it.folderId }
   187	
   188	        folders.forEach { folder ->
   189	            val folderBlocks = blocksByFolderId[folder.id].orEmpty()
   190	            items += HomeListItem.FolderHeader(folder, folderBlocks.size)
   191	            if (!folder.isCollapsed) {
   192	                folderBlocks.forEach { block ->
   193	                    items += HomeListItem.BlockItem(block, true)
   194	                }
   195	            }
   196	        }
   197	
   198	        blocks.filter { it.folderId == null || it.folderId !in folderIds }
   199	            .forEach { block ->
   200	                items += HomeListItem.BlockItem(block, false)
   201	            }
   202	
   203	        return items
   204	    }
   205	}
   206	