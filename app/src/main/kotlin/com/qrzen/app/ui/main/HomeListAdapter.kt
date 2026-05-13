     1	package com.qrzen.app.ui.main
     2	
     3	import android.content.Context
     4	import android.os.CountDownTimer
     5	import android.view.LayoutInflater
     6	import android.view.View
     7	import android.view.ViewGroup
     8	import android.widget.PopupMenu
     9	import androidx.recyclerview.widget.DiffUtil
    10	import androidx.recyclerview.widget.LinearLayoutManager
    11	import androidx.recyclerview.widget.ListAdapter
    12	import androidx.recyclerview.widget.RecyclerView
    13	import com.qrzen.app.R
    14	import com.qrzen.app.data.db.TimeBlockDao
    15	import com.qrzen.app.data.model.AppBlock
    16	import com.qrzen.app.data.model.BlockFolder
    17	import com.qrzen.app.data.model.TimeBlock
    18	import com.qrzen.app.databinding.ItemBlockBinding
    19	import com.qrzen.app.databinding.ItemFolderBinding
    20	import com.qrzen.app.databinding.ItemSelectedAppIconBinding
    21	import com.qrzen.app.ui.unlock.UnlockMethodUtils
    22	import kotlinx.coroutines.CoroutineScope
    23	import kotlinx.coroutines.Dispatchers
    24	import kotlinx.coroutines.Job
    25	import kotlinx.coroutines.SupervisorJob
    26	import kotlinx.coroutines.launch
    27	import kotlinx.coroutines.withContext
    28	
    29	sealed class HomeListItem {
    30	    data class FolderHeader(val folder: BlockFolder, val blockCount: Int) : HomeListItem()
    31	    data class BlockItem(val block: AppBlock, val isInFolder: Boolean) : HomeListItem()
    32	}
    33	
    34	class HomeListAdapter(
    35	    private val timeBlockDao: TimeBlockDao,
    36	    private val onToggle: (AppBlock, Boolean) -> Boolean,
    37	    private val onPause: (AppBlock) -> Unit,
    38	    private val onBlockNow: (AppBlock) -> Unit,
    39	    private val onEdit: (AppBlock) -> Unit,
    40	    private val onArchive: (AppBlock) -> Unit,
    41	    private val onDelete: (AppBlock) -> Unit,
    42	    private val onRestartFromPause: (AppBlock) -> Unit,
    43	    private val onLockWithTimer: (AppBlock) -> Unit,
    44	    private val onMoveToFolder: (AppBlock) -> Unit,
    45	    private val onFolderToggle: (BlockFolder, Boolean) -> Unit,
    46	    private val onFolderExpandCollapse: (BlockFolder) -> Unit,
    47	    private val onFolderEdit: (BlockFolder) -> Unit,
    48	    private val onFolderPause: (BlockFolder) -> Unit,
    49	    private val onFolderDelete: (BlockFolder) -> Unit
    50	) : ListAdapter<HomeListItem, RecyclerView.ViewHolder>(DIFF) {
    51	
    52	    companion object {
    53	        private const val VIEW_TYPE_FOLDER = 1
    54	        private const val VIEW_TYPE_BLOCK = 2
    55	
    56	        val DIFF = object : DiffUtil.ItemCallback<HomeListItem>() {
    57	            override fun areItemsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
    58	                return when {
    59	                    oldItem is HomeListItem.FolderHeader && newItem is HomeListItem.FolderHeader ->
    60	                        oldItem.folder.id == newItem.folder.id
    61	                    oldItem is HomeListItem.BlockItem && newItem is HomeListItem.BlockItem ->
    62	                        oldItem.block.id == newItem.block.id
    63	                    else -> false
    64	                }
    65	            }
    66	
    67	            override fun areContentsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
    68	                return oldItem == newItem
    69	            }
    70	        }
    71	    }
    72	
    73	    data class BlockAppIcon(
    74	        val packageName: String,
    75	        val icon: android.graphics.drawable.Drawable
    76	    )
    77	
    78	    private class BlockAppsIconAdapter(
    79	        private val apps: List<BlockAppIcon>
    80	    ) : RecyclerView.Adapter<BlockAppsIconAdapter.ViewHolder>() {
    81	        class ViewHolder(val binding: ItemSelectedAppIconBinding) : RecyclerView.ViewHolder(binding.root) {
    82	            fun bind(item: BlockAppIcon) {
    83	                binding.ivSelectedAppIcon.setImageDrawable(item.icon)
    84	            }
    85	        }
    86	
    87	        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    88	            ViewHolder(ItemSelectedAppIconBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    89	
    90	        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(apps[position])
    91	
    92	        override fun getItemCount(): Int = apps.size
    93	    }
    94	
    95	    inner class FolderViewHolder(private val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root) {
    96	        fun bind(item: HomeListItem.FolderHeader) {
    97	            val folder = item.folder
    98	            binding.tvFolderTitle.text = folder.title
    99	            binding.tvFolderInfo.text = buildFolderInfo(binding.root.context, folder, item.blockCount)
   100	            binding.ivFolderLock.visibility = if (
   101	                folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE } == UnlockMethodUtils.METHOD_NONE
   102	            ) {
   103	                View.GONE
   104	            } else {
   105	                View.VISIBLE
   106	            }
   107	            binding.ivExpandCollapse.setImageResource(
   108	                if (folder.isCollapsed) R.drawable.ic_expand_more else R.drawable.ic_expand_less
   109	            )
   110	
   111	            binding.switchFolderEnabled.setOnCheckedChangeListener(null)
   112	            binding.switchFolderEnabled.isChecked = folder.isEnabled
   113	            binding.switchFolderEnabled.setOnCheckedChangeListener { _, isChecked ->
   114	                onFolderToggle(folder, isChecked)
   115	            }
   116	
   117	            binding.root.setOnClickListener { onFolderExpandCollapse(folder) }
   118	            binding.ivExpandCollapse.setOnClickListener { onFolderExpandCollapse(folder) }
   119	            binding.btnFolderOverflow.setOnClickListener { showFolderPopupMenu(it, folder) }
   120	        }
   121	
   122	        private fun showFolderPopupMenu(anchor: View, folder: BlockFolder) {
   123	            val popup = PopupMenu(anchor.context, anchor)
   124	            popup.menuInflater.inflate(R.menu.menu_folder_overflow, popup.menu)
   125	            popup.setOnMenuItemClickListener { item ->
   126	                when (item.itemId) {
   127	                    R.id.action_pause_folder -> {
   128	                        onFolderPause(folder)
   129	                        true
   130	                    }
   131	                    R.id.action_edit_folder -> {
   132	                        onFolderEdit(folder)
   133	                        true
   134	                    }
   135	                    R.id.action_delete_folder -> {
   136	                        onFolderDelete(folder)
   137	                        true
   138	                    }
   139	                    else -> false
   140	                }
   141	            }
   142	            popup.show()
   143	        }
   144	    }
   145	
   146	    inner class BlockViewHolder(private val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
   147	        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
   148	        private var countDownTimer: CountDownTimer? = null
   149	        private var blockNowTimer: CountDownTimer? = null
   150	        private var lockTimer: CountDownTimer? = null
   151	        private var activeTimer: CountDownTimer? = null
   152	        private var pomodoroTimer: CountDownTimer? = null
   153	        private var usageStatusTimer: CountDownTimer? = null
   154	        private var iconLoadJob: Job? = null
   155	        private var usageQueryJob: Job? = null
   156	        private var boundBlockId: Int? = null
   157	        private var boundPackages: List<String> = emptyList()
   158	
   159	        fun bind(item: HomeListItem.BlockItem) {
   160	            val block = item.block
   161	
   162	            countDownTimer?.cancel()
   163	            countDownTimer = null
   164	            blockNowTimer?.cancel()
   165	            blockNowTimer = null
   166	            lockTimer?.cancel()
   167	            lockTimer = null
   168	            activeTimer?.cancel()
   169	            activeTimer = null
   170	            pomodoroTimer?.cancel()
   171	            pomodoroTimer = null
   172	            usageStatusTimer?.cancel()
   173	            usageStatusTimer = null
   174	            iconLoadJob?.cancel()
   175	            iconLoadJob = null
   176	            usageQueryJob?.cancel()
   177	            usageQueryJob = null
   178	
   179	            updateIndent(item.isInFolder)
   180	
   181	            boundBlockId = block.id
   182	            binding.tvTitle.text = block.title
   183	            val modePrefix = if (block.isAllowlistMode) "Allowlist" else "Blocklist"
   184	            when (block.blockingStyle) {
   185	                UnlockMethodUtils.STYLE_MANUAL -> {
   186	                    binding.tvTimeRange.text = "Manual"
   187	                    binding.tvDays.text = modePrefix
   188	                }
   189	                UnlockMethodUtils.STYLE_SCHEDULE -> {
   190	                    val timeRangeText = when (block.scheduleBreakType) {
   191	                        UnlockMethodUtils.BREAK_POMODORO -> {
   192	                            usageQueryJob = scope.launch {
   193	                                val timeBlocks = withContext(Dispatchers.IO) {
   194	                                    timeBlockDao.getByBlockId(block.id)
   195	                                }
   196	                                bindSchedulePomodoroPhase(block, timeBlocks)
   197	                            }
   198	                            "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
   199	                        }
   200	                        UnlockMethodUtils.BREAK_WAIT_TIMER -> {
   201	                            val adaptiveSuffix = if (block.waitTimerAdaptive) " · adaptive" else ""
   202	                            "Scheduled · Wait Timer (${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block$adaptiveSuffix)"
   203	                        }
   204	                        UnlockMethodUtils.BREAK_USAGE_LIMIT -> {
   205	                            val period = if (block.usageLimitPeriod == "HOURLY") "hour" else "day"
   206	                            "Scheduled · Usage Limit (${block.usageLimitMinutes} min/$period)"
   207	                        }
   208	                        UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE ->
   209	                            "Scheduled · Allowance (${block.scheduledAllowanceMinutes} min/window)"
   210	                        else -> "Scheduled"
   211	                    }
   212	                    binding.tvTimeRange.text = timeRangeText
   213	                    binding.tvDays.text = modePrefix
   214	                }
   215	                UnlockMethodUtils.STYLE_USAGE_LIMIT -> {
   216	                    val period = if (block.usageLimitPeriod == "HOURLY") "per hour" else "per day"
   217	                    binding.tvTimeRange.text = "${block.usageLimitMinutes} min $period"
   218	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   219	
   220	                    usageQueryJob = scope.launch {
   221	                        val remainingText = withContext(Dispatchers.IO) {
   222	                            computeUsageLimitRemaining(binding.root.context, block)
   223	                        }
   224	                        binding.tvTimeRange.text = remainingText
   225	                    }
   226	                }
   227	                UnlockMethodUtils.STYLE_WAIT_TIMER -> {
   228	                    val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
   229	                    binding.tvTimeRange.text = "${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block ($modeLabel)"
   230	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   231	
   232	                    usageQueryJob = scope.launch {
   233	                        val status = withContext(Dispatchers.IO) {
   234	                            computeWaitTimerStatus(binding.root.context, block)
   235	                        }
   236	                        binding.tvTimeRange.text = status.text
   237	
   238	                        if (status.blockingRemainingMs > 0L) {
   239	                            usageStatusTimer?.cancel()
   240	                            usageStatusTimer = object : CountDownTimer(status.blockingRemainingMs, 1000L) {
   241	                                override fun onTick(ms: Long) {
   242	                                    val min = ms / 60_000
   243	                                    val sec = (ms % 60_000) / 1000
   244	                                    binding.tvTimeRange.text = "Blocked for ${min}m ${sec}s"
   245	                                }
   246	
   247	                                override fun onFinish() {
   248	                                    binding.tvTimeRange.text = computeWaitTimerStatus(binding.root.context, block).text
   249	                                }
   250	                            }.start()
   251	                        }
   252	                    }
   253	                }
   254	                UnlockMethodUtils.STYLE_POMODORO -> {
   255	                    val state = UnlockMethodUtils.computePomodoroState(block)
   256	                    if (state.isSessionActive) {
   257	                        if (state.isInFocus) {
   258	                            binding.tvTimeRange.text = "🎯 Focus ${state.currentRound}/${state.totalRounds}"
   259	                        } else {
   260	                            binding.tvTimeRange.text = "☕ Break ${state.currentRound}/${state.totalRounds}"
   261	                        }
   262	                    } else {
   263	                        binding.tvTimeRange.text = "${block.pomodoroDurationMin}m focus / ${block.pomodoroBreakMin}m break"
   264	                    }
   265	                    binding.tvDays.text = modePrefix
   266	                }
   267	                else -> {
   268	                    binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
   269	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   270	                }
   271	            }
   272	
   273	            val unlockSummary = UnlockMethodUtils.getUnlockMethodSummary(binding.root.context, block)
   274	            binding.tvUnlockMethod.visibility = if (unlockSummary.isNullOrBlank()) View.GONE else View.VISIBLE
   275	            binding.tvUnlockMethod.text = unlockSummary.orEmpty()
   276	
   277	            binding.switchEnabled.setOnCheckedChangeListener(null)
   278	            binding.switchEnabled.isChecked = block.isEnabled
   279	            bindToggleListener(block)
   280	
   281	            binding.btnOverflow.setOnClickListener { view ->
   282	                showPopupMenu(view, block)
   283	            }
   284	
   285	            setupPauseTimer(block)
   286	            setupBlockNowTimer(block)
   287	            setupLockTimer(block)
   288	            setupActiveTimer(block)
   289	            setupPomodoroTimer(block)
   290	
   291	            val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
   292	            boundPackages = packages
   293	            if (packages.isEmpty()) {
   294	                binding.rvBlockApps.visibility = View.GONE
   295	                binding.rvBlockApps.adapter = null
   296	                return
   297	            }
   298	
   299	            binding.rvBlockApps.visibility = View.VISIBLE
   300	            binding.rvBlockApps.adapter = null
   301	            if (binding.rvBlockApps.layoutManager == null) {
   302	                binding.rvBlockApps.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
   303	            }
   304	
   305	            val pm = binding.root.context.packageManager
   306	            iconLoadJob = scope.launch {
   307	                val icons = withContext(Dispatchers.IO) {
   308	                    packages.mapNotNull { pkg ->
   309	                        try {
   310	                            val appInfo = pm.getApplicationInfo(pkg, 0)
   311	                            BlockAppIcon(pkg, pm.getApplicationIcon(appInfo))
   312	                        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
   313	                            null
   314	                        }
   315	                    }
   316	                }
   317	                if (packages != boundPackages) return@launch
   318	                if (icons.isEmpty()) {
   319	                    binding.rvBlockApps.visibility = View.GONE
   320	                    binding.rvBlockApps.adapter = null
   321	                } else {
   322	                    binding.rvBlockApps.visibility = View.VISIBLE
   323	                    binding.rvBlockApps.adapter = BlockAppsIconAdapter(icons)
   324	                }
   325	            }
   326	        }
   327	
   328	        private fun updateIndent(isInFolder: Boolean) {
   329	            val layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)
   330	                ?: RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
   331	            layoutParams.marginStart = dpToPx(if (isInFolder) 24 else 8)
   332	            layoutParams.marginEnd = dpToPx(8)
   333	            binding.root.layoutParams = layoutParams
   334	        }
   335	
   336	        private fun bindToggleListener(block: AppBlock) {
   337	            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
   338	                val accepted = onToggle(block, checked)
   339	                if (!accepted) {
   340	                    binding.switchEnabled.setOnCheckedChangeListener(null)
   341	                    binding.switchEnabled.isChecked = block.isEnabled
   342	                    bindToggleListener(block)
   343	                }
   344	            }
   345	        }
   346	
   347	        private fun setupPauseTimer(block: AppBlock) {
   348	            val now = System.currentTimeMillis()
   349	            val isPaused = block.pausedUntil > now
   350	            val isIndefinite = block.pausedUntil == Long.MAX_VALUE
   351	
   352	            if (isPaused || isIndefinite) {
   353	                binding.tvPauseTimer.visibility = View.VISIBLE
   354	                binding.tvPauseTimer.setOnClickListener {
   355	                    onRestartFromPause(block)
   356	                }
   357	
   358	                if (isIndefinite) {
   359	                    binding.tvPauseTimer.text = "⏸ Paused indefinitely • Tap to restart"
   360	                } else {
   361	                    val remaining = block.pausedUntil - now
   362	                    binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(remaining)} remaining"
   363	                    countDownTimer = object : CountDownTimer(remaining, 1000L) {
   364	                        override fun onTick(millisUntilFinished: Long) {
   365	                            binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(millisUntilFinished)} remaining"
   366	                        }
   367	
   368	                        override fun onFinish() {
   369	                            binding.tvPauseTimer.visibility = View.GONE
   370	                        }
   371	                    }.start()
   372	                }
   373	            } else {
   374	                binding.tvPauseTimer.visibility = View.GONE
   375	            }
   376	        }
   377	
   378	        private fun setupBlockNowTimer(block: AppBlock) {
   379	            val now = System.currentTimeMillis()
   380	            if (block.blockNowUntil > now) {
   381	                binding.tvBlockNowTimer.visibility = View.VISIBLE
   382	                val remaining = block.blockNowUntil - now
   383	                binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(remaining)}"
   384	                blockNowTimer = object : CountDownTimer(remaining, 1000L) {
   385	                    override fun onTick(ms: Long) {
   386	                        binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(ms)}"
   387	                    }
   388	
   389	                    override fun onFinish() {
   390	                        binding.tvBlockNowTimer.visibility = View.GONE
   391	                    }
   392	                }.start()
   393	            } else {
   394	                binding.tvBlockNowTimer.visibility = View.GONE
   395	            }
   396	        }
   397	
   398	        private fun setupLockTimer(block: AppBlock) {
   399	            val now = System.currentTimeMillis()
   400	            if (block.toggleLockUntil > now) {
   401	                binding.tvLockTimer.visibility = View.VISIBLE
   402	                val remaining = block.toggleLockUntil - now
   403	                binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(remaining)}"
   404	                lockTimer = object : CountDownTimer(remaining, 1000L) {
   405	                    override fun onTick(ms: Long) {
   406	                        binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(ms)}"
   407	                    }
   408	
   409	                    override fun onFinish() {
   410	                        binding.tvLockTimer.visibility = View.GONE
   411	                    }
   412	                }.start()
   413	            } else {
   414	                binding.tvLockTimer.visibility = View.GONE
   415	            }
   416	        }
   417	
   418	        private fun setupActiveTimer(block: AppBlock) {
   419	            val now = System.currentTimeMillis()
   420	            if (block.activeUntil > now) {
   421	                binding.tvActiveTimer.visibility = View.VISIBLE
   422	                val remaining = block.activeUntil - now
   423	                val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
   424	                val endsAt = sdf.format(java.util.Date(block.activeUntil))
   425	                binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(remaining)} (until $endsAt)"
   426	                activeTimer = object : CountDownTimer(remaining, 1000L) {
   427	                    override fun onTick(ms: Long) {
   428	                        binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(ms)} (until $endsAt)"
   429	                    }
   430	
   431	                    override fun onFinish() {
   432	                        binding.tvActiveTimer.visibility = View.GONE
   433	                    }
   434	                }.start()
   435	            } else {
   436	                binding.tvActiveTimer.visibility = View.GONE
   437	            }
   438	        }
   439	
   440	        private fun setupPomodoroTimer(block: AppBlock) {
   441	            if (block.blockingStyle != UnlockMethodUtils.STYLE_POMODORO) {
   442	                return
   443	            }
   444	            val state = UnlockMethodUtils.computePomodoroState(block)
   445	            if (!state.isSessionActive) {
   446	                if (block.blockNowUntil <= System.currentTimeMillis()) {
   447	                    binding.tvBlockNowTimer.visibility = View.GONE
   448	                }
   449	                return
   450	            }
   451	            binding.tvBlockNowTimer.visibility = View.VISIBLE
   452	            val label = if (state.isInFocus) "🎯" else "☕"
   453	            binding.tvBlockNowTimer.text = "$label ${formatDuration(state.periodRemainingMs)} remaining"
   454	            pomodoroTimer = object : CountDownTimer(state.periodRemainingMs, 1000L) {
   455	                override fun onTick(ms: Long) {
   456	                    binding.tvBlockNowTimer.text = "$label ${formatDuration(ms)} remaining"
   457	                }
   458	
   459	                override fun onFinish() {
   460	                    binding.tvBlockNowTimer.visibility = View.GONE
   461	                }
   462	            }.start()
   463	        }
   464	
   465	        private fun bindSchedulePomodoroPhase(block: AppBlock, timeBlocks: List<TimeBlock>) {
   466	            if (boundBlockId != block.id) return
   467	            val phase = UnlockMethodUtils.computeSchedulePomodoroPhase(block, timeBlocks)
   468	            if (!phase.isActive || phase.phaseRemainingMs <= 0L) {
   469	                binding.tvTimeRange.text = "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
   470	                return
   471	            }
   472	
   473	            val phaseEmoji = if (phase.isInFocus) "\uD83C\uDFAF" else "\u2615"
   474	            val phaseWord = if (phase.isInFocus) "Focus" else "Break"
   475	            binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(phase.phaseRemainingMs)}"
   476	
   477	            usageStatusTimer?.cancel()
   478	            usageStatusTimer = object : CountDownTimer(phase.phaseRemainingMs, 1000L) {
   479	                override fun onTick(ms: Long) {
   480	                    binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(ms)}"
   481	                }
   482	
   483	                override fun onFinish() {
   484	                    scope.launch {
   485	                        bindSchedulePomodoroPhase(block, timeBlocks)
   486	                    }
   487	                }
   488	            }.start()
   489	        }
   490	
   491	        private fun showPopupMenu(anchor: View, block: AppBlock) {
   492	            val popup = PopupMenu(anchor.context, anchor)
   493	            popup.menuInflater.inflate(R.menu.menu_block_overflow, popup.menu)
   494	
   495	            val now = System.currentTimeMillis()
   496	            val isPaused = block.pausedUntil > now || block.pausedUntil == Long.MAX_VALUE
   497	            popup.menu.findItem(R.id.action_pause)?.title = if (isPaused) "Unpause" else "Pause"
   498	
   499	            val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
   500	                UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE
   501	
   502	            popup.menu.findItem(R.id.action_pause)?.isVisible = !isManualNoMethod
   503	            popup.menu.findItem(R.id.action_block_now)?.isVisible = !isManualNoMethod
   504	            popup.menu.findItem(R.id.action_lock_with_timer)?.isVisible = isManualNoMethod
   505	
   506	            popup.setOnMenuItemClickListener { item ->
   507	                when (item.itemId) {
   508	                    R.id.action_pause -> {
   509	                        if (isPaused) onRestartFromPause(block) else onPause(block)
   510	                        true
   511	                    }
   512	                    R.id.action_block_now -> {
   513	                        onBlockNow(block)
   514	                        true
   515	                    }
   516	                    R.id.action_lock_with_timer -> {
   517	                        onLockWithTimer(block)
   518	                        true
   519	                    }
   520	                    R.id.action_move_to_folder -> {
   521	                        onMoveToFolder(block)
   522	                        true
   523	                    }
   524	                    R.id.action_edit -> {
   525	                        onEdit(block)
   526	                        true
   527	                    }
   528	                    R.id.action_archive -> {
   529	                        onArchive(block)
   530	                        true
   531	                    }
   532	                    R.id.action_delete -> {
   533	                        onDelete(block)
   534	                        true
   535	                    }
   536	                    else -> false
   537	                }
   538	            }
   539	            popup.show()
   540	        }
   541	
   542	        fun cancelTimer() {
   543	            countDownTimer?.cancel()
   544	            countDownTimer = null
   545	            blockNowTimer?.cancel()
   546	            blockNowTimer = null
   547	            lockTimer?.cancel()
   548	            lockTimer = null
   549	            activeTimer?.cancel()
   550	            activeTimer = null
   551	            pomodoroTimer?.cancel()
   552	            pomodoroTimer = null
   553	            usageStatusTimer?.cancel()
   554	            usageStatusTimer = null
   555	            iconLoadJob?.cancel()
   556	            iconLoadJob = null
   557	            usageQueryJob?.cancel()
   558	            usageQueryJob = null
   559	            boundBlockId = null
   560	        }
   561	    }
   562	
   563	    private data class WaitTimerStatus(val text: String, val blockingRemainingMs: Long)
   564	
   565	    override fun getItemViewType(position: Int): Int {
   566	        return when (getItem(position)) {
   567	            is HomeListItem.FolderHeader -> VIEW_TYPE_FOLDER
   568	            is HomeListItem.BlockItem -> VIEW_TYPE_BLOCK
   569	        }
   570	    }
   571	
   572	    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
   573	        return when (viewType) {
   574	            VIEW_TYPE_FOLDER -> FolderViewHolder(ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
   575	            else -> BlockViewHolder(ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false))
   576	        }
   577	    }
   578	
   579	    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
   580	        when (val item = getItem(position)) {
   581	            is HomeListItem.FolderHeader -> (holder as FolderViewHolder).bind(item)
   582	            is HomeListItem.BlockItem -> (holder as BlockViewHolder).bind(item)
   583	        }
   584	    }
   585	
   586	    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
   587	        super.onViewRecycled(holder)
   588	        if (holder is BlockViewHolder) {
   589	            holder.cancelTimer()
   590	        }
   591	    }
   592	
   593	    private fun buildFolderInfo(context: Context, folder: BlockFolder, blockCount: Int): String {
   594	        val blockCountText = if (blockCount == 1) "1 block" else "$blockCount blocks"
   595	        val lockText = when (folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }) {
   596	            UnlockMethodUtils.METHOD_DELAY -> "${context.getString(R.string.unlock_method_delay)} lock"
   597	            UnlockMethodUtils.METHOD_PASSWORD -> "${context.getString(R.string.unlock_method_password)} lock"
   598	            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> "${context.getString(R.string.unlock_method_type_over)} lock"
   599	            UnlockMethodUtils.METHOD_QR_CODE -> "${context.getString(R.string.unlock_method_qr_code)} lock"
   600	            UnlockMethodUtils.METHOD_EDIT_WINDOW -> "${context.getString(R.string.unlock_method_edit_window)} lock"
   601	            UnlockMethodUtils.METHOD_TIMER -> "${context.getString(R.string.unlock_method_timer)} lock"
   602	            else -> null
   603	        }
   604	        return if (lockText.isNullOrBlank()) blockCountText else "$blockCountText · $lockText"
   605	    }
   606	
   607	    private fun computeUsageLimitRemaining(context: Context, block: AppBlock): String {
   608	        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
   609	            ?: return "${block.usageLimitMinutes}m left"
   610	
   611	        val now = System.currentTimeMillis()
   612	        val startTime = when (block.usageLimitPeriod) {
   613	            "HOURLY" -> now - 3_600_000L
   614	            else -> {
   615	                java.util.Calendar.getInstance().apply {
   616	                    set(java.util.Calendar.HOUR_OF_DAY, 0)
   617	                    set(java.util.Calendar.MINUTE, 0)
   618	                    set(java.util.Calendar.SECOND, 0)
   619	                    set(java.util.Calendar.MILLISECOND, 0)
   620	                }.timeInMillis
   621	            }
   622	        }
   623	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   624	        val events = usageStatsManager.queryEvents(startTime, now)
   625	        val event = android.app.usage.UsageEvents.Event()
   626	        val foregroundStartTimes = mutableMapOf<String, Long>()
   627	        var totalUsageMs = 0L
   628	
   629	        while (events.hasNextEvent()) {
   630	            events.getNextEvent(event)
   631	            val pkg = event.packageName ?: continue
   632	            if (pkg !in packages) continue
   633	            when (event.eventType) {
   634	                android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
   635	                    foregroundStartTimes[pkg] = event.timeStamp
   636	                }
   637	                android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
   638	                    val start = foregroundStartTimes.remove(pkg)
   639	                    if (start != null) {
   640	                        totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
   641	                    }
   642	                }
   643	            }
   644	        }
   645	        for ((_, start) in foregroundStartTimes) {
   646	            totalUsageMs += (now - start).coerceAtLeast(0L)
   647	        }
   648	
   649	        val limitMs = block.usageLimitMinutes * 60_000L
   650	        val remainingMs = (limitMs - totalUsageMs).coerceAtLeast(0L)
   651	        val remainingMin = remainingMs / 60_000L
   652	
   653	        val periodLabel = if (block.usageLimitPeriod == "HOURLY") "this hour" else "today"
   654	        return if (remainingMs <= 0L) {
   655	            "Limit reached"
   656	        } else {
   657	            "${remainingMin}m left $periodLabel"
   658	        }
   659	    }
   660	
   661	    private fun computeWaitTimerStatus(@Suppress("UNUSED_PARAMETER") context: Context, block: AppBlock): WaitTimerStatus {
   662	        val now = System.currentTimeMillis()
   663	        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
   664	        val blockingUntilKey = "wait_timer_blocking_${block.id}"
   665	        val remainingKey = "wait_timer_remaining_${block.id}"
   666	        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)
   667	
   668	        if (blockingUntil > now) {
   669	            val remainingMs = blockingUntil - now
   670	            val remainingMin = remainingMs / 60_000L
   671	            return WaitTimerStatus("Blocked for ${remainingMin}m", remainingMs)
   672	        }
   673	
   674	        val remaining = kv.decodeLong(remainingKey, -1L)
   675	        val maxMs = block.waitTimerUseMinutes * 60_000L
   676	        val actualRemaining = if (remaining < 0L) maxMs else remaining
   677	        val remainingMin = actualRemaining / 60_000L
   678	        val remainingSec = (actualRemaining % 60_000L) / 1000L
   679	
   680	        return if (actualRemaining <= 0L) {
   681	            WaitTimerStatus("Block pending", 0L)
   682	        } else {
   683	            val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
   684	            WaitTimerStatus("${remainingMin}m ${remainingSec}s left ($modeLabel)", 0L)
   685	        }
   686	    }
   687	
   688	    private fun formatDuration(millis: Long): String {
   689	        val totalSeconds = millis / 1000
   690	        val hours = totalSeconds / 3600
   691	        val minutes = (totalSeconds % 3600) / 60
   692	        val seconds = totalSeconds % 60
   693	        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
   694	        else String.format("%d:%02d", minutes, seconds)
   695	    }
   696	
   697	    private fun formatPhaseDuration(ms: Long): String {
   698	        val min = ms / 60_000L
   699	        val sec = (ms % 60_000L) / 1000L
   700	        return "${min}m ${sec}s"
   701	    }
   702	
   703	    private fun dpToPx(value: Int): Int {
   704	        return (value * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
   705	    }
   706	}
   707	