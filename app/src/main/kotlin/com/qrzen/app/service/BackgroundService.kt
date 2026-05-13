     1	package com.qrzen.app.service
     2	
     3	import android.app.Notification
     4	import android.app.NotificationChannel
     5	import android.app.KeyguardManager
     6	import android.app.NotificationManager
     7	import android.app.Service
     8	import android.app.usage.UsageEvents
     9	import android.app.usage.UsageStatsManager
    10	import android.content.Context
    11	import android.content.Intent
    12	import android.content.IntentFilter
    13	import android.os.Build
    14	import android.os.Handler
    15	import android.os.IBinder
    16	import android.os.Looper
    17	import android.os.PowerManager
    18	import android.net.Uri
    19	import android.content.pm.PackageManager
    20	import android.view.inputmethod.InputMethodManager
    21	import androidx.core.app.NotificationCompat
    22	import com.qrzen.app.R
    23	import com.qrzen.app.data.db.AppBlockDao
    24	import com.qrzen.app.data.db.BlockFolderDao
    25	import com.qrzen.app.data.db.TimeBlockDao
    26	import com.qrzen.app.data.model.AppBlock
    27	import com.qrzen.app.data.model.TimeBlock
    28	import com.qrzen.app.data.prefs.Prefs
    29	import com.qrzen.app.receiver.AlarmKeepaliveReceiver
    30	import com.qrzen.app.receiver.PackageInstallReceiver
    31	import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity
    32	import com.qrzen.app.ui.lock.LockScreenActivity
    33	import com.qrzen.app.ui.unlock.UnlockMethodUtils
    34	import com.qrzen.app.widget.WidgetRefresh
    35	import com.tencent.mmkv.MMKV
    36	import dagger.hilt.android.AndroidEntryPoint
    37	import kotlinx.coroutines.CoroutineScope
    38	import kotlinx.coroutines.Dispatchers
    39	import kotlinx.coroutines.SupervisorJob
    40	import kotlinx.coroutines.cancel
    41	import kotlinx.coroutines.launch
    42	import java.util.Calendar
    43	import javax.inject.Inject
    44	
    45	@AndroidEntryPoint
    46	class BackgroundService : Service() {
    47	
    48	    @Inject lateinit var dao: AppBlockDao
    49	    @Inject lateinit var blockFolderDao: BlockFolderDao
    50	    @Inject lateinit var timeBlockDao: TimeBlockDao
    51	
    52	    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    53	    private val handler = Handler(Looper.getMainLooper())
    54	    private val usageHandler = Handler(Looper.getMainLooper())
    55	    private val previouslyActiveBlockIds = mutableSetOf<Int>()
    56	    private var usagePollingActive = false
    57	    private var lastBlockedPkg: String? = null
    58	    private var lastBlockedTime = 0L
    59	    private var wakeLock: PowerManager.WakeLock? = null
    60	    private var waitTimerOverlay: WaitTimerOverlay? = null
    61	    private var appTimerOverlay: AppTimerOverlay? = null
    62	    private var accessibilityObserver: android.database.ContentObserver? = null
    63	    @Volatile private var isAccessibilityEnabled = false
    64	    private var accessibilityBlockOverlay: AccessibilityBlockOverlay? = null
    65	    private var packageInstallReceiver: PackageInstallReceiver? = null
    66	    private var overlayHideCounter = 0
    67	    private val pomodoroNotifIds = mutableMapOf<Int, Int>()
    68	    private var nextPomodoroNotifId = 3000
    69	    private val iconCache = mutableMapOf<String, android.graphics.drawable.Drawable>()
    70	
    71	    private val systemExemptPackages = setOf(
    72	        "android",
    73	        "com.android.systemui",
    74	        "com.android.settings",
    75	        "com.android.intentresolver",
    76	        "com.android.documentsui",
    77	        "com.google.android.documentsui",
    78	        "com.miui.securitycenter",
    79	        "com.miui.securitycore",
    80	        "com.miui.guardprovider",
    81	        "com.miui.systemui.plugin",
    82	        "com.miui.mishare",
    83	        "com.miui.volume",
    84	        "com.miui.securityinputmethod",
    85	        "com.android.permissioncontroller",
    86	        "com.google.android.permissioncontroller",
    87	        "com.android.packageinstaller",
    88	        "com.google.android.packageinstaller",
    89	        "com.android.server.telecom",
    90	        "com.android.phone",
    91	        "com.android.incallui",
    92	        "com.google.android.dialer",
    93	        "com.samsung.android.dialer",
    94	        "com.samsung.android.incallui",
    95	        "com.samsung.android.app.sharelive",
    96	        "com.android.emergency"
    97	    )
    98	
    99	    private val launcherPackages: Set<String> by lazy {
   100	        val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
   101	        packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
   102	            .mapNotNull { it.activityInfo?.packageName }
   103	            .toSet()
   104	    }
   105	
   106	    private val imePackages: Set<String> by lazy {
   107	        val imm = getSystemService(InputMethodManager::class.java)
   108	        imm?.enabledInputMethodList?.map { it.packageName }?.toSet() ?: emptySet()
   109	    }
   110	
   111	    private val dialerPackages: Set<String> by lazy {
   112	        val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:") }
   113	        packageManager.queryIntentActivities(dialIntent, PackageManager.MATCH_DEFAULT_ONLY)
   114	            .mapNotNull { it.activityInfo?.packageName }
   115	            .toSet()
   116	    }
   117	
   118	    private val shareHandlerPackages: Set<String> by lazy {
   119	        val sendIntent = Intent(Intent.ACTION_SEND).apply {
   120	            type = "text/plain"
   121	        }
   122	        packageManager.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)
   123	            .mapNotNull { it.activityInfo?.packageName }
   124	            .filter { pkg ->
   125	                try {
   126	                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
   127	                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
   128	                } catch (_: Exception) {
   129	                    false
   130	                }
   131	            }
   132	            .toSet()
   133	    }
   134	
   135	    private val systemNonLauncherCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
   136	
   137	    private fun isSystemNonLauncherApp(pkg: String): Boolean {
   138	        systemNonLauncherCache[pkg]?.let { return it }
   139	        val result = try {
   140	            val appInfo = packageManager.getApplicationInfo(pkg, 0)
   141	            val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
   142	            isSystem && packageManager.getLaunchIntentForPackage(pkg) == null
   143	        } catch (_: Exception) {
   144	            false
   145	        }
   146	        systemNonLauncherCache[pkg] = result
   147	        return result
   148	    }
   149	
   150	    private val checkRunnable = object : Runnable {
   151	        override fun run() {
   152	            scope.launch { checkExpiredPauses() }
   153	            handler.postDelayed(this, CHECK_INTERVAL_MS)
   154	        }
   155	    }
   156	
   157	    private val usageCheckRunnable = object : Runnable {
   158	        override fun run() {
   159	            scope.launch { checkForegroundApp() }
   160	            if (usagePollingActive) {
   161	                usageHandler.postDelayed(this, USAGE_POLL_INTERVAL_MS)
   162	            }
   163	        }
   164	    }
   165	
   166	    companion object {
   167	        private const val NOTIF_CHANNEL_ID = "qrzen_bg"
   168	        private const val NOTIF_ID = 1001
   169	        private const val CHECK_INTERVAL_MS = 60_000L
   170	        private const val USAGE_POLL_INTERVAL_MS = 2_000L
   171	        private const val BLOCK_COOLDOWN_MS = 3_000L
   172	        private const val OVERLAY_HIDE_DEBOUNCE = 2
   173	
   174	        fun start(context: Context) {
   175	            val intent = Intent(context, BackgroundService::class.java)
   176	            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
   177	                context.startForegroundService(intent)
   178	            } else {
   179	                context.startService(intent)
   180	            }
   181	        }
   182	    }
   183	
   184	    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
   185	        startForeground(NOTIF_ID, buildNotification())
   186	        if (waitTimerOverlay == null) {
   187	            waitTimerOverlay = WaitTimerOverlay(this)
   188	        }
   189	        if (appTimerOverlay == null) {
   190	            appTimerOverlay = AppTimerOverlay(this)
   191	        }
   192	        if (accessibilityBlockOverlay == null) {
   193	            accessibilityBlockOverlay = AccessibilityBlockOverlay(this)
   194	        }
   195	        if (accessibilityObserver == null) {
   196	            isAccessibilityEnabled = checkAccessibilityEnabled()
   197	            val observer = object : android.database.ContentObserver(handler) {
   198	                override fun onChange(selfChange: Boolean) {
   199	                    val wasEnabled = isAccessibilityEnabled
   200	                    isAccessibilityEnabled = checkAccessibilityEnabled()
   201	                    if (wasEnabled && !isAccessibilityEnabled) {
   202	                        scope.launch { checkForegroundApp() }
   203	                    } else if (!wasEnabled && isAccessibilityEnabled) {
   204	                        accessibilityBlockOverlay?.hide()
   205	                    }
   206	                }
   207	            }
   208	            accessibilityObserver = observer
   209	            contentResolver.registerContentObserver(
   210	                android.provider.Settings.Secure.getUriFor(
   211	                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
   212	                ),
   213	                false,
   214	                observer
   215	            )
   216	        }
   217	        if (packageInstallReceiver == null) {
   218	            val receiver = PackageInstallReceiver()
   219	            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
   220	                addDataScheme("package")
   221	            }
   222	            registerReceiver(receiver, filter)
   223	            packageInstallReceiver = receiver
   224	        }
   225	        acquireWakeLock()
   226	        AlarmKeepaliveReceiver.schedule(applicationContext)
   227	        handler.removeCallbacks(checkRunnable)
   228	        handler.post(checkRunnable)
   229	        return START_STICKY
   230	    }
   231	
   232	    private fun checkAccessibilityEnabled(): Boolean {
   233	        val enabled = android.provider.Settings.Secure.getString(
   234	            contentResolver,
   235	            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
   236	        ) ?: return false
   237	        val target = android.content.ComponentName(this, BlockAccessibilityService::class.java)
   238	        val flatFull = target.flattenToString()
   239	        val flatShort = target.flattenToShortString()
   240	        return enabled.split(":").any { entry ->
   241	            entry.equals(flatFull, ignoreCase = true) ||
   242	                entry.equals(flatShort, ignoreCase = true)
   243	        }
   244	    }
   245	
   246	    private suspend fun checkExpiredPauses() {
   247	        val now = System.currentTimeMillis()
   248	        var shouldRefresh = false
   249	        if (Prefs.pauseAllUntil != 0L && now > Prefs.pauseAllUntil) {
   250	            Prefs.pauseAllUntil = 0L
   251	            shouldRefresh = true
   252	        }
   253	        val allBlocks = dao.getAll()
   254	        allBlocks
   255	            .filter { block ->
   256	                block.pausedUntil != 0L &&
   257	                    block.pausedUntil != Long.MAX_VALUE &&
   258	                    now > block.pausedUntil
   259	            }
   260	            .forEach { block ->
   261	                dao.setPausedUntil(block.id, 0L)
   262	                shouldRefresh = true
   263	            }
   264	        blockFolderDao.getAll()
   265	            .filter { folder ->
   266	                folder.pausedUntil != 0L &&
   267	                    folder.pausedUntil != Long.MAX_VALUE &&
   268	                    now > folder.pausedUntil
   269	            }
   270	            .forEach { folder ->
   271	                blockFolderDao.setPausedUntil(folder.id, 0L)
   272	                dao.setPausedUntilByFolderId(folder.id, 0L)
   273	                shouldRefresh = true
   274	            }
   275	        allBlocks
   276	            .filter { block ->
   277	                block.blockNowUntil != 0L &&
   278	                    block.blockNowUntil != Long.MAX_VALUE &&
   279	                    now > block.blockNowUntil
   280	            }
   281	            .forEach { block ->
   282	                dao.update(block.copy(blockNowUntil = 0L))
   283	                shouldRefresh = true
   284	            }
   285	        allBlocks
   286	            .filter { block ->
   287	                block.toggleLockUntil != 0L &&
   288	                    block.toggleLockUntil != Long.MAX_VALUE &&
   289	                    now > block.toggleLockUntil
   290	            }
   291	            .forEach { block ->
   292	                if (block.autoDisableOnToggleLockExpiry || block.isAllowlistMode) {
   293	                    Prefs.clearAllowlistUsageTimer(block.id)
   294	                    if (block.isAllowlistMode) {
   295	                        Prefs.clearAppTimersForBlock(block.id)
   296	                    }
   297	                    Prefs.clearWaitTimerState(block.id)
   298	                    Prefs.clearScheduleWtState(block.id)
   299	                    dao.update(
   300	                        block.copy(
   301	                            isEnabled = false,
   302	                            toggleLockUntil = 0L,
   303	                            autoDisableOnToggleLockExpiry = false,
   304	                            activeUntil = 0L
   305	                        )
   306	                    )
   307	                } else {
   308	                    dao.update(
   309	                        block.copy(
   310	                            toggleLockUntil = 0L,
   311	                            autoDisableOnToggleLockExpiry = false
   312	                        )
   313	                    )
   314	                }
   315	                shouldRefresh = true
   316	            }
   317	        allBlocks
   318	            .filter { block ->
   319	                block.activeUntil != 0L &&
   320	                    block.activeUntil != Long.MAX_VALUE &&
   321	                    now > block.activeUntil
   322	            }
   323	            .forEach { block ->
   324	                Prefs.clearAllowlistUsageTimer(block.id)
   325	                if (block.isAllowlistMode) {
   326	                    Prefs.clearAppTimersForBlock(block.id)
   327	                }
   328	                Prefs.clearWaitTimerState(block.id)
   329	                Prefs.clearScheduleWtState(block.id)
   330	                dao.update(
   331	                    block.copy(
   332	                        isEnabled = false,
   333	                        activeUntil = 0L,
   334	                        toggleLockUntil = 0L,
   335	                        autoDisableOnToggleLockExpiry = false
   336	                    )
   337	                )
   338	                shouldRefresh = true
   339	            }
   340	        allBlocks
   341	            .filter { block ->
   342	                block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
   343	                    block.isEnabled &&
   344	                    block.pomodoroRoundsTotal > 0
   345	            }
   346	            .forEach { block ->
   347	                val state = UnlockMethodUtils.computePomodoroState(block, now)
   348	                if (!state.isSessionActive) {
   349	                    dao.update(
   350	                        block.copy(
   351	                            isEnabled = false,
   352	                            pomodoroRoundsTotal = 0,
   353	                            pomodoroSessionStartMillis = 0L,
   354	                            toggleLockUntil = 0L,
   355	                            autoDisableOnToggleLockExpiry = false,
   356	                            activeUntil = 0L
   357	                        )
   358	                    )
   359	                    cancelPomodoroBreakNotification(block.id)
   360	                    shouldRefresh = true
   361	                } else if (state.isInBreak) {
   362	                    showPomodoroBreakNotification(block, state)
   363	                } else {
   364	                    cancelPomodoroBreakNotification(block.id)
   365	                }
   366	            }
   367	        val currentlyActiveIds = mutableSetOf<Int>()
   368	        allBlocks
   369	            .filter { it.isEnabled && !it.isArchived && it.pausedUntil <= now }
   370	            .forEach { block ->
   371	                if (isBlockActive(block)) currentlyActiveIds.add(block.id)
   372	            }
   373	        val newlyActive = currentlyActiveIds - previouslyActiveBlockIds
   374	        previouslyActiveBlockIds.clear()
   375	        previouslyActiveBlockIds.addAll(currentlyActiveIds)
   376	        if (newlyActive.isNotEmpty()) {
   377	            sendToHome()
   378	            shouldRefresh = true
   379	        }
   380	        if (shouldRefresh) WidgetRefresh.refresh(applicationContext)
   381	        resetAppTimersOnWindowChange(allBlocks)
   382	
   383	        val hasActiveBlocks = currentlyActiveIds.isNotEmpty()
   384	        val hasWaitTimerBlocks = allBlocks.any {
   385	            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
   386	                it.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER
   387	        }
   388	        val hasPomodoroBlocks = allBlocks.any {
   389	            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
   390	                it.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
   391	                it.pomodoroRoundsTotal > 0
   392	        }
   393	        val hasScheduleBreakBlocks = allBlocks.any {
   394	            it.isEnabled && !it.isArchived && it.pausedUntil <= now &&
   395	                it.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
   396	                it.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_NONE
   397	        }
   398	        val needsPolling =
   399	            hasActiveBlocks || hasWaitTimerBlocks || hasPomodoroBlocks || hasScheduleBreakBlocks
   400	        if (needsPolling) {
   401	            startUsagePolling()
   402	        } else {
   403	            stopUsagePolling()
   404	        }
   405	    }
   406	
   407	    private suspend fun isBlockActive(block: AppBlock, foregroundPkg: String? = null): Boolean {
   408	        if (block.blockNowUntil > System.currentTimeMillis()) return true
   409	
   410	        return when (block.blockingStyle) {
   411	            UnlockMethodUtils.STYLE_MANUAL -> true
   412	            UnlockMethodUtils.STYLE_POMODORO -> {
   413	                val state = UnlockMethodUtils.computePomodoroState(block)
   414	                state.isInFocus
   415	            }
   416	            UnlockMethodUtils.STYLE_SCHEDULE -> {
   417	                val scheduleActive = isScheduleActive(block)
   418	                when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
   419	                    UnlockMethodUtils.BREAK_NONE -> scheduleActive
   420	                    UnlockMethodUtils.BREAK_POMODORO -> scheduleActive && isSchedulePomodoroBlocking(block)
   421	                    UnlockMethodUtils.BREAK_WAIT_TIMER -> scheduleActive && isScheduleWaitTimerBlocking(block, foregroundPkg)
   422	                    UnlockMethodUtils.BREAK_USAGE_LIMIT -> scheduleActive && isScheduleUsageLimitExceeded(block)
   423	                    UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> scheduleActive && isScheduledAllowanceExhausted(block)
   424	                    else -> scheduleActive
   425	                }
   426	            }
   427	            UnlockMethodUtils.STYLE_USAGE_LIMIT -> isUsageLimitExceeded(block)
   428	            UnlockMethodUtils.STYLE_WAIT_TIMER -> isWaitTimerBlocking(block, foregroundPkg)
   429	            else -> isScheduleActive(block)
   430	        }
   431	    }
   432	
   433	    private suspend fun isScheduleActive(block: AppBlock): Boolean {
   434	        return isScheduleActive(block, timeBlockDao.getByBlockId(block.id))
   435	    }
   436	
   437	    private fun isScheduleActive(block: AppBlock, timeBlocks: List<TimeBlock>): Boolean {
   438	        return if (timeBlocks.isEmpty()) {
   439	            isLegacyScheduleActive(block)
   440	        } else {
   441	            UnlockMethodUtils.isScheduleCurrentlyActive(block, timeBlocks)
   442	        }
   443	    }
   444	
   445	    private fun isLegacyScheduleActive(block: AppBlock): Boolean {
   446	        return UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block) != null
   447	    }
   448	
   449	    private suspend fun isSchedulePomodoroBlocking(block: AppBlock): Boolean {
   450	        val timeBlocks = timeBlockDao.getByBlockId(block.id)
   451	        val windowStartMs = if (timeBlocks.isEmpty()) {
   452	            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block)
   453	        } else {
   454	            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks)
   455	        } ?: return true
   456	        return isSchedulePomodoroBlocking(block, windowStartMs)
   457	    }
   458	
   459	    private fun isSchedulePomodoroBlocking(block: AppBlock, windowStartMs: Long): Boolean {
   460	        val now = System.currentTimeMillis()
   461	        val elapsed = now - windowStartMs
   462	        if (elapsed < 0L) return true
   463	        val focusMs = block.pomodoroDurationMin * 60_000L
   464	        val breakMs = block.pomodoroBreakMin * 60_000L
   465	        val cycleMs = focusMs + breakMs
   466	        if (cycleMs <= 0L) return true
   467	        val positionInCycle = elapsed % cycleMs
   468	        return positionInCycle < focusMs
   469	    }
   470	
   471	    private suspend fun isScheduleUsageLimitExceeded(block: AppBlock): Boolean {
   472	        val timeBlocks = timeBlockDao.getByBlockId(block.id)
   473	        if (timeBlocks.isEmpty()) {
   474	            return isUsageLimitExceeded(block)
   475	        }
   476	        val cal = Calendar.getInstance()
   477	        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
   478	        val hasTodayBlock = timeBlocks.any { it.activeDays.getOrNull(dayIndex) == '1' }
   479	        if (!hasTodayBlock) return false
   480	        return computeUsageLimitRemainingMs(block) <= 0L
   481	    }
   482	
   483	    private fun isScheduleWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
   484	        return Prefs.getScheduleWtBlockingUntil(block.id) > System.currentTimeMillis()
   485	    }
   486	
   487	    private fun isScheduledAllowanceExhausted(block: AppBlock): Boolean {
   488	        return Prefs.getSchedAllowanceRemaining(block.id) == 0L
   489	    }
   490	
   491	    private fun isUsageLimitExceeded(block: AppBlock): Boolean {
   492	        val cal = Calendar.getInstance()
   493	        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
   494	        if (block.activeDays.getOrNull(dayIndex) != '1') return false
   495	        return computeUsageLimitRemainingMs(block) <= 0L
   496	    }
   497	
   498	    private fun computeUsageLimitRemainingMs(block: AppBlock): Long {
   499	        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return block.usageLimitMinutes * 60_000L
   500	        val now = System.currentTimeMillis()
   501	        val startTime = when (block.usageLimitPeriod) {
   502	            "HOURLY" -> now - 3_600_000L
   503	            else -> {
   504	                Calendar.getInstance().apply {
   505	                    set(Calendar.HOUR_OF_DAY, 0)
   506	                    set(Calendar.MINUTE, 0)
   507	                    set(Calendar.SECOND, 0)
   508	                    set(Calendar.MILLISECOND, 0)
   509	                }.timeInMillis
   510	            }
   511	        }
   512	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   513	        val events = usageStatsManager.queryEvents(startTime, now)
   514	        val event = UsageEvents.Event()
   515	        val foregroundStartTimes = mutableMapOf<String, Long>()
   516	        var totalUsageMs = 0L
   517	
   518	        while (events.hasNextEvent()) {
   519	            events.getNextEvent(event)
   520	            val pkg = event.packageName ?: continue
   521	            if (pkg !in packages) continue
   522	            when (event.eventType) {
   523	                UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundStartTimes[pkg] = event.timeStamp
   524	                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
   525	                    val start = foregroundStartTimes.remove(pkg)
   526	                    if (start != null) totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
   527	                }
   528	            }
   529	        }
   530	        for ((_, start) in foregroundStartTimes) {
   531	            totalUsageMs += (now - start).coerceAtLeast(0L)
   532	        }
   533	        val limitMs = block.usageLimitMinutes * 60_000L
   534	        return (limitMs - totalUsageMs).coerceAtLeast(0L)
   535	    }
   536	
   537	    private fun isWaitTimerBlocking(block: AppBlock, foregroundPkg: String? = null): Boolean {
   538	        val cal = Calendar.getInstance()
   539	        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
   540	        if (block.activeDays.getOrNull(dayIndex) != '1') return false
   541	
   542	        val kv = MMKV.defaultMMKV()
   543	        val now = System.currentTimeMillis()
   544	        val blockingUntilKey = "wait_timer_blocking_${block.id}"
   545	        val remainingKey = "wait_timer_remaining_${block.id}"
   546	        val lastUpdateKey = "wait_timer_last_update_${block.id}"
   547	        val inAppKey = "wait_timer_in_app_${block.id}"
   548	        val maxRemainingMs = block.waitTimerUseMinutes * 60_000L
   549	
   550	        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)
   551	        if (blockingUntil > now) return true
   552	
   553	        if (blockingUntil > 0L) {
   554	            kv.encode(remainingKey, maxRemainingMs)
   555	            kv.encode(blockingUntilKey, 0L)
   556	            kv.encode(lastUpdateKey, now)
   557	            kv.encode(inAppKey, false)
   558	            return false
   559	        }
   560	
   561	        if (foregroundPkg == null) return false
   562	
   563	        var remaining = kv.decodeLong(remainingKey, -1L)
   564	        if (remaining < 0L) {
   565	            remaining = maxRemainingMs
   566	            kv.encode(remainingKey, remaining)
   567	            kv.encode(lastUpdateKey, now)
   568	            kv.encode(inAppKey, false)
   569	            return false
   570	        }
   571	
   572	        val lastUpdate = kv.decodeLong(lastUpdateKey, now)
   573	        val elapsed = (now - lastUpdate).coerceAtLeast(0L)
   574	        val wasInApp = kv.decodeBool(inAppKey, false)
   575	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   576	        val isInApp = foregroundPkg in packages
   577	
   578	        if (isInApp) {
   579	            if (wasInApp) {
   580	                val decrement = elapsed.coerceAtMost(5_000L)
   581	                remaining = (remaining - decrement).coerceAtLeast(0L)
   582	            }
   583	            kv.encode(inAppKey, true)
   584	        } else {
   585	            if (block.waitTimerAdaptive && !wasInApp && elapsed > 0L) {
   586	                val refillRate = block.waitTimerUseMinutes.toDouble() / block.waitTimerWaitMinutes.toDouble()
   587	                val refillMs = (elapsed * refillRate).toLong()
   588	                remaining = (remaining + refillMs).coerceAtMost(maxRemainingMs)
   589	            }
   590	            kv.encode(inAppKey, false)
   591	        }
   592	
   593	        kv.encode(remainingKey, remaining)
   594	        kv.encode(lastUpdateKey, now)
   595	
   596	        if (remaining <= 0L) {
   597	            val waitUntil = now + block.waitTimerWaitMinutes * 60_000L
   598	            kv.encode(blockingUntilKey, waitUntil)
   599	            return true
   600	        }
   601	
   602	        return false
   603	    }
   604	
   605	    private suspend fun trackScheduleBreakState(blocks: List<AppBlock>, foregroundPkg: String?) {
   606	        for (block in blocks) {
   607	            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
   608	            when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
   609	                UnlockMethodUtils.BREAK_WAIT_TIMER -> {
   610	                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
   611	                    trackScheduleWaitTimerState(block, foregroundPkg, isScheduleActive(block, timeBlocks))
   612	                }
   613	                UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> {
   614	                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
   615	                    trackScheduledAllowanceState(block, foregroundPkg, isScheduleActive(block, timeBlocks), timeBlocks)
   616	                }
   617	            }
   618	        }
   619	    }
   620	
   621	    private fun trackScheduleWaitTimerState(block: AppBlock, foregroundPkg: String?, scheduleActive: Boolean) {
   622	        val blockId = block.id
   623	        val now = System.currentTimeMillis()
   624	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   625	        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages
   626	
   627	        if (!scheduleActive) {
   628	            if (block.waitTimerAdaptive) {
   629	                Prefs.clearScheduleWtState(blockId)
   630	            }
   631	            Prefs.setScheduleWtLastTick(blockId, 0L)
   632	            return
   633	        }
   634	
   635	        val blockingUntil = Prefs.getScheduleWtBlockingUntil(blockId)
   636	        if (blockingUntil > now) {
   637	            Prefs.setScheduleWtLastTick(blockId, 0L)
   638	            return
   639	        }
   640	
   641	        if (blockingUntil > 0L && blockingUntil <= now) {
   642	            Prefs.clearScheduleWtState(blockId)
   643	            return
   644	        }
   645	
   646	        val budgetMs = block.waitTimerUseMinutes * 60_000L
   647	        if (isUsingBlockedApp) {
   648	            val lastTick = Prefs.getScheduleWtLastTick(blockId)
   649	            var usedMs = Prefs.getScheduleWtUsedMs(blockId)
   650	            if (lastTick > 0L) {
   651	                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
   652	                usedMs = (usedMs + delta).coerceAtMost(budgetMs)
   653	            }
   654	            if (usedMs >= budgetMs) {
   655	                Prefs.setScheduleWtBlockingUntil(blockId, now + block.waitTimerWaitMinutes * 60_000L)
   656	                Prefs.setScheduleWtUsedMs(blockId, 0L)
   657	                Prefs.setScheduleWtLastTick(blockId, 0L)
   658	            } else {
   659	                Prefs.setScheduleWtUsedMs(blockId, usedMs)
   660	                Prefs.setScheduleWtLastTick(blockId, now)
   661	            }
   662	            return
   663	        }
   664	
   665	        val lastTick = Prefs.getScheduleWtLastTick(blockId)
   666	        if (block.waitTimerAdaptive) {
   667	            if (lastTick > 0L) {
   668	                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
   669	                val refillRate = if (block.waitTimerWaitMinutes > 0) {
   670	                    block.waitTimerUseMinutes.toDouble() / block.waitTimerWaitMinutes.toDouble()
   671	                } else {
   672	                    0.0
   673	                }
   674	                val refillMs = (delta * refillRate).toLong()
   675	                val usedMs = (Prefs.getScheduleWtUsedMs(blockId) - refillMs).coerceAtLeast(0L)
   676	                Prefs.setScheduleWtUsedMs(blockId, usedMs)
   677	            }
   678	            Prefs.setScheduleWtLastTick(blockId, now)
   679	        } else {
   680	            Prefs.setScheduleWtLastTick(blockId, 0L)
   681	        }
   682	    }
   683	
   684	    private fun trackScheduledAllowanceState(
   685	        block: AppBlock,
   686	        foregroundPkg: String?,
   687	        scheduleActive: Boolean,
   688	        timeBlocks: List<TimeBlock>
   689	    ) {
   690	        val blockId = block.id
   691	        val now = System.currentTimeMillis()
   692	
   693	        if (!scheduleActive) {
   694	            Prefs.setSchedAllowanceLastTick(blockId, 0L)
   695	            return
   696	        }
   697	
   698	        val currentWindowStart = if (timeBlocks.isEmpty()) {
   699	            UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block, now)
   700	        } else {
   701	            UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks, now)
   702	        } ?: return
   703	        val savedWindowStart = Prefs.getSchedAllowanceWindowStart(blockId)
   704	        val maxAllowanceMs = block.scheduledAllowanceMinutes * 60_000L
   705	
   706	        if (savedWindowStart != currentWindowStart || Prefs.getSchedAllowanceRemaining(blockId) < 0L) {
   707	            Prefs.setSchedAllowanceRemaining(blockId, maxAllowanceMs)
   708	            Prefs.setSchedAllowanceWindowStart(blockId, currentWindowStart)
   709	            Prefs.setSchedAllowanceLastTick(blockId, 0L)
   710	        }
   711	
   712	        val remaining = Prefs.getSchedAllowanceRemaining(blockId)
   713	        if (remaining <= 0L) {
   714	            Prefs.setSchedAllowanceLastTick(blockId, 0L)
   715	            return
   716	        }
   717	
   718	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   719	        val isUsingBlockedApp = foregroundPkg != null && foregroundPkg in packages
   720	        if (isUsingBlockedApp) {
   721	            val lastTick = Prefs.getSchedAllowanceLastTick(blockId)
   722	            if (lastTick > 0L) {
   723	                val delta = (now - lastTick).coerceIn(0L, USAGE_POLL_INTERVAL_MS * 2)
   724	                val newRemaining = (remaining - delta).coerceAtLeast(0L)
   725	                Prefs.setSchedAllowanceRemaining(blockId, newRemaining)
   726	                Prefs.setSchedAllowanceLastTick(blockId, if (newRemaining > 0L) now else 0L)
   727	            } else {
   728	                Prefs.setSchedAllowanceLastTick(blockId, now)
   729	            }
   730	        } else {
   731	            Prefs.setSchedAllowanceLastTick(blockId, 0L)
   732	        }
   733	    }
   734	
   735	    private suspend fun resetAppTimersOnWindowChange(blocks: List<AppBlock>) {
   736	        val now = System.currentTimeMillis()
   737	        val todayStartMs = java.util.Calendar.getInstance().apply {
   738	            set(java.util.Calendar.HOUR_OF_DAY, 0)
   739	            set(java.util.Calendar.MINUTE, 0)
   740	            set(java.util.Calendar.SECOND, 0)
   741	            set(java.util.Calendar.MILLISECOND, 0)
   742	        }.timeInMillis
   743	
   744	        for (block in blocks) {
   745	            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
   746	
   747	            when (block.blockingStyle) {
   748	                UnlockMethodUtils.STYLE_SCHEDULE -> {
   749	                    val timeBlocks = timeBlockDao.getByBlockId(block.id)
   750	                    val currentWindowStart = if (timeBlocks.isEmpty()) {
   751	                        UnlockMethodUtils.computeLegacyScheduleWindowStartMs(block, now)
   752	                    } else {
   753	                        UnlockMethodUtils.computeCurrentWindowStartMs(timeBlocks, now)
   754	                    }
   755	
   756	                    if (currentWindowStart == null) {
   757	                        Prefs.setAppTimerWindowStart(block.id, 0L)
   758	                        continue
   759	                    }
   760	
   761	                    val savedWindowStart = Prefs.getAppTimerWindowStart(block.id)
   762	                    if (savedWindowStart != currentWindowStart) {
   763	                        Prefs.resetAppTimersForBlock(block.id)
   764	                        Prefs.setAppTimerWindowStart(block.id, currentWindowStart)
   765	                    }
   766	                }
   767	
   768	                UnlockMethodUtils.STYLE_MANUAL -> {
   769	                    val savedWindowStart = Prefs.getAppTimerWindowStart(block.id)
   770	                    if (savedWindowStart != todayStartMs) {
   771	                        Prefs.resetAppTimersForBlock(block.id)
   772	                        Prefs.setAppTimerWindowStart(block.id, todayStartMs)
   773	                    }
   774	                }
   775	            }
   776	        }
   777	    }
   778	
   779	    private fun pauseScheduleBreakUsageTracking(blocks: List<AppBlock>) {
   780	        for (block in blocks) {
   781	            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
   782	            when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
   783	                UnlockMethodUtils.BREAK_WAIT_TIMER -> Prefs.setScheduleWtLastTick(block.id, 0L)
   784	                UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> Prefs.setSchedAllowanceLastTick(block.id, 0L)
   785	            }
   786	        }
   787	    }
   788	
   789	    private fun buildNotification(): Notification {
   790	        val channel = NotificationChannel(
   791	            NOTIF_CHANNEL_ID,
   792	            "QR Zen",
   793	            NotificationManager.IMPORTANCE_MIN
   794	        ).apply { setShowBadge(false) }
   795	        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
   796	
   797	        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
   798	            .setContentTitle("QR Zen is active")
   799	            .setSmallIcon(R.drawable.ic_notification)
   800	            .setPriority(NotificationCompat.PRIORITY_MIN)
   801	            .setOngoing(true)
   802	            .build()
   803	    }
   804	
   805	    private fun showPomodoroBreakNotification(
   806	        block: AppBlock,
   807	        state: UnlockMethodUtils.PomodoroState
   808	    ) {
   809	        val nm = getSystemService(NotificationManager::class.java)
   810	        val channelId = "qrzen_pomodoro_break"
   811	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
   812	            val channel = NotificationChannel(
   813	                channelId,
   814	                "Pomodoro Breaks",
   815	                NotificationManager.IMPORTANCE_LOW
   816	            ).apply { setShowBadge(false) }
   817	            nm.createNotificationChannel(channel)
   818	        }
   819	        val notifId = pomodoroNotifIds.getOrPut(block.id) { nextPomodoroNotifId++ }
   820	        val breakEndMs = System.currentTimeMillis() + state.periodRemainingMs
   821	        val notification = NotificationCompat.Builder(this, channelId)
   822	            .setSmallIcon(R.drawable.ic_notification)
   823	            .setContentTitle("☕ Break – ${block.title}")
   824	            .setContentText(
   825	                "Round ${state.currentRound}/${state.totalRounds} complete. Next round in ${
   826	                    UnlockMethodUtils.formatCountdown(state.periodRemainingMs)
   827	                }"
   828	            )
   829	            .setWhen(breakEndMs)
   830	            .setUsesChronometer(true)
   831	            .setChronometerCountDown(true)
   832	            .setOngoing(true)
   833	            .setPriority(NotificationCompat.PRIORITY_LOW)
   834	            .build()
   835	        nm.notify(notifId, notification)
   836	    }
   837	
   838	    private fun cancelPomodoroBreakNotification(blockId: Int) {
   839	        val notifId = pomodoroNotifIds.remove(blockId) ?: return
   840	        getSystemService(NotificationManager::class.java).cancel(notifId)
   841	    }
   842	
   843	    private fun isExemptPackage(pkg: String): Boolean {
   844	        return pkg == packageName ||
   845	            pkg in systemExemptPackages ||
   846	            pkg in launcherPackages ||
   847	            pkg in imePackages ||
   848	            pkg in dialerPackages ||
   849	            pkg in shareHandlerPackages ||
   850	            isSystemNonLauncherApp(pkg)
   851	    }
   852	
   853	    private fun isPackageTrackedByBlock(block: AppBlock, pkg: String): Boolean {
   854	        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
   855	        return pkg in packages
   856	    }
   857	
   858	    private fun isDeviceLocked(): Boolean {
   859	        val keyguardManager = getSystemService(KeyguardManager::class.java) ?: return false
   860	        return keyguardManager.isKeyguardLocked
   861	    }
   862	
   863	    private fun getAppLabel(pkg: String): String {
   864	        return try {
   865	            packageManager.getApplicationLabel(
   866	                packageManager.getApplicationInfo(pkg, 0)
   867	            ).toString()
   868	        } catch (_: Exception) {
   869	            pkg
   870	        }
   871	    }
   872	
   873	    private fun getFreshImePackages(): Set<String> {
   874	        return getSystemService(InputMethodManager::class.java)
   875	            ?.enabledInputMethodList
   876	            ?.map { it.packageName }
   877	            ?.toSet()
   878	            ?: emptySet()
   879	    }
   880	
   881	    private fun getForegroundPackage(): String? {
   882	        val accessibilityPkg = BlockAccessibilityService.currentForegroundPackage
   883	        if (accessibilityPkg != null && BlockAccessibilityService.isRunning) {
   884	            return accessibilityPkg
   885	        }
   886	
   887	        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return null
   888	        val endTime = System.currentTimeMillis()
   889	        val startTime = endTime - 60_000
   890	        val events = usageStatsManager.queryEvents(startTime, endTime)
   891	        var lastPkg: String? = null
   892	        val event = UsageEvents.Event()
   893	        while (events.hasNextEvent()) {
   894	            events.getNextEvent(event)
   895	            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
   896	                lastPkg = event.packageName
   897	            }
   898	        }
   899	        return lastPkg
   900	    }
   901	
   902	    private suspend fun checkForegroundApp() {
   903	        isAccessibilityEnabled = checkAccessibilityEnabled()
   904	        val now = System.currentTimeMillis()
   905	        val allCandidates = dao.getAll().filter {
   906	            it.isEnabled && !it.isArchived && now > it.pausedUntil
   907	        }
   908	        if (isDeviceLocked()) {
   909	            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
   910	            waitTimerOverlay?.hide()
   911	            pauseUsageTimers(allCandidates)
   912	            pauseScheduleBreakUsageTracking(allCandidates)
   913	            appTimerOverlay?.hide()
   914	            accessibilityBlockOverlay?.hide()
   915	            DiagnosticNotifier.cancelPollState(applicationContext)
   916	            return
   917	        }
   918	        if (Prefs.pauseAllUntil > now) {
   919	            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
   920	            waitTimerOverlay?.hide()
   921	            pauseUsageTimers(allCandidates)
   922	            pauseScheduleBreakUsageTracking(allCandidates)
   923	            appTimerOverlay?.hide()
   924	            accessibilityBlockOverlay?.hide()
   925	            DiagnosticNotifier.cancelPollState(applicationContext)
   926	            return
   927	        }
   928	
   929	        val pkg = getForegroundPackage()
   930	        trackScheduleBreakState(allCandidates, pkg)
   931	        if (pkg == null) {
   932	            updateTimerOverlays(allCandidates, null)
   933	            pauseUsageTimers(allCandidates)
   934	            appTimerOverlay?.hide()
   935	            accessibilityBlockOverlay?.hide()
   936	            DiagnosticNotifier.cancelPollState(applicationContext)
   937	            return
   938	        }
   939	        if (isExemptPackage(pkg)) {
   940	            updateTimerOverlays(allCandidates, null)
   941	            pauseUsageTimers(allCandidates)
   942	            appTimerOverlay?.hide()
   943	            accessibilityBlockOverlay?.hide()
   944	            DiagnosticNotifier.cancelPollState(applicationContext)
   945	            return
   946	        }
   947	        if (pkg == lastBlockedPkg && now - lastBlockedTime < BLOCK_COOLDOWN_MS) {
   948	            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
   949	            waitTimerOverlay?.hide()
   950	            pauseUsageTimers(allCandidates)
   951	            appTimerOverlay?.hide()
   952	            accessibilityBlockOverlay?.hide()
   953	            DiagnosticNotifier.cancelPollState(applicationContext)
   954	            return
   955	        }
   956	        val activeBlocks = mutableListOf<AppBlock>()
   957	        for (block in allCandidates) {
   958	            if (isBlockActive(block, pkg)) activeBlocks.add(block)
   959	        }
   960	
   961	        val blocklistBlock = activeBlocks
   962	            .filter { !it.isAllowlistMode }
   963	            .firstOrNull { block ->
   964	                block.appPackages.split(",").map { it.trim() }.contains(pkg)
   965	            }
   966	
   967	        val allowlistBlocks = activeBlocks.filter { it.isAllowlistMode }
   968	        var allowedForegroundPkg: String? = pkg
   969	        if (allowlistBlocks.isNotEmpty()) {
   970	            val allowedSets = allowlistBlocks.map { block ->
   971	                block.appPackages.split(",")
   972	                    .map { it.trim() }
   973	                    .filter { it.isNotEmpty() }
   974	                    .filterNot { Prefs.isAppTimerExpired(block.id, it) }
   975	                    .toSet()
   976	            }
   977	            val intersection = allowedSets.reduce { acc, set -> acc.intersect(set) }
   978	            if (!intersection.contains(pkg)) {
   979	                lastBlockedPkg = pkg
   980	                lastBlockedTime = now
   981	                overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
   982	                waitTimerOverlay?.hide()
   983	                allowedForegroundPkg = null
   984	                accessibilityBlockOverlay?.hide()
   985	            }
   986	        }
   987	
   988	        if (Prefs.diagnosticNotifications) {
   989	            val freshImePackages = getFreshImePackages()
   990	            val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
   991	            val exemptReason = when {
   992	                pkg == packageName -> "self"
   993	                pkg in systemExemptPackages -> "system"
   994	                pkg in launcherPackages -> "launcher"
   995	                pkg in imePackages -> "keyboard/IME"
   996	                pkg in dialerPackages -> "dialer"
   997	                pkg in shareHandlerPackages -> "share handler"
   998	                isSystemNonLauncher -> "system non-launcher"
   999	                else -> null
  1000	            }
  1001	            DiagnosticNotifier.notifyPollState(
  1002	                context = applicationContext,
  1003	                source = if (isAccessibilityEnabled) "Accessibility" else "UsageStats",
  1004	                detectedPkg = pkg,
  1005	                appLabel = getAppLabel(pkg),
  1006	                cachedImePackages = imePackages,
  1007	                freshImePackages = freshImePackages,
  1008	                isSystemNonLauncher = isSystemNonLauncher,
  1009	                isExempt = isExemptPackage(pkg),
  1010	                exemptReason = exemptReason,
  1011	                activeBlockCount = activeBlocks.size,
  1012	                blocklistMatchName = blocklistBlock?.title,
  1013	                allowlistResult = if (allowlistBlocks.isNotEmpty()) {
  1014	                    if (allowedForegroundPkg != null) "allowed" else "BLOCKED"
  1015	                } else {
  1016	                    null
  1017	                }
  1018	            )
  1019	        }
  1020	
  1021	        if (blocklistBlock != null) {
  1022	            lastBlockedPkg = pkg
  1023	            lastBlockedTime = now
  1024	            overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
  1025	            waitTimerOverlay?.hide()
  1026	            pauseUsageTimers(allCandidates)
  1027	            appTimerOverlay?.hide()
  1028	            accessibilityBlockOverlay?.hide()
  1029	            launchLockScreen(pkg, blocklistBlock)
  1030	            if (Prefs.diagnosticNotifications) {
  1031	                val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
  1032	                DiagnosticNotifier.notifyBlockTriggered(
  1033	                    context = applicationContext,
  1034	                    source = "UsageStats",
  1035	                    detectedPkg = pkg,
  1036	                    appLabel = getAppLabel(pkg),
  1037	                    triggerType = "Blocklist",
  1038	                    matchedBlocks = listOf(blocklistBlock),
  1039	                    cachedImePackages = imePackages,
  1040	                    freshImePackages = getFreshImePackages(),
  1041	                    isSystemNonLauncher = isSystemNonLauncher,
  1042	                    exemptReason = null,
  1043	                    extraInfo = null
  1044	                )
  1045	            }
  1046	            return
  1047	        }
  1048	
  1049	        if (allowedForegroundPkg == null) {
  1050	            launchAllowlistOverlay(pkg, allowlistBlocks)
  1051	            if (Prefs.diagnosticNotifications) {
  1052	                val isSystemNonLauncher = isSystemNonLauncherApp(pkg)
  1053	                DiagnosticNotifier.notifyBlockTriggered(
  1054	                    context = applicationContext,
  1055	                    source = "UsageStats",
  1056	                    detectedPkg = pkg,
  1057	                    appLabel = getAppLabel(pkg),
  1058	                    triggerType = "Allowlist",
  1059	                    matchedBlocks = allowlistBlocks,
  1060	                    cachedImePackages = imePackages,
  1061	                    freshImePackages = getFreshImePackages(),
  1062	                    isSystemNonLauncher = isSystemNonLauncher,
  1063	                    exemptReason = null,
  1064	                    extraInfo = "Pkg not in allowlist intersection"
  1065	                )
  1066	            }
  1067	        }
  1068	
  1069	        updateTimerOverlays(allCandidates, pkg)
  1070	        if (!isAccessibilityEnabled) {
  1071	            val accessibilityMessage = when {
  1072	                allCandidates.any { block ->
  1073	                    block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
  1074	                        block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } == UnlockMethodUtils.BREAK_WAIT_TIMER &&
  1075	                        isPackageTrackedByBlock(block, pkg) &&
  1076	                        isScheduleActive(block)
  1077	                } -> getString(R.string.accessibility_block_wait_timer)
  1078	                allCandidates.any { block ->
  1079	                    block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE &&
  1080	                        block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } == UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE &&
  1081	                        isPackageTrackedByBlock(block, pkg) &&
  1082	                        isScheduleActive(block)
  1083	                } -> getString(R.string.accessibility_block_scheduled_allowance)
  1084	                allCandidates.any { block ->
  1085	                    block.blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER &&
  1086	                        isPackageTrackedByBlock(block, pkg)
  1087	                } -> getString(R.string.accessibility_block_wait_timer)
  1088	                else -> null
  1089	            }
  1090	            if (accessibilityMessage != null) {
  1091	                accessibilityBlockOverlay?.show(accessibilityMessage)
  1092	                sendToHome()
  1093	                return
  1094	            }
  1095	        } else {
  1096	            accessibilityBlockOverlay?.hide()
  1097	        }
  1098	        accessibilityBlockOverlay?.hide()
  1099	        trackAllowlistUsageTimers(activeBlocks, allowedForegroundPkg ?: "")
  1100	        trackPerAppTimers(activeBlocks, allowedForegroundPkg ?: "")
  1101	        updateAppTimerOverlay(activeBlocks, allowedForegroundPkg)
  1102	    }
  1103	
  1104	    private fun pauseUsageTimers(blocks: List<AppBlock>) {
  1105	        trackAllowlistUsageTimers(blocks, "")
  1106	        trackPerAppTimers(blocks, "")
  1107	    }
  1108	
  1109	    private fun trackAllowlistUsageTimers(blocks: List<AppBlock>, foregroundPkg: String) {
  1110	        // Block-level timer is now pure wall-clock via activeUntil;
  1111	        // expiry handled by checkExpiredPauses().
  1112	    }
  1113	
  1114	    private fun trackPerAppTimers(blocks: List<AppBlock>, foregroundPkg: String) {
  1115	        val now = System.currentTimeMillis()
  1116	        for (block in blocks) {
  1117	            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
  1118	            for (pkg in block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
  1119	                val remaining = Prefs.getAppTimerRemaining(block.id, pkg)
  1120	                if (remaining <= 0L) continue
  1121	
  1122	                if (Prefs.getAppTimerOriginal(block.id, pkg) <= 0L) {
  1123	                    Prefs.setAppTimerOriginal(block.id, pkg, remaining)
  1124	                }
  1125	
  1126	                if (pkg == foregroundPkg) {
  1127	                    val lastFg = Prefs.getAppTimerLastFg(block.id, pkg)
  1128	                    if (lastFg > 0L) {
  1129	                        val elapsed = (now - lastFg).coerceAtLeast(0L).coerceAtMost(5_000L)
  1130	                        val newRemaining = (remaining - elapsed).coerceAtLeast(0L)
  1131	                        Prefs.setAppTimerRemaining(block.id, pkg, newRemaining)
  1132	                        if (newRemaining <= 0L) {
  1133	                            Prefs.setAppTimerLastFg(block.id, pkg, 0L)
  1134	                            continue
  1135	                        }
  1136	                    }
  1137	                    Prefs.setAppTimerLastFg(block.id, pkg, now)
  1138	                } else {
  1139	                    Prefs.setAppTimerLastFg(block.id, pkg, 0L)
  1140	                }
  1141	            }
  1142	        }
  1143	    }
  1144	
  1145	    private fun updateAppTimerOverlay(blocks: List<AppBlock>, foregroundPkg: String?) {
  1146	        if (foregroundPkg == null || isExemptPackage(foregroundPkg)) {
  1147	            appTimerOverlay?.hide()
  1148	            return
  1149	        }
  1150	
  1151	        val entries = mutableListOf<AppTimerOverlay.TimerEntry>()
  1152	        var perAppRemaining = -1L
  1153	        for (block in blocks) {
  1154	            if (!block.isAllowlistMode || !block.isEnabled || block.isArchived) continue
  1155	            val remaining = Prefs.getAppTimerRemaining(block.id, foregroundPkg)
  1156	            if (remaining > 0L) {
  1157	                perAppRemaining = if (perAppRemaining < 0L) remaining else minOf(perAppRemaining, remaining)
  1158	            }
  1159	        }
  1160	        if (perAppRemaining > 0L) {
  1161	            val icon = getOrLoadIcon(foregroundPkg)
  1162	            if (icon != null) {
  1163	                entries.add(AppTimerOverlay.TimerEntry(foregroundPkg, icon, perAppRemaining))
  1164	            }
  1165	        }
  1166	
  1167	        if (entries.isNotEmpty()) {
  1168	            appTimerOverlay?.update(entries)
  1169	        } else {
  1170	            appTimerOverlay?.hide()
  1171	        }
  1172	    }
  1173	
  1174	    private fun getOrLoadIcon(pkg: String): android.graphics.drawable.Drawable? {
  1175	        iconCache[pkg]?.let { return it }
  1176	        return try {
  1177	            packageManager.getApplicationIcon(pkg).also { iconCache[pkg] = it }
  1178	        } catch (_: Exception) {
  1179	            null
  1180	        }
  1181	    }
  1182	
  1183	    private suspend fun updateTimerOverlays(blocks: List<AppBlock>, foregroundPkg: String?) {
  1184	        val kv = MMKV.defaultMMKV()
  1185	        val now = System.currentTimeMillis()
  1186	        val entries = mutableListOf<WaitTimerOverlay.TimerEntry>()
  1187	
  1188	        for (block in blocks) {
  1189	            if (block.blockingStyle != UnlockMethodUtils.STYLE_WAIT_TIMER) continue
  1190	            if (!block.showTimer) continue
  1191	            if (kv.decodeBool("wait_timer_in_app_${block.id}", false)) {
  1192	                val remaining = kv.decodeLong("wait_timer_remaining_${block.id}", -1L)
  1193	                if (remaining > 0L) {
  1194	                    entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
  1195	                }
  1196	            }
  1197	        }
  1198	
  1199	        for (block in blocks) {
  1200	            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
  1201	            if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_WAIT_TIMER) continue
  1202	            if (!block.showTimer) continue
  1203	            val blockingUntil = Prefs.getScheduleWtBlockingUntil(block.id)
  1204	            if (blockingUntil > now) {
  1205	                entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, blockingUntil - now))
  1206	            }
  1207	        }
  1208	
  1209	        for (block in blocks) {
  1210	            if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
  1211	            if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_POMODORO) continue
  1212	            if (!block.showTimer) continue
  1213	            val timeBlocks = timeBlockDao.getByBlockId(block.id)
  1214	            val phase = UnlockMethodUtils.computeSchedulePomodoroPhase(block, timeBlocks, now)
  1215	            if (phase.isActive && phase.phaseRemainingMs > 0L) {
  1216	                val phaseLabel = if (phase.isInFocus) "\uD83C\uDFAF ${block.title}" else "\u2615 ${block.title}"
  1217	                entries.add(WaitTimerOverlay.TimerEntry(block.id, phaseLabel, phase.phaseRemainingMs))
  1218	            }
  1219	        }
  1220	
  1221	        if (foregroundPkg != null) {
  1222	            for (block in blocks) {
  1223	                if (block.blockingStyle != UnlockMethodUtils.STYLE_USAGE_LIMIT) continue
  1224	                if (!block.showTimer) continue
  1225	                val cal = Calendar.getInstance()
  1226	                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
  1227	                if (block.activeDays.getOrNull(dayIndex) != '1') continue
  1228	                val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
  1229	                if (foregroundPkg in packages) {
  1230	                    val remaining = computeUsageLimitRemainingMs(block)
  1231	                    if (remaining > 0L) {
  1232	                        entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
  1233	                    }
  1234	                }
  1235	            }
  1236	
  1237	            for (block in blocks) {
  1238	                if (block.blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) continue
  1239	                if (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE } != UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE) continue
  1240	                if (!block.showTimer) continue
  1241	                if (!isPackageTrackedByBlock(block, foregroundPkg)) continue
  1242	                if (!isScheduleActive(block)) continue
  1243	                val remaining = Prefs.getSchedAllowanceRemaining(block.id)
  1244	                if (remaining > 0L) {
  1245	                    entries.add(WaitTimerOverlay.TimerEntry(block.id, block.title, remaining))
  1246	                }
  1247	            }
  1248	        }
  1249	
  1250	        if (entries.isNotEmpty()) {
  1251	            overlayHideCounter = 0
  1252	            waitTimerOverlay?.update(entries)
  1253	        } else {
  1254	            overlayHideCounter++
  1255	            if (overlayHideCounter >= OVERLAY_HIDE_DEBOUNCE) {
  1256	                waitTimerOverlay?.hide()
  1257	            }
  1258	        }
  1259	    }
  1260	
  1261	    private fun launchLockScreen(blockedPkg: String, block: AppBlock) {
  1262	        startActivity(Intent(this, LockScreenActivity::class.java).apply {
  1263	            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
  1264	            putExtra(LockScreenActivity.EXTRA_BLOCK_ID, block.id)
  1265	            putExtra(LockScreenActivity.EXTRA_BLOCKED_PKG, blockedPkg)
  1266	        })
  1267	    }
  1268	
  1269	    private fun launchAllowlistOverlay(blockedPkg: String, blocks: List<AppBlock>) {
  1270	        startActivity(Intent(this, AllowlistOverlayActivity::class.java).apply {
  1271	            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
  1272	            putExtra(AllowlistOverlayActivity.EXTRA_BLOCK_IDS, blocks.map { it.id }.toIntArray())
  1273	            putExtra(AllowlistOverlayActivity.EXTRA_BLOCKED_PKG, blockedPkg)
  1274	        })
  1275	    }
  1276	
  1277	    private fun sendToHome() {
  1278	        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
  1279	            addCategory(Intent.CATEGORY_HOME)
  1280	            flags = Intent.FLAG_ACTIVITY_NEW_TASK
  1281	        }
  1282	        applicationContext.startActivity(homeIntent)
  1283	    }
  1284	
  1285	    private fun startUsagePolling() {
  1286	        if (usagePollingActive) return
  1287	        usagePollingActive = true
  1288	        usageHandler.post(usageCheckRunnable)
  1289	    }
  1290	
  1291	    private fun stopUsagePolling() {
  1292	        usagePollingActive = false
  1293	        usageHandler.removeCallbacks(usageCheckRunnable)
  1294	        lastBlockedPkg = null
  1295	        overlayHideCounter = OVERLAY_HIDE_DEBOUNCE
  1296	        waitTimerOverlay?.hide()
  1297	        appTimerOverlay?.hide()
  1298	        accessibilityBlockOverlay?.hide()
  1299	        DiagnosticNotifier.cancelPollState(applicationContext)
  1300	    }
  1301	
  1302	    private fun acquireWakeLock() {
  1303	        if (wakeLock == null) {
  1304	            val powerManager = getSystemService(PowerManager::class.java)
  1305	            wakeLock = powerManager?.newWakeLock(
  1306	                PowerManager.PARTIAL_WAKE_LOCK,
  1307	                "QrZen::BackgroundService"
  1308	            )
  1309	            wakeLock?.acquire()
  1310	        }
  1311	    }
  1312	
  1313	    override fun onDestroy() {
  1314	        val nm = getSystemService(NotificationManager::class.java)
  1315	        pomodoroNotifIds.values.forEach { nm.cancel(it) }
  1316	        pomodoroNotifIds.clear()
  1317	        accessibilityObserver?.let { contentResolver.unregisterContentObserver(it) }
  1318	        accessibilityObserver = null
  1319	        waitTimerOverlay?.destroy()
  1320	        waitTimerOverlay = null
  1321	        appTimerOverlay?.destroy()
  1322	        appTimerOverlay = null
  1323	        accessibilityBlockOverlay?.destroy()
  1324	        accessibilityBlockOverlay = null
  1325	        packageInstallReceiver?.let {
  1326	            try { unregisterReceiver(it) } catch (_: Exception) {}
  1327	            packageInstallReceiver = null
  1328	        }
  1329	        iconCache.clear()
  1330	        scope.cancel()
  1331	        stopUsagePolling()
  1332	        wakeLock?.let {
  1333	            if (it.isHeld) it.release()
  1334	            wakeLock = null
  1335	        }
  1336	        handler.removeCallbacks(checkRunnable)
  1337	        super.onDestroy()
  1338	    }
  1339	
  1340	    override fun onBind(intent: Intent?): IBinder? = null
  1341	}
  1342	