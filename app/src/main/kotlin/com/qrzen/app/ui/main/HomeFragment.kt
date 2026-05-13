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
    48	    private var _binding: FragmentHomeBinding? = null
    49	    private val binding get() = _binding!!
    50	    private val viewModel: HomeViewModel by viewModels()
    51	    @Inject lateinit var timeBlockDao: TimeBlockDao
    52	    private lateinit var adapter: HomeListAdapter
    53	    private var pendingUnlockAction: PendingUnlockAction? = null
    54	
    55	    private val unlockChallengeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    56	        val pending = pendingUnlockAction ?: return@registerForActivityResult
    57	        pendingUnlockAction = null
    58	        if (result.resultCode == Activity.RESULT_OK) {
    59	            completePendingUnlockAction(pending)
    60	        }
    61	    }
    62	
    63	    override fun onCreateView(
    64	        inflater: LayoutInflater, container: ViewGroup?,
    65	        savedInstanceState: Bundle?
    66	    ): View {
    67	        _binding = FragmentHomeBinding.inflate(inflater, container, false)
    68	        return binding.root
    69	    }
    70	
    71	    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    72	        super.onViewCreated(view, savedInstanceState)
    73	
    74	        adapter = HomeListAdapter(
    75	            timeBlockDao = timeBlockDao,
    76	            onToggle = { block, enabled ->
    77	                if (enabled) {
    78	                    if (block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO) {
    79	                        showPomodoroActivationDialog(block)
    80	                        false
    81	                    } else {
    82	                        val isManualAllowlist = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
    83	                            block.isAllowlistMode
    84	                        if (isManualAllowlist) {
    85	                            showAllowlistDurationPicker(block)
    86	                            false
    87	                        } else {
    88	                            viewModel.setEnabled(block, true)
    89	                            goToHomeIfBlockActive(block)
    90	                            true
    91	                        }
    92	                    }
    93	                } else {
    94	                    if (block.toggleLockUntil > System.currentTimeMillis()) {
    95	                        Toast.makeText(
    96	                            requireContext(),
    97	                            getString(R.string.lock_timer_locked, UnlockMethodUtils.formatDateTime(block.toggleLockUntil)),
    98	                            Toast.LENGTH_SHORT
    99	                        ).show()
   100	                        false
   101	                    } else {
   102	                        val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
   103	                            UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE
   104	                        if (isManualNoMethod) {
   105	                            viewModel.disableAndClearTimers(block)
   106	                            true
   107	                        } else {
   108	                            requestUnlock(block, UnlockChallengeActivity.ACTION_TOGGLE, enabled)
   109	                        }
   110	                    }
   111	                }
   112	            },
   113	            onPause = { block ->
   114	                requestUnlock(block, UnlockChallengeActivity.ACTION_PAUSE)
   115	            },
   116	            onBlockNow = { block -> showBlockNowDurationPicker(block) },
   117	            onEdit = { block ->
   118	                requestUnlock(block, UnlockChallengeActivity.ACTION_EDIT)
   119	            },
   120	            onArchive = { block ->
   121	                requestUnlock(block, UnlockChallengeActivity.ACTION_ARCHIVE)
   122	            },
   123	            onDelete = { block ->
   124	                requestUnlock(block, UnlockChallengeActivity.ACTION_DELETE)
   125	            },
   126	            onMoveToFolder = { block ->
   127	                requestUnlock(block, UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER)
   128	            },
   129	            onRestartFromPause = { block ->
   130	                AlertDialog.Builder(requireContext())
   131	                    .setTitle("Restart Block")
   132	                    .setMessage("Restart '${block.title}' now? This will end the pause and resume blocking.")
   133	                    .setPositiveButton("Restart") { _, _ ->
   134	                        viewModel.unpause(block)
   135	                        goToHomeIfBlockActive(block)
   136	                    }
   137	                    .setNegativeButton("Cancel", null)
   138	                    .show()
   139	            },
   140	            onLockWithTimer = { block -> showLockWithTimerDialog(block) },
   141	            onFolderToggle = { folder, enabled ->
   142	                viewModel.setFolderEnabled(folder, enabled)
   143	            },
   144	            onFolderExpandCollapse = { folder ->
   145	                viewModel.toggleFolderCollapsed(folder)
   146	            },
   147	            onFolderEdit = { folder ->
   148	                startActivity(Intent(requireContext(), EditFolderActivity::class.java).apply {
   149	                    putExtra(EditFolderActivity.EXTRA_FOLDER_ID, folder.id)
   150	                })
   151	            },
   152	            onFolderPause = {
   153	                Toast.makeText(requireContext(), R.string.folder_pause_coming_soon, Toast.LENGTH_SHORT).show()
   154	            },
   155	            onFolderDelete = { folder ->
   156	                showDeleteFolderDialog(folder)
   157	            }
   158	        )
   159	        binding.rvBlocks.layoutManager = LinearLayoutManager(requireContext())
   160	        binding.rvBlocks.adapter = adapter
   161	
   162	        binding.fabAdd.setOnClickListener {
   163	            val options = arrayOf(
   164	                getString(R.string.home_new_blocklist_block),
   165	                getString(R.string.home_new_allowlist_block),
   166	                getString(R.string.home_new_folder)
   167	            )
   168	            AlertDialog.Builder(requireContext())
   169	                .setTitle(getString(R.string.block_type_title))
   170	                .setItems(options) { _, which ->
   171	                    when (which) {
   172	                        0, 1 -> {
   173	                            startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
   174	                                putExtra(EditBlockActivity.EXTRA_IS_ALLOWLIST, which == 1)
   175	                            })
   176	                        }
   177	                        2 -> {
   178	                            startActivity(Intent(requireContext(), EditFolderActivity::class.java))
   179	                        }
   180	                    }
   181	                }
   182	                .show()
   183	        }
   184	
   185	        viewLifecycleOwner.lifecycleScope.launch {
   186	            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
   187	                viewModel.homeItems.collect { items ->
   188	                    adapter.submitList(items)
   189	                    binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
   190	                    binding.rvBlocks.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
   191	                }
   192	            }
   193	        }
   194	
   195	        binding.cardServiceWarning.setOnClickListener {
   196	            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
   197	        }
   198	        updateServiceWarning()
   199	    }
   200	
   201	    override fun onResume() {
   202	        super.onResume()
   203	        updateServiceWarning()
   204	    }
   205	
   206	    private fun requestUnlock(block: AppBlock, action: String, toggleEnabledState: Boolean? = null): Boolean {
   207	        val pending = PendingUnlockAction(block, action, toggleEnabledState)
   208	        if (shouldSkipUnlock(block, action, toggleEnabledState)) {
   209	            completePendingUnlockAction(pending)
   210	            return true
   211	        }
   212	        val method = UnlockMethodUtils.getNormalizedMethod(block)
   213	        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) {
   214	            viewLifecycleOwner.lifecycleScope.launch {
   215	                val isActive = viewModel.isBlockCurrentlyActive(block)
   216	                if (!isActive) {
   217	                    completePendingUnlockAction(pending)
   218	                } else {
   219	                    pendingUnlockAction = pending
   220	                    unlockChallengeLauncher.launch(
   221	                        UnlockChallengeActivity.createIntent(requireContext(), block.id, action)
   222	                    )
   223	                }
   224	            }
   225	            return false
   226	        }
   227	        pendingUnlockAction = pending
   228	        unlockChallengeLauncher.launch(UnlockChallengeActivity.createIntent(requireContext(), block.id, action))
   229	        return false
   230	    }
   231	
   232	    private fun shouldSkipUnlock(block: AppBlock, action: String, toggleEnabledState: Boolean?): Boolean {
   233	        if (action == UnlockChallengeActivity.ACTION_TOGGLE && toggleEnabledState == true) return true
   234	        if (!block.isEnabled && action == UnlockChallengeActivity.ACTION_EDIT) return true
   235	        if (!block.isEnabled && action == UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER) return true
   236	        if (!block.isEnabled && (action == UnlockChallengeActivity.ACTION_ARCHIVE || action == UnlockChallengeActivity.ACTION_DELETE)) return true
   237	        if (block.toggleLockUntil > System.currentTimeMillis()) return false
   238	        val method = UnlockMethodUtils.getNormalizedMethod(block)
   239	        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) return false
   240	        if (method == UnlockMethodUtils.METHOD_TIMER && UnlockMethodUtils.isTimerExpired(block)) return true
   241	        return method == UnlockMethodUtils.METHOD_NONE
   242	    }
   243	
   244	    private fun completePendingUnlockAction(pending: PendingUnlockAction) {
   245	        when (pending.action) {
   246	            UnlockChallengeActivity.ACTION_EDIT -> {
   247	                startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
   248	                    putExtra(EditBlockActivity.EXTRA_BLOCK_ID, pending.block.id)
   249	                })
   250	            }
   251	            UnlockChallengeActivity.ACTION_PAUSE -> showPauseDurationPicker(pending.block)
   252	            UnlockChallengeActivity.ACTION_TOGGLE -> {
   253	                pending.toggleEnabledState?.let { enabled ->
   254	                    viewModel.setEnabled(pending.block, enabled)
   255	                }
   256	            }
   257	            UnlockChallengeActivity.ACTION_MOVE_TO_FOLDER -> showMoveToFolderDialog(pending.block)
   258	            UnlockChallengeActivity.ACTION_ARCHIVE -> {
   259	                AlertDialog.Builder(requireContext())
   260	                    .setTitle("Archive Block")
   261	                    .setMessage("Archive '${pending.block.title}'? It will be hidden but can be restored later.")
   262	                    .setPositiveButton("Archive") { _, _ -> viewModel.archive(pending.block) }
   263	                    .setNegativeButton("Cancel", null)
   264	                    .show()
   265	            }
   266	            UnlockChallengeActivity.ACTION_DELETE -> {
   267	                AlertDialog.Builder(requireContext())
   268	                    .setTitle("Delete Block")
   269	                    .setMessage("Delete '${pending.block.title}'? This cannot be undone.")
   270	                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(pending.block) }
   271	                    .setNegativeButton("Cancel", null)
   272	                    .show()
   273	            }
   274	        }
   275	    }
   276	
   277	    private fun showPauseDurationPicker(block: AppBlock) {
   278	        val isPomodoroActive = block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
   279	            block.pomodoroRoundsTotal > 0
   280	        val pomodoroState = if (isPomodoroActive) UnlockMethodUtils.computePomodoroState(block) else null
   281	        val sessionRemainingMs = pomodoroState?.sessionRemainingMs ?: Long.MAX_VALUE
   282	
   283	        val durations = mutableListOf<Pair<String, Long>>()
   284	        val candidates = listOf(
   285	            "15 minutes" to 15 * 60_000L,
   286	            "30 minutes" to 30 * 60_000L,
   287	            "1 hour" to 60 * 60_000L,
   288	            "2 hours" to 2 * 60 * 60_000L
   289	        )
   290	        for ((label, ms) in candidates) {
   291	            if (ms <= sessionRemainingMs) durations.add(label to ms)
   292	        }
   293	        if (!isPomodoroActive) {
   294	            durations.add("Rest of day" to millisUntilMidnight())
   295	            durations.add("Indefinitely" to -1L)
   296	        } else {
   297	            durations.add(getString(R.string.pomodoro_end_early) to -2L)
   298	        }
   299	
   300	        val options = durations.map { it.first }.toTypedArray()
   301	        AlertDialog.Builder(requireContext())
   302	            .setTitle("Pause '${block.title}'")
   303	            .setItems(options) { _, which ->
   304	                val (_, durationMs) = durations[which]
   305	                when (durationMs) {
   306	                    -1L -> viewModel.setEnabled(block, false)
   307	                    -2L -> viewModel.disableAndClearTimers(block)
   308	                    else -> if (durationMs > 0L) viewModel.pause(block, durationMs)
   309	                }
   310	            }
   311	            .setNegativeButton("Cancel", null)
   312	            .show()
   313	    }
   314	
   315	    private fun showPomodoroActivationDialog(block: AppBlock) {
   316	        val dialogView = LinearLayout(requireContext()).apply {
   317	            orientation = LinearLayout.VERTICAL
   318	            setPadding(48, 24, 48, 0)
   319	        }
   320	
   321	        val roundsLabel = android.widget.TextView(requireContext()).apply {
   322	            text = getString(R.string.pomodoro_rounds_label)
   323	            textSize = 14f
   324	        }
   325	        dialogView.addView(roundsLabel)
   326	
   327	        val roundsPicker = NumberPicker(requireContext()).apply {
   328	            minValue = 1
   329	            maxValue = 20
   330	            value = 4
   331	        }
   332	        dialogView.addView(roundsPicker)
   333	
   334	        val lockCheckBox = CheckBox(requireContext()).apply {
   335	            text = getString(R.string.pomodoro_lock_editing_label)
   336	            isChecked = block.pomodoroLockEditing
   337	        }
   338	        dialogView.addView(lockCheckBox)
   339	
   340	        AlertDialog.Builder(requireContext())
   341	            .setTitle(getString(R.string.pomodoro_activation_title, block.title))
   342	            .setView(dialogView)
   343	            .setPositiveButton(android.R.string.ok) { _, _ ->
   344	                val rounds = roundsPicker.value
   345	                val lockEditing = lockCheckBox.isChecked
   346	                val focusMs = block.pomodoroDurationMin * 60_000L
   347	                val breakMs = block.pomodoroBreakMin * 60_000L
   348	                val totalMs = focusMs * rounds + breakMs * (rounds - 1)
   349	                val endTime = UnlockMethodUtils.formatDateTime(System.currentTimeMillis() + totalMs)
   350	
   351	                AlertDialog.Builder(requireContext())
   352	                    .setTitle(getString(R.string.pomodoro_confirm_title))
   353	                    .setMessage(
   354	                        getString(
   355	                            R.string.pomodoro_confirm_message,
   356	                            rounds,
   357	                            block.pomodoroDurationMin,
   358	                            block.pomodoroBreakMin,
   359	                            endTime
   360	                        )
   361	                    )
   362	                    .setPositiveButton(getString(R.string.pomodoro_start)) { _, _ ->
   363	                        viewModel.startPomodoroSession(block, rounds, lockEditing)
   364	                        startActivity(Intent(Intent.ACTION_MAIN).apply {
   365	                            addCategory(Intent.CATEGORY_HOME)
   366	                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
   367	                        })
   368	                    }
   369	                    .setNegativeButton(android.R.string.cancel, null)
   370	                    .show()
   371	            }
   372	            .setNegativeButton(android.R.string.cancel, null)
   373	            .show()
   374	    }
   375	
   376	    private fun showMoveToFolderDialog(block: AppBlock) {
   377	        val folders = viewModel.folders.value
   378	        val options = listOf(getString(R.string.folder_none_option)) + folders.map { it.title }
   379	        var selectedIndex = folders.indexOfFirst { it.id == block.folderId }
   380	            .takeIf { it >= 0 }
   381	            ?.plus(1)
   382	            ?: 0
   383	
   384	        AlertDialog.Builder(requireContext())
   385	            .setTitle(getString(R.string.move_to_folder_title, block.title))
   386	            .setSingleChoiceItems(options.toTypedArray(), selectedIndex) { _, which ->
   387	                selectedIndex = which
   388	            }
   389	            .setPositiveButton(R.string.move_to_folder_confirm) { _, _ ->
   390	                val folderId = if (selectedIndex == 0) null else folders.getOrNull(selectedIndex - 1)?.id
   391	                viewModel.moveBlockToFolder(block, folderId)
   392	            }
   393	            .setNegativeButton(android.R.string.cancel, null)
   394	            .show()
   395	    }
   396	
   397	    private fun showBlockNowDurationPicker(block: AppBlock) {
   398	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
   399	        AlertDialog.Builder(requireContext())
   400	            .setTitle("Block '${block.title}' now for...")
   401	            .setItems(options) { _, which ->
   402	                val durationMs = when (which) {
   403	                    0 -> 15 * 60_000L
   404	                    1 -> 30 * 60_000L
   405	                    2 -> 60 * 60_000L
   406	                    3 -> 2 * 60 * 60_000L
   407	                    4 -> 4 * 60 * 60_000L
   408	                    5 -> millisUntilMidnight()
   409	                    else -> 0L
   410	                }
   411	                if (durationMs > 0L) {
   412	                    viewModel.blockNow(block, durationMs)
   413	                    startActivity(Intent(Intent.ACTION_MAIN).apply {
   414	                        addCategory(Intent.CATEGORY_HOME)
   415	                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
   416	                    })
   417	                }
   418	            }
   419	            .setNegativeButton("Cancel", null)
   420	            .show()
   421	    }
   422	
   423	    private fun showAllowlistDurationPicker(block: AppBlock) {
   424	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
   425	        AlertDialog.Builder(requireContext())
   426	            .setTitle(getString(R.string.allowlist_duration_title, block.title))
   427	            .setItems(options) { _, which ->
   428	                val durationMs = when (which) {
   429	                    0 -> 15 * 60_000L
   430	                    1 -> 30 * 60_000L
   431	                    2 -> 60 * 60_000L
   432	                    3 -> 2 * 60 * 60_000L
   433	                    4 -> 4 * 60 * 60_000L
   434	                    5 -> millisUntilMidnight()
   435	                    else -> 0L
   436	                }
   437	                if (durationMs > 0L) {
   438	                    viewModel.enableWithActiveUntil(block, durationMs)
   439	                    startActivity(Intent(Intent.ACTION_MAIN).apply {
   440	                        addCategory(Intent.CATEGORY_HOME)
   441	                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
   442	                    })
   443	                }
   444	            }
   445	            .setNegativeButton("Cancel", null)
   446	            .show()
   447	    }
   448	
   449	    private fun showLockWithTimerDialog(block: AppBlock) {
   450	        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "8 hours", "Rest of day")
   451	        var selectedIndex = 0
   452	
   453	        val dialogView = LinearLayout(requireContext()).apply {
   454	            orientation = LinearLayout.VERTICAL
   455	            setPadding(48, 24, 48, 0)
   456	        }
   457	
   458	        val checkBox = if (!block.isAllowlistMode) {
   459	            CheckBox(requireContext()).apply {
   460	                text = getString(R.string.lock_with_timer_auto_off)
   461	                isChecked = false
   462	            }.also { dialogView.addView(it) }
   463	        } else {
   464	            null
   465	        }
   466	
   467	        AlertDialog.Builder(requireContext())
   468	            .setTitle(getString(R.string.lock_with_timer_title, block.title))
   469	            .setSingleChoiceItems(options, selectedIndex) { _, which ->
   470	                selectedIndex = which
   471	            }
   472	            .setView(dialogView)
   473	            .setPositiveButton(android.R.string.ok) { _, _ ->
   474	                val durationMs = when (selectedIndex) {
   475	                    0 -> 15 * 60_000L
   476	                    1 -> 30 * 60_000L
   477	                    2 -> 60 * 60_000L
   478	                    3 -> 2 * 60 * 60_000L
   479	                    4 -> 4 * 60 * 60_000L
   480	                    5 -> 8 * 60 * 60_000L
   481	                    6 -> millisUntilMidnight()
   482	                    else -> 0L
   483	                }
   484	                if (durationMs > 0L) {
   485	                    val autoDisable = block.isAllowlistMode || (checkBox?.isChecked == true)
   486	                    viewModel.lockWithTimer(block, durationMs, autoDisable)
   487	                }
   488	            }
   489	            .setNegativeButton(android.R.string.cancel, null)
   490	            .show()
   491	    }
   492	
   493	    private fun millisUntilMidnight(): Long {
   494	        val cal = Calendar.getInstance().apply {
   495	            set(Calendar.HOUR_OF_DAY, 23)
   496	            set(Calendar.MINUTE, 59)
   497	            set(Calendar.SECOND, 59)
   498	            set(Calendar.MILLISECOND, 999)
   499	        }
   500	        return cal.timeInMillis - System.currentTimeMillis()
   501	    }
   502	
   503	    private fun goToHomeIfBlockActive(block: AppBlock) {
   504	        viewLifecycleOwner.lifecycleScope.launch {
   505	            if (viewModel.isBlockCurrentlyActive(block)) {
   506	                startActivity(Intent(Intent.ACTION_MAIN).apply {
   507	                    addCategory(Intent.CATEGORY_HOME)
   508	                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
   509	                })
   510	            }
   511	        }
   512	    }
   513	
   514	    private fun isAccessibilityServiceEnabled(): Boolean {
   515	        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
   516	        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
   517	        return enabledServices.any {
   518	            it.resolveInfo.serviceInfo.packageName == requireContext().packageName
   519	        }
   520	    }
   521	
   522	    private fun updateServiceWarning() {
   523	        val enabled = isAccessibilityServiceEnabled()
   524	        binding.cardServiceWarning.visibility = if (enabled) View.GONE else View.VISIBLE
   525	    }
   526	
   527	    private fun showDeleteFolderDialog(folder: BlockFolder) {
   528	        AlertDialog.Builder(requireContext())
   529	            .setTitle(getString(R.string.folder_delete_title))
   530	            .setMessage(getString(R.string.folder_delete_message, folder.title))
   531	            .setPositiveButton(R.string.folder_delete_confirm) { _, _ ->
   532	                viewModel.deleteFolder(folder)
   533	            }
   534	            .setNegativeButton(android.R.string.cancel, null)
   535	            .show()
   536	    }
   537	
   538	    override fun onDestroyView() {
   539	        super.onDestroyView()
   540	        _binding = null
   541	    }
   542	}
   543	