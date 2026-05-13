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
   134	    fun pauseFolder(folder: BlockFolder, durationMs: Long) = viewModelScope.launch {
   135	        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
   136	        else System.currentTimeMillis() + durationMs
   137	        blockFolderDao.setPausedUntil(folder.id, until)
   138	        dao.setPausedUntilByFolderId(folder.id, until)
   139	        WidgetRefresh.refresh(ctx)
   140	    }
   141	
   142	    fun unpauseFolder(folder: BlockFolder) = viewModelScope.launch {
   143	        blockFolderDao.setPausedUntil(folder.id, 0L)
   144	        dao.setPausedUntilByFolderId(folder.id, 0L)
   145	        WidgetRefresh.refresh(ctx)
   146	    }
   147	
   148	    fun blockNow(block: AppBlock, durationMs: Long) = viewModelScope.launch {
   149	        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE
   150	        else System.currentTimeMillis() + durationMs
   151	        dao.update(block.copy(isEnabled = true, pausedUntil = 0L, blockNowUntil = until))
   152	        WidgetRefresh.refresh(ctx)
   153	    }
   154	
   155	    fun archive(block: AppBlock) = viewModelScope.launch {
   156	        dao.setArchived(block.id, true)
   157	        WidgetRefresh.refresh(ctx)
   158	    }
   159	
   160	    fun setFolderEnabled(folder: BlockFolder, enabled: Boolean) = viewModelScope.launch {
   161	        if (!enabled) {
   162	            dao.getByFolderId(folder.id).forEach { block ->
   163	                Prefs.clearAllowlistUsageTimer(block.id)
   164	                if (block.isAllowlistMode) {
   165	                    Prefs.clearAppTimersForBlock(block.id)
   166	                }
   167	                Prefs.clearWaitTimerState(block.id)
   168	                Prefs.clearScheduleWtState(block.id)
   169	            }
   170	        }
   171	        blockFolderDao.setEnabled(folder.id, enabled)
   172	        dao.setEnabledByFolderId(folder.id, enabled)
   173	        WidgetRefresh.refresh(ctx)
   174	    }
   175	
   176	    fun toggleFolderCollapsed(folder: BlockFolder) = viewModelScope.launch {
   177	        blockFolderDao.setCollapsed(folder.id, !folder.isCollapsed)
   178	    }
   179	
   180	    fun deleteFolder(folder: BlockFolder) = viewModelScope.launch {
   181	        dao.clearFolderId(folder.id)
   182	        blockFolderDao.delete(folder)
   183	        WidgetRefresh.refresh(ctx)
   184	    }
   185	
   186	    fun moveBlockToFolder(block: AppBlock, folderId: Int?) = viewModelScope.launch {
   187	        dao.setFolderId(block.id, folderId)
   188	        WidgetRefresh.refresh(ctx)
   189	    }
   190	
   191	    suspend fun isBlockCurrentlyActive(block: AppBlock): Boolean {
   192	        val timeBlocks = timeBlockDao.getByBlockId(block.id)
   193	        return UnlockMethodUtils.isBlockCurrentlyActive(block, timeBlocks)
   194	    }
   195	
   196	    private fun buildHomeItems(blocks: List<AppBlock>, folders: List<BlockFolder>): List<HomeListItem> {
   197	        val items = mutableListOf<HomeListItem>()
   198	        val folderIds = folders.map { it.id }.toSet()
   199	        val blocksByFolderId = blocks.filter { it.folderId != null && it.folderId in folderIds }
   200	            .groupBy { it.folderId }
   201	
   202	        folders.forEach { folder ->
   203	            val folderBlocks = blocksByFolderId[folder.id].orEmpty()
   204	            items += HomeListItem.FolderHeader(folder, folderBlocks.size)
   205	            if (!folder.isCollapsed) {
   206	                folderBlocks.forEach { block ->
   207	                    items += HomeListItem.BlockItem(block, true)
   208	                }
   209	            }
   210	        }
   211	
   212	        blocks.filter { it.folderId == null || it.folderId !in folderIds }
   213	            .forEach { block ->
   214	                items += HomeListItem.BlockItem(block, false)
   215	            }
   216	
   217	        return items
   218	    }
   219	}
   220	