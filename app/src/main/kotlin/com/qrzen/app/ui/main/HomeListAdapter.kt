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
    45	    private val onFolderToggle: (BlockFolder, Boolean) -> Boolean,
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
    96	        private var pauseTimer: CountDownTimer? = null
    97	
    98	        fun bind(item: HomeListItem.FolderHeader) {
    99	            val folder = item.folder
   100	            pauseTimer?.cancel()
   101	            pauseTimer = null
   102	
   103	            binding.tvFolderTitle.text = folder.title
   104	            binding.tvFolderInfo.text = buildFolderInfo(binding.root.context, folder, item.blockCount)
   105	            binding.ivFolderLock.visibility = if (
   106	                folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE } == UnlockMethodUtils.METHOD_NONE
   107	            ) {
   108	                View.GONE
   109	            } else {
   110	                View.VISIBLE
   111	            }
   112	            binding.ivExpandCollapse.setImageResource(
   113	                if (folder.isCollapsed) R.drawable.ic_expand_more else R.drawable.ic_expand_less
   114	            )
   115	
   116	            setupPauseTimer(folder)
   117	
   118	            binding.switchFolderEnabled.setOnCheckedChangeListener(null)
   119	            binding.switchFolderEnabled.isChecked = folder.isEnabled
   120	            bindFolderToggleListener(folder)
   121	
   122	            binding.root.setOnClickListener { onFolderExpandCollapse(folder) }
   123	            binding.ivExpandCollapse.setOnClickListener { onFolderExpandCollapse(folder) }
   124	            binding.btnFolderOverflow.setOnClickListener { showFolderPopupMenu(it, folder) }
   125	        }
   126	
   127	        private fun bindFolderToggleListener(folder: BlockFolder) {
   128	            binding.switchFolderEnabled.setOnCheckedChangeListener { _, isChecked ->
   129	                val accepted = onFolderToggle(folder, isChecked)
   130	                if (!accepted) {
   131	                    binding.switchFolderEnabled.setOnCheckedChangeListener(null)
   132	                    binding.switchFolderEnabled.isChecked = folder.isEnabled
   133	                    bindFolderToggleListener(folder)
   134	                }
   135	            }
   136	        }
   137	
   138	        private fun setupPauseTimer(folder: BlockFolder) {
   139	            val now = System.currentTimeMillis()
   140	            if (folder.pausedUntil == Long.MAX_VALUE) {
   141	                binding.tvFolderPauseTimer.visibility = View.VISIBLE
   142	                binding.tvFolderPauseTimer.text = "⏸ Paused indefinitely"
   143	                return
   144	            }
   145	            if (folder.pausedUntil > now) {
   146	                val remaining = folder.pausedUntil - now
   147	                binding.tvFolderPauseTimer.visibility = View.VISIBLE
   148	                binding.tvFolderPauseTimer.text = "⏸ Paused – ${formatDuration(remaining)} remaining"
   149	                pauseTimer = object : CountDownTimer(remaining, 1000L) {
   150	                    override fun onTick(millisUntilFinished: Long) {
   151	                        binding.tvFolderPauseTimer.text = "⏸ Paused – ${formatDuration(millisUntilFinished)} remaining"
   152	                    }
   153	
   154	                    override fun onFinish() {
   155	                        binding.tvFolderPauseTimer.visibility = View.GONE
   156	                    }
   157	                }.start()
   158	            } else {
   159	                binding.tvFolderPauseTimer.visibility = View.GONE
   160	            }
   161	        }
   162	
   163	        private fun showFolderPopupMenu(anchor: View, folder: BlockFolder) {
   164	            val popup = PopupMenu(anchor.context, anchor)
   165	            popup.menuInflater.inflate(R.menu.menu_folder_overflow, popup.menu)
   166	            popup.menu.findItem(R.id.action_pause_folder)?.title = if (folder.pausedUntil > System.currentTimeMillis()) {
   167	                "Unpause"
   168	            } else {
   169	                "Pause"
   170	            }
   171	            popup.setOnMenuItemClickListener { item ->
   172	                when (item.itemId) {
   173	                    R.id.action_pause_folder -> {
   174	                        onFolderPause(folder)
   175	                        true
   176	                    }
   177	                    R.id.action_edit_folder -> {
   178	                        onFolderEdit(folder)
   179	                        true
   180	                    }
   181	                    R.id.action_delete_folder -> {
   182	                        onFolderDelete(folder)
   183	                        true
   184	                    }
   185	                    else -> false
   186	                }
   187	            }
   188	            popup.show()
   189	        }
   190	
   191	        fun cancelTimer() {
   192	            pauseTimer?.cancel()
   193	            pauseTimer = null
   194	        }
   195	    }
   196	
   197	    inner class BlockViewHolder(private val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
   198	        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
   199	        private var countDownTimer: CountDownTimer? = null
   200	        private var blockNowTimer: CountDownTimer? = null
   201	        private var lockTimer: CountDownTimer? = null
   202	        private var activeTimer: CountDownTimer? = null
   203	        private var pomodoroTimer: CountDownTimer? = null
   204	        private var usageStatusTimer: CountDownTimer? = null
   205	        private var iconLoadJob: Job? = null
   206	        private var usageQueryJob: Job? = null
   207	        private var boundBlockId: Int? = null
   208	        private var boundPackages: List<String> = emptyList()
   209	
   210	        fun bind(item: HomeListItem.BlockItem) {
   211	            val block = item.block
   212	
   213	            countDownTimer?.cancel()
   214	            countDownTimer = null
   215	            blockNowTimer?.cancel()
   216	            blockNowTimer = null
   217	            lockTimer?.cancel()
   218	            lockTimer = null
   219	            activeTimer?.cancel()
   220	            activeTimer = null
   221	            pomodoroTimer?.cancel()
   222	            pomodoroTimer = null
   223	            usageStatusTimer?.cancel()
   224	            usageStatusTimer = null
   225	            iconLoadJob?.cancel()
   226	            iconLoadJob = null
   227	            usageQueryJob?.cancel()
   228	            usageQueryJob = null
   229	
   230	            updateIndent(item.isInFolder)
   231	
   232	            boundBlockId = block.id
   233	            binding.tvTitle.text = block.title
   234	            val modePrefix = if (block.isAllowlistMode) "Allowlist" else "Blocklist"
   235	            when (block.blockingStyle) {
   236	                UnlockMethodUtils.STYLE_MANUAL -> {
   237	                    binding.tvTimeRange.text = "Manual"
   238	                    binding.tvDays.text = modePrefix
   239	                }
   240	                UnlockMethodUtils.STYLE_SCHEDULE -> {
   241	                    val timeRangeText = when (block.scheduleBreakType) {
   242	                        UnlockMethodUtils.BREAK_POMODORO -> {
   243	                            usageQueryJob = scope.launch {
   244	                                val timeBlocks = withContext(Dispatchers.IO) {
   245	                                    timeBlockDao.getByBlockId(block.id)
   246	                                }
   247	                                bindSchedulePomodoroPhase(block, timeBlocks)
   248	                            }
   249	                            "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
   250	                        }
   251	                        UnlockMethodUtils.BREAK_WAIT_TIMER -> {
   252	                            val adaptiveSuffix = if (block.waitTimerAdaptive) " · adaptive" else ""
   253	                            "Scheduled · Wait Timer (${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block$adaptiveSuffix)"
   254	                        }
   255	                        UnlockMethodUtils.BREAK_USAGE_LIMIT -> {
   256	                            val period = if (block.usageLimitPeriod == "HOURLY") "hour" else "day"
   257	                            "Scheduled · Usage Limit (${block.usageLimitMinutes} min/$period)"
   258	                        }
   259	                        UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE ->
   260	                            "Scheduled · Allowance (${block.scheduledAllowanceMinutes} min/window)"
   261	                        else -> "Scheduled"
   262	                    }
   263	                    binding.tvTimeRange.text = timeRangeText
   264	                    binding.tvDays.text = modePrefix
   265	                }
   266	                UnlockMethodUtils.STYLE_USAGE_LIMIT -> {
   267	                    val period = if (block.usageLimitPeriod == "HOURLY") "per hour" else "per day"
   268	                    binding.tvTimeRange.text = "${block.usageLimitMinutes} min $period"
   269	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   270	
   271	                    usageQueryJob = scope.launch {
   272	                        val remainingText = withContext(Dispatchers.IO) {
   273	                            computeUsageLimitRemaining(binding.root.context, block)
   274	                        }
   275	                        binding.tvTimeRange.text = remainingText
   276	                    }
   277	                }
   278	                UnlockMethodUtils.STYLE_WAIT_TIMER -> {
   279	                    val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
   280	                    binding.tvTimeRange.text = "${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block ($modeLabel)"
   281	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   282	
   283	                    usageQueryJob = scope.launch {
   284	                        val status = withContext(Dispatchers.IO) {
   285	                            computeWaitTimerStatus(binding.root.context, block)
   286	                        }
   287	                        binding.tvTimeRange.text = status.text
   288	
   289	                        if (status.blockingRemainingMs > 0L) {
   290	                            usageStatusTimer?.cancel()
   291	                            usageStatusTimer = object : CountDownTimer(status.blockingRemainingMs, 1000L) {
   292	                                override fun onTick(ms: Long) {
   293	                                    val min = ms / 60_000
   294	                                    val sec = (ms % 60_000) / 1000
   295	                                    binding.tvTimeRange.text = "Blocked for ${min}m ${sec}s"
   296	                                }
   297	
   298	                                override fun onFinish() {
   299	                                    binding.tvTimeRange.text = computeWaitTimerStatus(binding.root.context, block).text
   300	                                }
   301	                            }.start()
   302	                        }
   303	                    }
   304	                }
   305	                UnlockMethodUtils.STYLE_POMODORO -> {
   306	                    val state = UnlockMethodUtils.computePomodoroState(block)
   307	                    if (state.isSessionActive) {
   308	                        if (state.isInFocus) {
   309	                            binding.tvTimeRange.text = "🎯 Focus ${state.currentRound}/${state.totalRounds}"
   310	                        } else {
   311	                            binding.tvTimeRange.text = "☕ Break ${state.currentRound}/${state.totalRounds}"
   312	                        }
   313	                    } else {
   314	                        binding.tvTimeRange.text = "${block.pomodoroDurationMin}m focus / ${block.pomodoroBreakMin}m break"
   315	                    }
   316	                    binding.tvDays.text = modePrefix
   317	                }
   318	                else -> {
   319	                    binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
   320	                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
   321	                }
   322	            }
   323	
   324	            val unlockSummary = UnlockMethodUtils.getUnlockMethodSummary(binding.root.context, block)
   325	            binding.tvUnlockMethod.visibility = if (unlockSummary.isNullOrBlank()) View.GONE else View.VISIBLE
   326	            binding.tvUnlockMethod.text = unlockSummary.orEmpty()
   327	
   328	            binding.switchEnabled.setOnCheckedChangeListener(null)
   329	            binding.switchEnabled.isChecked = block.isEnabled
   330	            bindToggleListener(block)
   331	
   332	            binding.btnOverflow.setOnClickListener { view ->
   333	                showPopupMenu(view, block)
   334	            }
   335	
   336	            setupPauseTimer(block)
   337	            setupBlockNowTimer(block)
   338	            setupLockTimer(block)
   339	            setupActiveTimer(block)
   340	            setupPomodoroTimer(block)
   341	
   342	            val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
   343	            boundPackages = packages
   344	            if (packages.isEmpty()) {
   345	                binding.rvBlockApps.visibility = View.GONE
   346	                binding.rvBlockApps.adapter = null
   347	                return
   348	            }
   349	
   350	            binding.rvBlockApps.visibility = View.VISIBLE
   351	            binding.rvBlockApps.adapter = null
   352	            if (binding.rvBlockApps.layoutManager == null) {
   353	                binding.rvBlockApps.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
   354	            }
   355	
   356	            val pm = binding.root.context.packageManager
   357	            iconLoadJob = scope.launch {
   358	                val icons = withContext(Dispatchers.IO) {
   359	                    packages.mapNotNull { pkg ->
   360	                        try {
   361	                            val appInfo = pm.getApplicationInfo(pkg, 0)
   362	                            BlockAppIcon(pkg, pm.getApplicationIcon(appInfo))
   363	                        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
   364	                            null
   365	                        }
   366	                    }
   367	                }
   368	                if (packages != boundPackages) return@launch
   369	                if (icons.isEmpty()) {
   370	                    binding.rvBlockApps.visibility = View.GONE
   371	                    binding.rvBlockApps.adapter = null
   372	                } else {
   373	                    binding.rvBlockApps.visibility = View.VISIBLE
   374	                    binding.rvBlockApps.adapter = BlockAppsIconAdapter(icons)
   375	                }
   376	            }
   377	        }
   378	
   379	        private fun updateIndent(isInFolder: Boolean) {
   380	            val layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)
   381	                ?: RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
   382	            layoutParams.marginStart = dpToPx(if (isInFolder) 24 else 8)
   383	            layoutParams.marginEnd = dpToPx(8)
   384	            binding.root.layoutParams = layoutParams
   385	        }
   386	
   387	        private fun bindToggleListener(block: AppBlock) {
   388	            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
   389	                val accepted = onToggle(block, checked)
   390	                if (!accepted) {
   391	                    binding.switchEnabled.setOnCheckedChangeListener(null)
   392	                    binding.switchEnabled.isChecked = block.isEnabled
   393	                    bindToggleListener(block)
   394	                }
   395	            }
   396	        }
   397	
   398	        private fun setupPauseTimer(block: AppBlock) {
   399	            val now = System.currentTimeMillis()
   400	            val isPaused = block.pausedUntil > now
   401	            val isIndefinite = block.pausedUntil == Long.MAX_VALUE
   402	
   403	            if (isPaused || isIndefinite) {
   404	                binding.tvPauseTimer.visibility = View.VISIBLE
   405	                binding.tvPauseTimer.setOnClickListener {
   406	                    onRestartFromPause(block)
   407	                }
   408	
   409	                if (isIndefinite) {
   410	                    binding.tvPauseTimer.text = "⏸ Paused indefinitely • Tap to restart"
   411	                } else {
   412	                    val remaining = block.pausedUntil - now
   413	                    binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(remaining)} remaining"
   414	                    countDownTimer = object : CountDownTimer(remaining, 1000L) {
   415	                        override fun onTick(millisUntilFinished: Long) {
   416	                            binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(millisUntilFinished)} remaining"
   417	                        }
   418	
   419	                        override fun onFinish() {
   420	                            binding.tvPauseTimer.visibility = View.GONE
   421	                        }
   422	                    }.start()
   423	                }
   424	            } else {
   425	                binding.tvPauseTimer.visibility = View.GONE
   426	            }
   427	        }
   428	
   429	        private fun setupBlockNowTimer(block: AppBlock) {
   430	            val now = System.currentTimeMillis()
   431	            if (block.blockNowUntil > now) {
   432	                binding.tvBlockNowTimer.visibility = View.VISIBLE
   433	                val remaining = block.blockNowUntil - now
   434	                binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(remaining)}"
   435	                blockNowTimer = object : CountDownTimer(remaining, 1000L) {
   436	                    override fun onTick(ms: Long) {
   437	                        binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(ms)}"
   438	                    }
   439	
   440	                    override fun onFinish() {
   441	                        binding.tvBlockNowTimer.visibility = View.GONE
   442	                    }
   443	                }.start()
   444	            } else {
   445	                binding.tvBlockNowTimer.visibility = View.GONE
   446	            }
   447	        }
   448	
   449	        private fun setupLockTimer(block: AppBlock) {
   450	            val now = System.currentTimeMillis()
   451	            if (block.toggleLockUntil > now) {
   452	                binding.tvLockTimer.visibility = View.VISIBLE
   453	                val remaining = block.toggleLockUntil - now
   454	                binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(remaining)}"
   455	                lockTimer = object : CountDownTimer(remaining, 1000L) {
   456	                    override fun onTick(ms: Long) {
   457	                        binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(ms)}"
   458	                    }
   459	
   460	                    override fun onFinish() {
   461	                        binding.tvLockTimer.visibility = View.GONE
   462	                    }
   463	                }.start()
   464	            } else {
   465	                binding.tvLockTimer.visibility = View.GONE
   466	            }
   467	        }
   468	
   469	        private fun setupActiveTimer(block: AppBlock) {
   470	            val now = System.currentTimeMillis()
   471	            if (block.activeUntil > now) {
   472	                binding.tvActiveTimer.visibility = View.VISIBLE
   473	                val remaining = block.activeUntil - now
   474	                val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
   475	                val endsAt = sdf.format(java.util.Date(block.activeUntil))
   476	                binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(remaining)} (until $endsAt)"
   477	                activeTimer = object : CountDownTimer(remaining, 1000L) {
   478	                    override fun onTick(ms: Long) {
   479	                        binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(ms)} (until $endsAt)"
   480	                    }
   481	
   482	                    override fun onFinish() {
   483	                        binding.tvActiveTimer.visibility = View.GONE
   484	                    }
   485	                }.start()
   486	            } else {
   487	                binding.tvActiveTimer.visibility = View.GONE
   488	            }
   489	        }
   490	
   491	        private fun setupPomodoroTimer(block: AppBlock) {
   492	            if (block.blockingStyle != UnlockMethodUtils.STYLE_POMODORO) {
   493	                return
   494	            }
   495	            val state = UnlockMethodUtils.computePomodoroState(block)
   496	            if (!state.isSessionActive) {
   497	                if (block.blockNowUntil <= System.currentTimeMillis()) {
   498	                    binding.tvBlockNowTimer.visibility = View.GONE
   499	                }
   500	                return
   501	            }
   502	            binding.tvBlockNowTimer.visibility = View.VISIBLE
   503	            val label = if (state.isInFocus) "🎯" else "☕"
   504	            binding.tvBlockNowTimer.text = "$label ${formatDuration(state.periodRemainingMs)} remaining"
   505	            pomodoroTimer = object : CountDownTimer(state.periodRemainingMs, 1000L) {
   506	                override fun onTick(ms: Long) {
   507	                    binding.tvBlockNowTimer.text = "$label ${formatDuration(ms)} remaining"
   508	                }
   509	
   510	                override fun onFinish() {
   511	                    binding.tvBlockNowTimer.visibility = View.GONE
   512	                }
   513	            }.start()
   514	        }
   515	
   516	        private fun bindSchedulePomodoroPhase(block: AppBlock, timeBlocks: List<TimeBlock>) {
   517	            if (boundBlockId != block.id) return
   518	            val phase = UnlockMethodUtils.computeSchedulePomodoroPhase(block, timeBlocks)
   519	            if (!phase.isActive || phase.phaseRemainingMs <= 0L) {
   520	                binding.tvTimeRange.text = "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
   521	                return
   522	            }
   523	
   524	            val phaseEmoji = if (phase.isInFocus) "\uD83C\uDFAF" else "\u2615"
   525	            val phaseWord = if (phase.isInFocus) "Focus" else "Break"
   526	            binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(phase.phaseRemainingMs)}"
   527	
   528	            usageStatusTimer?.cancel()
   529	            usageStatusTimer = object : CountDownTimer(phase.phaseRemainingMs, 1000L) {
   530	                override fun onTick(ms: Long) {
   531	                    binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(ms)}"
   532	                }
   533	
   534	                override fun onFinish() {
   535	                    scope.launch {
   536	                        bindSchedulePomodoroPhase(block, timeBlocks)
   537	                    }
   538	                }
   539	            }.start()
   540	        }
   541	
   542	        private fun showPopupMenu(anchor: View, block: AppBlock) {
   543	            val popup = PopupMenu(anchor.context, anchor)
   544	            popup.menuInflater.inflate(R.menu.menu_block_overflow, popup.menu)
   545	
   546	            val now = System.currentTimeMillis()
   547	            val isPaused = block.pausedUntil > now || block.pausedUntil == Long.MAX_VALUE
   548	            popup.menu.findItem(R.id.action_pause)?.title = if (isPaused) "Unpause" else "Pause"
   549	
   550	            val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
   551	                UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE
   552	
   553	            popup.menu.findItem(R.id.action_pause)?.isVisible = !isManualNoMethod
   554	            popup.menu.findItem(R.id.action_block_now)?.isVisible = !isManualNoMethod
   555	            popup.menu.findItem(R.id.action_lock_with_timer)?.isVisible = isManualNoMethod
   556	
   557	            popup.setOnMenuItemClickListener { item ->
   558	                when (item.itemId) {
   559	                    R.id.action_pause -> {
   560	                        if (isPaused) onRestartFromPause(block) else onPause(block)
   561	                        true
   562	                    }
   563	                    R.id.action_block_now -> {
   564	                        onBlockNow(block)
   565	                        true
   566	                    }
   567	                    R.id.action_lock_with_timer -> {
   568	                        onLockWithTimer(block)
   569	                        true
   570	                    }
   571	                    R.id.action_move_to_folder -> {
   572	                        onMoveToFolder(block)
   573	                        true
   574	                    }
   575	                    R.id.action_edit -> {
   576	                        onEdit(block)
   577	                        true
   578	                    }
   579	                    R.id.action_archive -> {
   580	                        onArchive(block)
   581	                        true
   582	                    }
   583	                    R.id.action_delete -> {
   584	                        onDelete(block)
   585	                        true
   586	                    }
   587	                    else -> false
   588	                }
   589	            }
   590	            popup.show()
   591	        }
   592	
   593	        fun cancelTimer() {
   594	            countDownTimer?.cancel()
   595	            countDownTimer = null
   596	            blockNowTimer?.cancel()
   597	            blockNowTimer = null
   598	            lockTimer?.cancel()
   599	            lockTimer = null
   600	            activeTimer?.cancel()
   601	            activeTimer = null
   602	            pomodoroTimer?.cancel()
   603	            pomodoroTimer = null
   604	            usageStatusTimer?.cancel()
   605	            usageStatusTimer = null
   606	            iconLoadJob?.cancel()
   607	            iconLoadJob = null
   608	            usageQueryJob?.cancel()
   609	            usageQueryJob = null
   610	            boundBlockId = null
   611	        }
   612	    }
   613	
   614	    private data class WaitTimerStatus(val text: String, val blockingRemainingMs: Long)
   615	
   616	    override fun getItemViewType(position: Int): Int {
   617	        return when (getItem(position)) {
   618	            is HomeListItem.FolderHeader -> VIEW_TYPE_FOLDER
   619	            is HomeListItem.BlockItem -> VIEW_TYPE_BLOCK
   620	        }
   621	    }
   622	
   623	    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
   624	        return when (viewType) {
   625	            VIEW_TYPE_FOLDER -> FolderViewHolder(ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
   626	            else -> BlockViewHolder(ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false))
   627	        }
   628	    }
   629	
   630	    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
   631	        when (val item = getItem(position)) {
   632	            is HomeListItem.FolderHeader -> (holder as FolderViewHolder).bind(item)
   633	            is HomeListItem.BlockItem -> (holder as BlockViewHolder).bind(item)
   634	        }
   635	    }
   636	
   637	    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
   638	        super.onViewRecycled(holder)
   639	        when (holder) {
   640	            is FolderViewHolder -> holder.cancelTimer()
   641	            is BlockViewHolder -> holder.cancelTimer()
   642	        }
   643	    }
   644	
   645	    private fun buildFolderInfo(context: Context, folder: BlockFolder, blockCount: Int): String {
   646	        val blockCountText = if (blockCount == 1) "1 block" else "$blockCount blocks"
   647	        val lockText = when (folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }) {
   648	            UnlockMethodUtils.METHOD_DELAY -> "${context.getString(R.string.unlock_method_delay)} lock"
   649	            UnlockMethodUtils.METHOD_PASSWORD -> "${context.getString(R.string.unlock_method_password)} lock"
   650	            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> "${context.getString(R.string.unlock_method_type_over)} lock"
   651	            UnlockMethodUtils.METHOD_QR_CODE -> "${context.getString(R.string.unlock_method_qr_code)} lock"
   652	            UnlockMethodUtils.METHOD_EDIT_WINDOW -> "${context.getString(R.string.unlock_method_edit_window)} lock"
   653	            UnlockMethodUtils.METHOD_TIMER -> "${context.getString(R.string.unlock_method_timer)} lock"
   654	            else -> null
   655	        }
   656	        return if (lockText.isNullOrBlank()) blockCountText else "$blockCountText · $lockText"
   657	    }
   658	
   659	    private fun computeUsageLimitRemaining(context: Context, block: AppBlock): String {
   660	        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
   661	            ?: return "${block.usageLimitMinutes}m left"
   662	
   663	        val now = System.currentTimeMillis()
   664	        val startTime = when (block.usageLimitPeriod) {
   665	            "HOURLY" -> now - 3_600_000L
   666	            else -> {
   667	                java.util.Calendar.getInstance().apply {
   668	                    set(java.util.Calendar.HOUR_OF_DAY, 0)
   669	                    set(java.util.Calendar.MINUTE, 0)
   670	                    set(java.util.Calendar.SECOND, 0)
   671	                    set(java.util.Calendar.MILLISECOND, 0)
   672	                }.timeInMillis
   673	            }
   674	        }
   675	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   676	        val events = usageStatsManager.queryEvents(startTime, now)
   677	        val event = android.app.usage.UsageEvents.Event()
   678	        val foregroundStartTimes = mutableMapOf<String, Long>()
   679	        var totalUsageMs = 0L
   680	
   681	        while (events.hasNextEvent()) {
   682	            events.getNextEvent(event)
   683	            val pkg = event.packageName ?: continue
   684	            if (pkg !in packages) continue
   685	            when (event.eventType) {
   686	                android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
   687	                    foregroundStartTimes[pkg] = event.timeStamp
   688	                }
   689	                android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
   690	                    val start = foregroundStartTimes.remove(pkg)
   691	                    if (start != null) {
   692	                        totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
   693	                    }
   694	                }
   695	            }
   696	        }
   697	        for ((_, start) in foregroundStartTimes) {
   698	            totalUsageMs += (now - start).coerceAtLeast(0L)
   699	        }
   700	
   701	        val limitMs = block.usageLimitMinutes * 60_000L
   702	        val remainingMs = (limitMs - totalUsageMs).coerceAtLeast(0L)
   703	        val remainingMin = remainingMs / 60_000L
   704	
   705	        val periodLabel = if (block.usageLimitPeriod == "HOURLY") "this hour" else "today"
   706	        return if (remainingMs <= 0L) {
   707	            "Limit reached"
   708	        } else {
   709	            "${remainingMin}m left $periodLabel"
   710	        }
   711	    }
   712	
   713	    private fun computeWaitTimerStatus(@Suppress("UNUSED_PARAMETER") context: Context, block: AppBlock): WaitTimerStatus {
   714	        val now = System.currentTimeMillis()
   715	        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
   716	        val blockingUntilKey = "wait_timer_blocking_${block.id}"
   717	        val remainingKey = "wait_timer_remaining_${block.id}"
   718	        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)
   719	
   720	        if (blockingUntil > now) {
   721	            val remainingMs = blockingUntil - now
   722	            val remainingMin = remainingMs / 60_000L
   723	            return WaitTimerStatus("Blocked for ${remainingMin}m", remainingMs)
   724	        }
   725	
   726	        val remaining = kv.decodeLong(remainingKey, -1L)
   727	        val maxMs = block.waitTimerUseMinutes * 60_000L
   728	        val actualRemaining = if (remaining < 0L) maxMs else remaining
   729	        val remainingMin = actualRemaining / 60_000L
   730	        val remainingSec = (actualRemaining % 60_000L) / 1000L
   731	
   732	        return if (actualRemaining <= 0L) {
   733	            WaitTimerStatus("Block pending", 0L)
   734	        } else {
   735	            val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
   736	            WaitTimerStatus("${remainingMin}m ${remainingSec}s left ($modeLabel)", 0L)
   737	        }
   738	    }
   739	
   740	    private fun formatDuration(millis: Long): String {
   741	        val totalSeconds = millis / 1000
   742	        val hours = totalSeconds / 3600
   743	        val minutes = (totalSeconds % 3600) / 60
   744	        val seconds = totalSeconds % 60
   745	        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
   746	        else String.format("%d:%02d", minutes, seconds)
   747	    }
   748	
   749	    private fun formatPhaseDuration(ms: Long): String {
   750	        val min = ms / 60_000L
   751	        val sec = (ms % 60_000L) / 1000L
   752	        return "${min}m ${sec}s"
   753	    }
   754	
   755	    private fun dpToPx(value: Int): Int {
   756	        return (value * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
   757	    }
   758	}
   759	