     1	package com.qrzen.app.ui.main
     2	
     3	import android.accessibilityservice.AccessibilityServiceInfo
     4	import android.app.Activity
     5	import android.content.Context
     6	import android.content.Intent
     7	import android.os.Bundle
     8	import android.provider.Settings
     9	import android.view.LayoutInflater
    10	import android.view.View
    11	import android.view.ViewGroup
    12	import android.view.accessibility.AccessibilityManager
    13	import android.widget.CheckBox
    14	import android.widget.LinearLayout
    15	import android.widget.NumberPicker
    16	import android.widget.Toast
    17	import androidx.activity.result.contract.ActivityResultContracts
    18	import androidx.appcompat.app.AlertDialog
    19	import androidx.fragment.app.Fragment
    20	import androidx.fragment.app.viewModels
    21	import androidx.lifecycle.Lifecycle
    22	import androidx.lifecycle.lifecycleScope
    23	import androidx.lifecycle.repeatOnLifecycle
    24	import androidx.recyclerview.widget.LinearLayoutManager
    25	import com.qrzen.app.R
    26	import com.qrzen.app.data.db.TimeBlockDao
    27	import com.qrzen.app.data.model.AppBlock
    28	import com.qrzen.app.data.model.BlockFolder
    29	import com.qrzen.app.databinding.FragmentHomeBinding
    30	import com.qrzen.app.ui.block.EditBlockActivity
    31	import com.qrzen.app.ui.folder.EditFolderActivity
    32	import com.qrzen.app.ui.unlock.UnlockChallengeActivity
    33	import com.qrzen.app.ui.unlock.UnlockMethodUtils
    34	import dagger.hilt.android.AndroidEntryPoint
    35	import javax.inject.Inject
    36	import kotlinx.coroutines.launch
    37	import java.util.Calendar
    38	
    39	@AndroidEntryPoint
    40	class HomeFragment : Fragment() {
    41	
    42	    private data class PendingUnlockAction(
    43	        val block: AppBlock,
    44	        val action: String,
    45	        val toggleEnabledState: Boolean? = null
    46	    )
    47	
    48	    private data class PendingFolderUnlockAction(
    49	        val folder: BlockFolder,
    50	        val action: String
    51	    )
    52	
    53	    private var _binding: FragmentHomeBinding? = null
    54	    private val binding get() = _binding!!
    55	    private val viewModel: HomeViewModel by viewModels()
    56	    @Inject lateinit var timeBlockDao: TimeBlockDao
    57	    private lateinit var adapter: HomeListAdapter
    58	    private var pendingUnlockAction: PendingUnlockAction? = null
    59	    private var pendingFolderUnlockAction: PendingFolderUnlockAction? = null
    60	
    61	    private val unlockChallengeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    62	        val pending = pendingUnlockAction ?: return@registerForActivityResult
    63	        pendingUnlockAction = null
    64	        if (result.resultCode == Activity.RESULT_OK) {
    65	            completePendingUnlockAction(pending)
    66	        }
    67	    }
    68	
    69	    private val folderUnlockChallengeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    70	        val pending = pendingFolderUnlockAction ?: return@registerForActivityResult
    71	        pendingFolderUnlockAction = null
    72	        if (result.resultCode == Activity.RESULT_OK) {
    73	            completePendingFolderUnlockAction(pending)
    74	        }
    75	    }
    76	
    77	    override fun onCreateView(
    78	        inflater: LayoutInflater, container: ViewGroup?,
    79	        savedInstanceState: Bundle?
    80	    ): View {
    81	        _binding = FragmentHomeBinding.inflate(inflater, container, false)
    82	        return binding.root
    83	    }
    84	
    85	    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    86	        super.onViewCreated(view, savedInstanceState)
    87	
    88	        adapter = HomeListAdapter(
    89	            timeBlockDao = timeBlockDao,
    90	            onToggle = { block, enabled ->
    91	                if (enabled) {
    92	                    if (block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO) {
    93	                        showPomodoroActivationDialog(block)
    94	                        false
    95	                    } else {
    96	                        val isManualAllowlist = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
    97	                            block.isAllowlistMode
    98	                        if (isManualAllowlist) {
    99	                            showAllowlistDurationPicker(block)
   100	                            false
   101	                        } else {
   102	                            viewModel.setEnabled(block, true)
   103	                            goToHomeIfBlockActive(block)
   104	                            true
   105	                        }
   106	                    }
   107	                } else {
   108	                    if (block.toggleLockUntil > System.currentTimeMillis()) {
   109	                        Toast.makeText(
   110	                            requireContext(),
   111	                            getString(R.string.lock_timer_locked, UnlockMethodUtils.formatDateTime(block.toggleLockUntil)),
   112	                            Toast.LENGTH_SHORT
   113	                        ).show()
   114	                        false
   115	                    } else {
   116	                        val folderMethod = getFolderForBlock(block)?.unlockMethod
   117	                            ?.ifBlank { UnlockMethodUtils.METHOD_NONE }
   118	                        val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
   119	                            UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE &&
   120	                            folderMethod == UnlockMethodUtils.METHOD_NONE
   121	                        if (isManualNoMethod) {
   122	                            viewModel.disableAndClearTimers(block)
   123	                            true
   124	                        } else {
   125	                            requestUnlock(block, UnlockChallengeActivity.ACTION_TOGGLE, enabled)
   126	                        }
   127	                    }
   128	                }
   129	            },
   130	            onPause = { block ->
   131	                requestUnlock(block, UnlockChallengeActivity.ACTION_PAUSE)
   132	            },
   133	            onBlockNow = { block -> showBlockNowDurationPicker(block) },
   134	            onEdit = { block ->
   135	                requestUnlock(block, UnlockChallengeActivity.ACTION_EDIT)
   136	            },
   137	            onArchive = { block ->
   138	                requestUnlock(block, UnlockChallengeActivity.ACTION_ARCHIVE)
   139	            },
   140	            onDelete = { block ->
   141	                requestUnlock(block, UnlockChallengeActivity.ACTION_DELETE)
   142	            },
   143	            onMoveToFolder = { block ->
   144	                requestUnlock(block, UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER)
   145	            },
   146	            onRestartFromPause = { block ->
   147	                AlertDialog.Builder(requireContext())
   148	                    .setTitle("Restart Block")
   149	                    .setMessage("Restart '${block.title}' now? This will end the pause and resume blocking.")
   150	                    .setPositiveButton("Restart") { _, _ ->
   151	                        viewModel.unpause(block)
   152	                        goToHomeIfBlockActive(block)
   153	                    }
   154	                    .setNegativeButton("Cancel", null)
   155	                    .show()
   156	            },
   157	            onLockWithTimer = { block -> showLockWithTimerDialog(block) },
   158	            onFolderToggle = { folder, enabled ->
   159	                if (enabled) {
   160	                    viewModel.setFolderEnabled(folder, true)
   161	                    true
   162	                } else {
   163	                    requestFolderUnlock(folder, UnlockChallengeActivity.ACTION_FOLDER_TOGGLE)
   164	                }
   165	            },
   166	            onFolderExpandCollapse = { folder ->
   167	                viewModel.toggleFolderCollapsed(folder)
   168	            },
   169	            onFolderEdit = { folder ->
   170	                requestFolderUnlock(folder, UnlockChallengeActivity.ACTION_FOLDER_EDIT)
   171	            },
   172	            onFolderPause = { folder ->
   173	                requestFolderUnlock(folder, UnlockChallengeActivity.ACTION_FOLDER_PAUSE)
   174	            },
   175	            onFolderDelete = { folder ->
   176	                requestFolderUnlock(folder, UnlockChallengeActivity.ACTION_FOLDER_DELETE)
   177	            }
   178	        )
   179	        binding.rvBlocks.layoutManager = LinearLayoutManager(requireContext())
   180	        binding.rvBlocks.adapter = adapter
   181	
   182	        binding.fabAdd.setOnClickListener {
   183	            val options = arrayOf(
   184	                getString(R.string.block_type_blocklist),
   185	                getString(R.string.block_type_allowlist),
   186	                getString(R.string.home_new_folder)
   187	            )
   188	            AlertDialog.Builder(requireContext())
   189	                .setTitle(getString(R.string.block_type_title))
   190	                .setItems(options) { _, which ->
   191	                    when (which) {
   192	                        0, 1 -> {
   193	                            startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
   194	                                putExtra(EditBlockActivity.EXTRA_IS_ALLOWLIST, which == 1)
   195	                            })
   196	                        }
   197	                        2 -> {
   198	                            startActivity(Intent(requireContext(), EditFolderActivity::class.java))
   199	                        }
   200	                    }
   201	                }
   202	                .show()
   203	        }
   204	
   205	        viewLifecycleOwner.lifecycleScope.launch {
   206	            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
   207	                viewModel.homeItems.collect { items ->
   208	                    adapter.submitList(items)
   209	                    binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
   210	                    binding.rvBlocks.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
   211	                }
   212	            }
   213	        }
   214	
   215	        binding.cardServiceWarning.setOnClickListener {
   216	            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
   217	        }
   218	        updateServiceWarning()
   219	    }
   220	
   221	    override fun onResume() {
   222	        super.onResume()
   223	        updateServiceWarning()
   224	    }
   225	
   226	    private fun requestUnlock(block: AppBlock, action: String, toggleEnabledState: Boolean? = null): Boolean {
   227	        val pending = PendingUnlockAction(block, action, toggleEnabledState)
   228	        val folder = getFolderForBlock(block)
   229	        if (shouldSkipUnlock(block, folder, action, toggleEnabledState)) {
   230	            completePendingUnlockAction(pending)
   231	            return true
   232	        }
   233	
   234	        val folderMethod = folder?.unlockMethod?.ifBlank { UnlockMethodUtils.METHOD_NONE }
   235	        if (folder != null && folderMethod != UnlockMethodUtils.METHOD_NONE) {
   236	            pendingUnlockAction = pending
   237	            unlockChallengeLauncher.launch(
   238	                UnlockChallengeActivity.createFolderIntent(requireContext(), folder.id, action)
   239	            )
   240	            return false
   241	        }
   242	
   243	        val method = UnlockMethodUtils.getNormalizedMethod(block)
   244	        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) {
   245	            viewLifecycleOwner.lifecycleScope.launch {
   246	                val isActive = viewModel.isBlockCurrentlyActive(block)
   247	                if (!isActive) {
   248	                    completePendingUnlockAction(pending)
   249	                } else {
   250	                    pendingUnlockAction = pending
   251	                    unlockChallengeLauncher.launch(
   252	                        UnlockChallengeActivity.createIntent(requireContext(), block.id, action)
   253	                    )
   254	                }
   255	            }
   256	            return false
   257	        }
   258	        pendingUnlockAction = pending
   259	        unlockChallengeLauncher.launch(UnlockChallengeActivity.createIntent(requireContext(), block.id, action))
   260	        return false
   261	    }
   262	
   263	    private fun requestFolderUnlock(folder: BlockFolder, action: String): Boolean {
   264	        val pending = PendingFolderUnlockAction(folder, action)
   265	        if (shouldSkipFolderUnlock(folder)) {
   266	            completePendingFolderUnlockAction(pending)
   267	            return true
   268	        }
   269	        pendingFolderUnlockAction = pending
   270	        folderUnlockChallengeLauncher.launch(
   271	            UnlockChallengeActivity.createFolderIntent(requireContext(), folder.id, action)
   272	        )
   273	        return false
   274	    }
   275	
   276	    private fun shouldSkipUnlock(
   277	        block: AppBlock,
   278	        folder: BlockFolder?,
   279	        action: String,
   280	        toggleEnabledState: Boolean?
   281	    ): Boolean {
   282	        if (action == UnlockChallengeActivity.ACTION_TOGGLE && toggleEnabledState == true) return true
   283	        if (block.toggleLockUntil > System.currentTimeMillis()) return false
   284	
   285	        val folderMethod = folder?.unlockMethod?.ifBlank { UnlockMethodUtils.METHOD_NONE }
   286	        if (folder != null && folderMethod != UnlockMethodUtils.METHOD_NONE) {
   287	            if (folderMethod == UnlockMethodUtils.METHOD_TIMER && System.currentTimeMillis() >= folder.lockUntil) {
   288	                return true
   289	            }
   290	            return false
   291	        }
   292	
   293	        if (!block.isEnabled && action == UnlockChallengeActivity.ACTION_EDIT) return true
   294	        if (!block.isEnabled && action == UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER) return true
   295	        if (!block.isEnabled && (action == UnlockChallengeActivity.ACTION_ARCHIVE || action == UnlockChallengeActivity.ACTION_DELETE)) return true
   296	        val method = UnlockMethodUtils.getNormalizedMethod(block)
   297	        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) return false
   298	        if (method == UnlockMethodUtils.METHOD_TIMER && UnlockMethodUtils.isTimerExpired(block)) return true
   299	        return method == UnlockMethodUtils.METHOD_NONE
   300	    }
   301	
   302	    private fun shouldSkipFolderUnlock(folder: BlockFolder): Boolean {
   303	        return when (folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }) {
   304	            UnlockMethodUtils.METHOD_NONE -> true
   305	            UnlockMethodUtils.METHOD_TIMER -> System.currentTimeMillis() >= folder.lockUntil
   306	            else -> false
   307	        }
   308	    }
   309	
   310	    private fun getFolderForBlock(block: AppBlock): BlockFolder? {
   311	        val folderId = block.folderId ?: return null
   312	        return viewModel.folders.value.firstOrNull { it.id == folderId }
   313	    }
   314	
   315	    private fun getCurrentFolder(folderId: Int): BlockFolder? {
   316	        return viewModel.folders.value.firstOrNull { it.id == folderId }
   317	    }
   318	
   319	    private fun completePendingUnlockAction(pending: PendingUnlockAction) {
   320	        when (pending.action) {
   321	            UnlockChallengeActivity.ACTION_EDIT -> {
   322	                startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
   323	                    putExtra(EditBlockActivity.EXTRA_BLOCK_ID, pending.block.id)
   324	                })
   325	            }
   326	            UnlockChallengeActivity.ACTION_PAUSE -> showPauseDurationPicker(pending.block)
   327	            UnlockChallengeActivity.ACTION_TOGGLE -> {
   328	                pending.toggleEnabledState?.let { enabled ->
   329	                    viewModel.setEnabled(pending.block, enabled)
   330	                }
   331	            }
   332	            UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER -> showMoveToFolderDialog(pending.block)
   333	            UnlockChallengeActivity.ACTION_ARCHIVE -> {
   334	                AlertDialog.Builder(requireContext())
   335	                    .setTitle("Archive Block")
   336	                    .setMessage("Archive '${pending.block.title}'? It will be hidden but can be restored later.")
   337	                    .setPositiveButton("Archive") { _, _ -> viewModel.archive(pending.block) }
   338	                    .setNegativeButton("Cancel", null)
   339	                    .show()
   340	            }
   341	            UnlockChallengeActivity.ACTION_DELETE -> {
   342	                AlertDialog.Builder(requireContext())
   343	                    .setTitle("Delete Block")
   344	                    .setMessage("Delete '${pending.block.title}'? This cannot be undone.")
   345	                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(pending.block) }
   346	                    .setNegativeButton("Cancel", null)
   347	                    .show()
   348	            }
   349	        }
   350	    }
   351	
   352	    private fun completePendingFolderUnlockAction(pending: PendingFolderUnlockAction) {
   353	        val folder = getCurrentFolder(pending.folder.id) ?: pending.folder
   354	        when (pending.action) {
   355	            UnlockChallengeActivity.ACTION_FOLDER_PAUSE -> {
   356	                if (folder.pausedUntil > System.currentTimeMillis() || folder.pausedUntil == Long.MAX_VALUE) {
   357	                    viewModel.unpauseFolder(folder)
   358	                } else {
   359	                    showFolderPauseDurationPicker(folder)
   360	                }
   361	            }
   362	            UnlockChallengeActivity.ACTION_FOLDER_EDIT -> {
   363	                startActivity(Intent(requireContext(), EditFolderActivity::class.java).apply {
   364	                    putExtra(EditFolderActivity.EXTRA_FOLDER_ID, folder.id)
   365	                })
   366	            }
   367	            UnlockChallengeActivity.ACTION_FOLDER_TOGGLE -> viewModel.setFolderEnabled(folder, false)
   368	            UnlockChallengeActivity.ACTION_FOLDER_DELETE -> showDeleteFolderDialog(folder)
   369	        }
   370	    }
   371	
   372	    private fun showPauseDurationPicker(block: AppBlock) {
   373	        val isPomodoroActive = block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
   374	            block.pomodoroRoundsTotal > 0
   375	        val pomodoroState = if (isPomodoroActive) UnlockMethodUtils.computePomodoroState(block) else null
   376	        val sessionRemainingMs = pomodoroState?.sessionRemainingMs ?: Long.MAX_VALUE
   377	
   378	        val durations = mutableListOf<Pair<String, Long>>()
   379	        val candidates = listOf(
   380	            "15 minutes" to 15 * 60_000L,
   381	            "30 minutes" to 30 * 60_000L,
   382	            "1 hour" to 60 * 60_000L,
   383	            "2 hours" to 2 * 60 * 60_000L
   384	        )
   385	        for ((label, ms) in candidates) {
   386	            if (ms <= sessionRemainingMs) durations.add(label to ms)
   387	        }
   388	        if (!isPomodoroActive) {
   389	            durations.add("Rest of day" to millisUntilMidnight())
   390	            durations.add("Indefinitely" to -1L)
   391	        } else {
   392	            durations.add(getString(R.string.pomodoro_end_early) to -2L)
   393	        }
   394	
   395	        val options = durations.map { it.first }.toTypedArray()
   396	        AlertDialog.Builder(requireContext())
   397	            .setTitle("Pause '${block.title}'")
   398	            .setItems(options) { _, which ->
   399	                val (_, durationMs) = durations[which]
   400	                when (durationMs) {
   401	                    -1L -> viewModel.setEnabled(block, false)
   402	                    -2L -> viewModel.disableAndClearTimers(block)
   403	                    else -> if (durationMs > 0L) viewModel.pause(block, durationMs)
   404	                }
   405	            }
   406	            .setNegativeButton("Cancel", null)
   407	            .show()
   408	    }
   409	
   410	    private fun showFolderPauseDurationPicker(folder: BlockFolder) {
   411	        val durations = listOf(
   412	            "15 minutes" to 15 * 60_000L,
   413	            "30 minutes" to 30 * 60_000L,
   414	            "1 hour" to 60 * 60_000L,
   415	            "2 hours" to 2 * 60 * 60_000L,
   416	            "Rest of day" to millisUntilMidnight(),
   417	            "Indefinitely" to Long.MAX_VALUE
   418	        )
   419	        AlertDialog.Builder(requireContext())
   420	            .setTitle("Pause '${folder.title}'")
   421	            .setItems(durations.map { it.first }.toTypedArray()) { _, which ->
   422	                viewModel.pauseFolder(folder, durations[which].second)
   423	            }
   424	            .setNegativeButton("Cancel", null)
   425	            .show()
   426	    }
   427	
   428	    private fun showPomodoroActivationDialog(block: AppBlock) {
   429	        val dialogView = LinearLayout(requireContext()).apply {
   430	            orientation = LinearLayout.VERTICAL
   431	            setPadding(48, 24, 48, 0)
   432	        }
   433	
   434	        val roundsLabel = android.widget.TextView(requireContext()).apply {
   435	            text = getString(R.string.pomodoro_rounds_label)
   436	            textSize = 14f
   437	        }
   438	        dialogView.addView(roundsLabel)
   439	
   440	        val roundsPicker = NumberPicker(requireContext()).apply {
   441	            minValue = 1
   442	            maxValue = 20
   443	            value = 4
   444	        }
   445	        dialogView.addView(roundsPicker)
   446	
   447	        val lockCheckBox = CheckBox(requireContext()).apply {
   448	            text = getString(R.string.pomodoro_lock_editing_label)
   449	            isChecked = block.pomodoroLockEditing
   450	        }
   451	        dialogView.addView(lockCheckBox)
   452	
   453	        AlertDialog.Builder(requireContext())
   454	            .setTitle(getString(R.string.pomodoro_activation_title, block.title))
   455	            .setView(dialogView)
   456	            .setPositiveButton(android.R.string.ok) { _, _ ->
   457	                val rounds = roundsPicker.value
   458	                val lockEditing = lockCheckBox.isChecked
   459	                val focusMs = block.pomodoroDurationMin * 60_000L
   460	                val breakMs = block.pomodoroBreakMin * 60_000L
   461	                val totalMs = focusMs * rounds + breakMs * (rounds - 1)
   462	                val endTime = UnlockMethodUtils.formatDateTime(System.currentTimeMillis() + totalMs)
   463	
   464	                AlertDialog.Builder(requireContext())
   465	                    .setTitle(getString(R.string.pomodoro_confirm_title))
   466	                    .setMessage(
   467	                        getString(
   468	                            R.string.pomodoro_confirm_message,
   469	                            rounds,
   470	                            block.pomodoroDurationMin,
   471	                            block.pomodoroBreakMin,
   472	                            endTime
   473	                        )
   474	                    )
   475	                    .setPositiveButton(getString(R.string.pomodoro_start)) { _, _ ->
   476	                        viewModel.startPomodoroSession(block, rounds, lockEditing)
   477	                        startActivity(Intent(Intent.ACTION_MAIN).apply {
   478	                            addCategory(Intent.CATEGORY_HOME)
   479	                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
   480	                        })
   481	                    }
   482	                    .setNegativeButton(android.R.string.cancel, null)
   483	                    .show()
   484	            }
   485	            .setNegativeButton(android.R.string.cancel, null)
   486	            .show()
   487	    }
   488	
   489	    private fun showMoveToFolderDialog(block: AppBlock) {
   490	        val folders = viewModel.folders.value
   491	        val options = listOf(getString(R.string.folder_none_option)) + folders.map { it.title }
   492	        var selectedIndex = folders.indexOfFirst { it.id == block.folderId }
   493	            .takeIf { it >= 0 }
   494	            ?.plus(1)
   495	            ?: 0
   496	
   497	        AlertDialog.Builder(requireContext())
   498	            .setTitle(getString(R.string.move_to_folder_title, block.title))
   499	            .setSingleChoiceItems(options.toTypedArray(), selectedIndex) { _, which ->
   500	                selectedIndex = which
   501	            }
   502	            .setPositiveButton(R.string.move_to_folder_confirm) { _, _ ->
   503	                val folderId = if (selectedIndex == 0) null else folders.getOrNull(selectedIndex - 1)?.id
   504	                viewModel.moveBlockToFolder(block, folderId)
   505	            }
   506	            .setNegativeButton(android.R.string.cancel, null)
   507	            .show()
   508	    }
   509	
   510	    private fun showBlockNowDurationPicker(block: AppBlock) {
   511	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
   512	        AlertDialog.Builder(requireContext())
   513	            .setTitle("Block '${block.title}' now for...")
   514	            .setItems(options) { _, which ->
   515	                val durationMs = when (which) {
   516	                    0 -> 15 * 60_000L
   517	                    1 -> 30 * 60_000L
   518	                    2 -> 60 * 60_000L
   519	                    3 -> 2 * 60 * 60_000L
   520	                    4 -> 4 * 60 * 60_000L
   521	                    5 -> millisUntilMidnight()
   522	                    else -> 0L
   523	                }
   524	                if (durationMs > 0L) {
   525	                    viewModel.blockNow(block, durationMs)
   526	                    startActivity(Intent(Intent.ACTION_MAIN).apply {
   527	                        addCategory(Intent.CATEGORY_HOME)
   528	                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
   529	                    })
   530	                }
   531	            }
   532	            .setNegativeButton("Cancel", null)
   533	            .show()
   534	    }
   535	
   536	    private fun showAllowlistDurationPicker(block: AppBlock) {
   537	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
   538	        AlertDialog.Builder(requireContext())
   539	            .setTitle(getString(R.string.allowlist_duration_title, block.title))
   540	            .setItems(options) { _, which ->
   541	                val durationMs = when (which) {
   542	                    0 -> 15 * 60_000L
   543	                    1 -> 30 * 60_000L
   544	                    2 -> 60 * 60_000L
   545	                    3 -> 2 * 60 * 60_000L
   546	                    4 -> 4 * 60 * 60_000L
   547	                    5 -> millisUntilMidnight()
   548	                    else -> 0L
   549	                }
   550	                if (durationMs > 0L) {
   551	                    viewModel.enableWithActiveUntil(block, durationMs)
   552	                    startActivity(Intent(Intent.ACTION_MAIN).apply {
   553	                        addCategory(Intent.CATEGORY_HOME)
   554	                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
   555	                    })
   556	                }
   557	            }
   558	            .setNegativeButton("Cancel", null)
   559	            .show()
   560	    }
   561	
   562	    private fun showLockWithTimerDialog(block: AppBlock) {
   563	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "8 hours", "Rest of day")
   564	        var selectedIndex = 0
   565	
   566	        val dialogView = LinearLayout(requireContext()).apply {
   567	            orientation = LinearLayout.VERTICAL
   568	            setPadding(48, 24, 48, 0)
   569	        }
   570	
   571	        val checkBox = if (!block.isAllowlistMode) {
   572	            CheckBox(requireContext()).apply {
   573	                text = getString(R.string.lock_with_timer_auto_off)
   574	                isChecked = false
   575	            }.also { dialogView.addView(it) }
   576	        } else {
   577	            null
   578	        }
   579	
   580	        AlertDialog.Builder(requireContext())
   581	            .setTitle(getString(R.string.lock_with_timer_title, block.title))
   582	            .setSingleChoiceItems(options, selectedIndex) { _, which ->
   583	                selectedIndex = which
   584	            }
   585	            .setView(dialogView)
   586	            .setPositiveButton(android.R.string.ok) { _, _ ->
   587	                val durationMs = when (selectedIndex) {
   588	                    0 -> 15 * 60_000L
   589	                    1 -> 30 * 60_000L
   590	                    2 -> 60 * 60_000L
   591	                    3 -> 2 * 60 * 60_000L
   592	                    4 -> 4 * 60 * 60_000L
   593	                    5 -> 8 * 60 * 60_000L
   594	                    6 -> millisUntilMidnight()
   595	                    else -> 0L
   596	                }
   597	                if (durationMs > 0L) {
   598	                    val autoDisable = block.isAllowlistMode || (checkBox?.isChecked == true)
   599	                    viewModel.lockWithTimer(block, durationMs, autoDisable)
   600	                }
   601	            }
   602	            .setNegativeButton(android.R.string.cancel, null)
   603	            .show()
   604	    }
   605	
   606	    private fun millisUntilMidnight(): Long {
   607	        val cal = Calendar.getInstance().apply {
   608	            set(Calendar.HOUR_OF_DAY, 23)
   609	            set(Calendar.MINUTE, 59)
   610	            set(Calendar.SECOND, 59)
   611	            set(Calendar.MILLISECOND, 999)
   612	        }
   613	        return cal.timeInMillis - System.currentTimeMillis()
   614	    }
   615	
   616	    private fun goToHomeIfBlockActive(block: AppBlock) {
   617	        viewLifecycleOwner.lifecycleScope.launch {
   618	            if (viewModel.isBlockCurrentlyActive(block)) {
   619	                startActivity(Intent(Intent.ACTION_MAIN).apply {
   620	                    addCategory(Intent.CATEGORY_HOME)
   621	                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
   622	                })
   623	            }
   624	        }
   625	    }
   626	
   627	    private fun isAccessibilityServiceEnabled(): Boolean {
   628	        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
   629	        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
   630	        return enabledServices.any {
   631	            it.resolveInfo.serviceInfo.packageName == requireContext().packageName
   632	        }
   633	    }
   634	
   635	    private fun updateServiceWarning() {
   636	        val enabled = isAccessibilityServiceEnabled()
   637	        binding.cardServiceWarning.visibility = if (enabled) View.GONE else View.VISIBLE
   638	    }
   639	
   640	    private fun showDeleteFolderDialog(folder: BlockFolder) {
   641	        AlertDialog.Builder(requireContext())
   642	            .setTitle(getString(R.string.folder_delete_title))
   643	            .setMessage(getString(R.string.folder_delete_message, folder.title))
   644	            .setPositiveButton(R.string.folder_delete_confirm) { _, _ ->
   645	                viewModel.deleteFolder(folder)
   646	            }
   647	            .setNegativeButton(android.R.string.cancel, null)
   648	            .show()
   649	    }
   650	
   651	    override fun onDestroyView() {
   652	        super.onDestroyView()
   653	        _binding = null
   654	    }
   655	}
   656	