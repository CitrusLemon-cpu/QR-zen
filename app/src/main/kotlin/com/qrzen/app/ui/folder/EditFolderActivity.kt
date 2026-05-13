     1	package com.qrzen.app.ui.folder
     2	
     3	import android.os.Bundle
     4	import android.view.MenuItem
     5	import android.view.View
     6	import android.widget.ArrayAdapter
     7	import android.widget.NumberPicker
     8	import android.widget.Toast
     9	import android.widget.ToggleButton
    10	import androidx.appcompat.app.AppCompatActivity
    11	import androidx.lifecycle.lifecycleScope
    12	import com.google.android.material.datepicker.MaterialDatePicker
    13	import com.google.android.material.timepicker.MaterialTimePicker
    14	import com.google.android.material.timepicker.TimeFormat
    15	import com.qrzen.app.R
    16	import com.qrzen.app.data.db.BlockFolderDao
    17	import com.qrzen.app.data.model.BlockFolder
    18	import com.qrzen.app.databinding.ActivityEditFolderBinding
    19	import com.qrzen.app.ui.unlock.UnlockMethodUtils
    20	import dagger.hilt.android.AndroidEntryPoint
    21	import kotlinx.coroutines.launch
    22	import java.text.SimpleDateFormat
    23	import java.util.Calendar
    24	import java.util.Locale
    25	import java.util.TimeZone
    26	import java.util.UUID
    27	import javax.inject.Inject
    28	
    29	@AndroidEntryPoint
    30	class EditFolderActivity : AppCompatActivity() {
    31	
    32	    companion object {
    33	        const val EXTRA_FOLDER_ID = "extra_folder_id"
    34	    }
    35	
    36	    @Inject lateinit var blockFolderDao: BlockFolderDao
    37	
    38	    private lateinit var binding: ActivityEditFolderBinding
    39	
    40	    private var existingFolder: BlockFolder? = null
    41	    private var currentQrSecret: String = ""
    42	    private var unlockMethod: String = UnlockMethodUtils.METHOD_NONE
    43	    private var delayMinutes: Int = 5
    44	    private var blockPassword: String = ""
    45	    private var typeOverText: String = ""
    46	    private var typeOverIsRandom: Boolean = false
    47	    private var editWindowStart: String = "09:00"
    48	    private var editWindowEnd: String = "10:00"
    49	    private var editWindowDays: String = "1111111"
    50	    private var lockUntil: Long = 0L
    51	
    52	    private val unlockMethods = listOf(
    53	        UnlockMethodUtils.METHOD_NONE to R.string.unlock_method_none,
    54	        UnlockMethodUtils.METHOD_DELAY to R.string.unlock_method_delay,
    55	        UnlockMethodUtils.METHOD_PASSWORD to R.string.unlock_method_password,
    56	        UnlockMethodUtils.METHOD_TYPE_OVER_TEXT to R.string.unlock_method_type_over,
    57	        UnlockMethodUtils.METHOD_QR_CODE to R.string.unlock_method_qr_code,
    58	        UnlockMethodUtils.METHOD_EDIT_WINDOW to R.string.unlock_method_edit_window,
    59	        UnlockMethodUtils.METHOD_TIMER to R.string.unlock_method_timer
    60	    )
    61	
    62	    override fun onCreate(savedInstanceState: Bundle?) {
    63	        super.onCreate(savedInstanceState)
    64	        binding = ActivityEditFolderBinding.inflate(layoutInflater)
    65	        setContentView(binding.root)
    66	
    67	        setSupportActionBar(binding.toolbar)
    68	        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    69	        binding.toolbar.setNavigationOnClickListener { finish() }
    70	        binding.toolbar.inflateMenu(R.menu.menu_edit_folder)
    71	        binding.toolbar.setOnMenuItemClickListener(::onToolbarMenuItemSelected)
    72	
    73	        setupUi()
    74	
    75	        val folderId = intent.getIntExtra(EXTRA_FOLDER_ID, -1)
    76	        if (folderId == -1) {
    77	            currentQrSecret = UUID.randomUUID().toString()
    78	            supportActionBar?.title = getString(R.string.edit_folder_new_title)
    79	            applyCurrentStateToUi()
    80	        } else {
    81	            supportActionBar?.title = getString(R.string.edit_folder_edit_title)
    82	            lifecycleScope.launch {
    83	                val folder = blockFolderDao.getById(folderId) ?: run {
    84	                    finish()
    85	                    return@launch
    86	                }
    87	                existingFolder = folder
    88	                populateForm(folder)
    89	            }
    90	        }
    91	    }
    92	
    93	    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    94	        return when (item.itemId) {
    95	            android.R.id.home -> {
    96	                finish()
    97	                true
    98	            }
    99	            else -> super.onOptionsItemSelected(item)
   100	        }
   101	    }
   102	
   103	    private fun onToolbarMenuItemSelected(item: MenuItem): Boolean {
   104	        return when (item.itemId) {
   105	            R.id.action_save_folder -> {
   106	                saveFolder()
   107	                true
   108	            }
   109	            else -> false
   110	        }
   111	    }
   112	
   113	    private fun setupUi() {
   114	        binding.actUnlockMethod.keyListener = null
   115	
   116	        binding.npDelayMinutes.minValue = 1
   117	        binding.npDelayMinutes.maxValue = 60
   118	        binding.npDelayMinutes.value = delayMinutes
   119	        binding.npDelayMinutes.setOnValueChangedListener { _, _, newVal ->
   120	            delayMinutes = newVal
   121	        }
   122	
   123	        setupUnlockMethodDropdown()
   124	
   125	        binding.switchTypeOverRandom.setOnCheckedChangeListener { _, isChecked ->
   126	            typeOverIsRandom = isChecked
   127	            updateTypeOverUi()
   128	        }
   129	
   130	        binding.btnEditWindowStart.setOnClickListener {
   131	            showTimePicker(getString(R.string.unlock_edit_window_start), editWindowStart) { time ->
   132	                editWindowStart = time
   133	                updateEditWindowButtons()
   134	            }
   135	        }
   136	        binding.btnEditWindowEnd.setOnClickListener {
   137	            showTimePicker(getString(R.string.unlock_edit_window_end), editWindowEnd) { time ->
   138	                editWindowEnd = time
   139	                updateEditWindowButtons()
   140	            }
   141	        }
   142	        binding.btnLockUntil.setOnClickListener { showLockUntilPicker() }
   143	    }
   144	
   145	    private fun setupUnlockMethodDropdown() {
   146	        refreshUnlockMethodDropdown()
   147	        binding.actUnlockMethod.setOnItemClickListener { _, _, position, _ ->
   148	            unlockMethod = unlockMethods[position].first
   149	            updateUnlockMethodUi()
   150	        }
   151	    }
   152	
   153	    private fun refreshUnlockMethodDropdown() {
   154	        binding.actUnlockMethod.setAdapter(
   155	            ArrayAdapter(this, android.R.layout.simple_list_item_1, unlockMethods.map { getString(it.second) })
   156	        )
   157	        binding.actUnlockMethod.setText(getUnlockMethodLabel(unlockMethod), false)
   158	    }
   159	
   160	    private fun populateForm(folder: BlockFolder) {
   161	        currentQrSecret = folder.qrSecret.ifBlank { UUID.randomUUID().toString() }
   162	        unlockMethod = folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }
   163	        delayMinutes = folder.delayMinutes.coerceIn(1, 60)
   164	        blockPassword = folder.blockPassword
   165	        typeOverText = folder.typeOverText
   166	        typeOverIsRandom = folder.typeOverIsRandom
   167	        editWindowStart = folder.editWindowStart.ifBlank { "09:00" }
   168	        editWindowEnd = folder.editWindowEnd.ifBlank { "10:00" }
   169	        editWindowDays = folder.editWindowDays.ifBlank { "1111111" }
   170	        lockUntil = folder.lockUntil
   171	
   172	        binding.etTitle.setText(folder.title)
   173	        binding.npDelayMinutes.value = delayMinutes
   174	        binding.etBlockPassword.setText(blockPassword)
   175	        binding.etConfirmPassword.setText(blockPassword)
   176	        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
   177	        binding.etTypeOverText.setText(typeOverText)
   178	        setToggleStates(editWindowDayToggles(), editWindowDays)
   179	        applyCurrentStateToUi()
   180	    }
   181	
   182	    private fun applyCurrentStateToUi() {
   183	        refreshUnlockMethodDropdown()
   184	        binding.tvQrSecret.text = currentQrSecret
   185	        binding.npDelayMinutes.value = delayMinutes.coerceIn(1, 60)
   186	        binding.etTypeOverText.setText(typeOverText)
   187	        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
   188	        setToggleStates(editWindowDayToggles(), editWindowDays)
   189	        updateEditWindowButtons()
   190	        updateLockUntilDisplay()
   191	        updateTypeOverUi()
   192	        updateUnlockMethodUi()
   193	    }
   194	
   195	    private fun updateUnlockMethodUi() {
   196	        binding.llUnlockNone.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_NONE) View.VISIBLE else View.GONE
   197	        binding.llUnlockDelay.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_DELAY) View.VISIBLE else View.GONE
   198	        binding.llUnlockPassword.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_PASSWORD) View.VISIBLE else View.GONE
   199	        binding.llUnlockTypeOver.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_TYPE_OVER_TEXT) View.VISIBLE else View.GONE
   200	        binding.llUnlockQr.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_QR_CODE) View.VISIBLE else View.GONE
   201	        binding.llUnlockEditWindow.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_EDIT_WINDOW) View.VISIBLE else View.GONE
   202	        binding.llUnlockTimer.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_TIMER) View.VISIBLE else View.GONE
   203	    }
   204	
   205	    private fun updateTypeOverUi() {
   206	        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
   207	        binding.tvTypeOverRandomInfo.visibility = if (typeOverIsRandom) View.VISIBLE else View.GONE
   208	        binding.tilTypeOverText.visibility = if (typeOverIsRandom) View.GONE else View.VISIBLE
   209	    }
   210	
   211	    private fun updateEditWindowButtons() {
   212	        binding.btnEditWindowStart.text = "${getString(R.string.unlock_edit_window_start)}: $editWindowStart"
   213	        binding.btnEditWindowEnd.text = "${getString(R.string.unlock_edit_window_end)}: $editWindowEnd"
   214	    }
   215	
   216	    private fun updateLockUntilDisplay() {
   217	        binding.tvLockUntilValue.text = if (lockUntil > 0L) {
   218	            formatDateTime(lockUntil)
   219	        } else {
   220	            "${getString(R.string.unlock_timer_lock_until)}: —"
   221	        }
   222	    }
   223	
   224	    private fun saveFolder() {
   225	        val title = binding.etTitle.text?.toString()?.trim() ?: ""
   226	        if (title.isEmpty()) {
   227	            binding.tilTitle.error = getString(R.string.edit_folder_title_required)
   228	            return
   229	        }
   230	
   231	        binding.tilTitle.error = null
   232	        binding.tilBlockPassword.error = null
   233	        binding.tilConfirmPassword.error = null
   234	        binding.tilTypeOverText.error = null
   235	
   236	        delayMinutes = binding.npDelayMinutes.value
   237	        blockPassword = binding.etBlockPassword.text?.toString() ?: ""
   238	        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
   239	        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
   240	        typeOverText = binding.etTypeOverText.text?.toString()?.trim() ?: ""
   241	        editWindowDays = buildDaysString(editWindowDayToggles())
   242	
   243	        when (unlockMethod) {
   244	            UnlockMethodUtils.METHOD_PASSWORD -> {
   245	                if (blockPassword.isEmpty()) {
   246	                    binding.tilBlockPassword.error = getString(R.string.edit_folder_password_required)
   247	                    return
   248	                }
   249	                if (blockPassword != confirmPassword) {
   250	                    binding.tilConfirmPassword.error = getString(R.string.edit_folder_passwords_mismatch)
   251	                    return
   252	                }
   253	            }
   254	            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> {
   255	                if (!typeOverIsRandom && typeOverText.isEmpty()) {
   256	                    binding.tilTypeOverText.error = getString(R.string.edit_folder_type_over_required)
   257	                    return
   258	                }
   259	            }
   260	            UnlockMethodUtils.METHOD_TIMER -> {
   261	                if (lockUntil <= System.currentTimeMillis()) {
   262	                    Toast.makeText(this, R.string.edit_folder_lock_until_future, Toast.LENGTH_SHORT).show()
   263	                    return
   264	                }
   265	            }
   266	        }
   267	
   268	        if (currentQrSecret.isBlank()) {
   269	            currentQrSecret = UUID.randomUUID().toString()
   270	        }
   271	
   272	        val folder = BlockFolder(
   273	            id = existingFolder?.id ?: 0,
   274	            title = title,
   275	            isEnabled = existingFolder?.isEnabled ?: true,
   276	            pausedUntil = existingFolder?.pausedUntil ?: 0L,
   277	            isCollapsed = existingFolder?.isCollapsed ?: false,
   278	            sortOrder = existingFolder?.sortOrder ?: 0,
   279	            unlockMethod = unlockMethod,
   280	            delayMinutes = delayMinutes,
   281	            blockPassword = blockPassword,
   282	            typeOverText = typeOverText,
   283	            typeOverIsRandom = typeOverIsRandom,
   284	            editWindowStart = editWindowStart,
   285	            editWindowEnd = editWindowEnd,
   286	            editWindowDays = editWindowDays,
   287	            lockUntil = lockUntil,
   288	            qrSecret = currentQrSecret,
   289	            masterPasswordEnabled = existingFolder?.masterPasswordEnabled ?: false
   290	        )
   291	
   292	        lifecycleScope.launch {
   293	            if (existingFolder == null) {
   294	                blockFolderDao.insert(folder)
   295	            } else {
   296	                blockFolderDao.update(folder)
   297	            }
   298	            finish()
   299	        }
   300	    }
   301	
   302	    private fun showTimePicker(title: String, current: String, onPicked: (String) -> Unit) {
   303	        val parts = current.split(":")
   304	        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
   305	        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
   306	        val picker = MaterialTimePicker.Builder()
   307	            .setTitleText(title)
   308	            .setTimeFormat(TimeFormat.CLOCK_24H)
   309	            .setHour(hour)
   310	            .setMinute(minute)
   311	            .build()
   312	        picker.addOnPositiveButtonClickListener {
   313	            onPicked(String.format(Locale.US, "%02d:%02d", picker.hour, picker.minute))
   314	        }
   315	        picker.show(supportFragmentManager, "folder_time_picker_${title.replace(" ", "_")}")
   316	    }
   317	
   318	    private fun showLockUntilPicker() {
   319	        val now = System.currentTimeMillis()
   320	        val initialSelection = if (lockUntil > now) lockUntil else now
   321	        val datePicker = MaterialDatePicker.Builder.datePicker()
   322	            .setTitleText(getString(R.string.unlock_timer_lock_until))
   323	            .setSelection(toUtcDateSelection(initialSelection))
   324	            .build()
   325	        datePicker.addOnPositiveButtonClickListener { selectedDate ->
   326	            showLockUntilTimePicker(selectedDate)
   327	        }
   328	        datePicker.show(supportFragmentManager, "folder_lock_until_date_picker")
   329	    }
   330	
   331	    private fun showLockUntilTimePicker(selectedDateUtcMillis: Long) {
   332	        val currentLock = Calendar.getInstance().apply {
   333	            if (lockUntil > System.currentTimeMillis()) {
   334	                timeInMillis = lockUntil
   335	            }
   336	        }
   337	        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
   338	            timeInMillis = selectedDateUtcMillis
   339	        }
   340	        val picker = MaterialTimePicker.Builder()
   341	            .setTitleText(getString(R.string.unlock_timer_pick_date))
   342	            .setTimeFormat(TimeFormat.CLOCK_24H)
   343	            .setHour(currentLock.get(Calendar.HOUR_OF_DAY))
   344	            .setMinute(currentLock.get(Calendar.MINUTE))
   345	            .build()
   346	        picker.addOnPositiveButtonClickListener {
   347	            val localCalendar = Calendar.getInstance().apply {
   348	                set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
   349	                set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
   350	                set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
   351	                set(Calendar.HOUR_OF_DAY, picker.hour)
   352	                set(Calendar.MINUTE, picker.minute)
   353	                set(Calendar.SECOND, 0)
   354	                set(Calendar.MILLISECOND, 0)
   355	            }
   356	            lockUntil = localCalendar.timeInMillis
   357	            updateLockUntilDisplay()
   358	        }
   359	        picker.show(supportFragmentManager, "folder_lock_until_time_picker")
   360	    }
   361	
   362	    private fun buildDaysString(toggles: List<ToggleButton>): String = buildString {
   363	        toggles.forEach { append(if (it.isChecked) '1' else '0') }
   364	    }
   365	
   366	    private fun setToggleStates(toggles: List<ToggleButton>, days: String) {
   367	        val paddedDays = days.padEnd(7, '0')
   368	        toggles.forEachIndexed { index, toggle ->
   369	            toggle.isChecked = paddedDays.getOrNull(index) == '1'
   370	        }
   371	    }
   372	
   373	    private fun editWindowDayToggles(): List<ToggleButton> = listOf(
   374	        binding.toggleEditMon,
   375	        binding.toggleEditTue,
   376	        binding.toggleEditWed,
   377	        binding.toggleEditThu,
   378	        binding.toggleEditFri,
   379	        binding.toggleEditSat,
   380	        binding.toggleEditSun
   381	    )
   382	
   383	    private fun getUnlockMethodLabel(method: String): String {
   384	        return unlockMethods.firstOrNull { it.first == method }?.second?.let(::getString)
   385	            ?: getString(R.string.unlock_method_none)
   386	    }
   387	
   388	    private fun formatDateTime(epochMillis: Long): String {
   389	        return SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault()).format(epochMillis)
   390	    }
   391	
   392	    private fun toUtcDateSelection(epochMillis: Long): Long {
   393	        val localCalendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
   394	        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
   395	            set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
   396	            set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
   397	            set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
   398	            set(Calendar.HOUR_OF_DAY, 0)
   399	            set(Calendar.MINUTE, 0)
   400	            set(Calendar.SECOND, 0)
   401	            set(Calendar.MILLISECOND, 0)
   402	        }.timeInMillis
   403	    }
   404	}
   405	