package com.qrzen.app.ui.block

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityEditBlockBinding
import com.qrzen.app.databinding.ItemEditAppGridBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditBlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_IS_ALLOWLIST = "extra_is_allowlist"

        private const val UNLOCK_NONE = "NONE"
        private const val UNLOCK_DELAY = "DELAY"
        private const val UNLOCK_PASSWORD = "PASSWORD"
        private const val UNLOCK_TYPE_OVER_TEXT = "TYPE_OVER_TEXT"
        private const val UNLOCK_QR_CODE = "QR_CODE"
        private const val UNLOCK_EDIT_WINDOW = "EDIT_WINDOW"
        private const val UNLOCK_TIMER = "TIMER"
        private const val UNLOCK_WHILE_ACTIVE = "WHILE_ACTIVE"
    }

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private lateinit var binding: ActivityEditBlockBinding
    private lateinit var selectedBlockDetailView: View
    private lateinit var selectedBlockStartButton: MaterialButton
    private lateinit var selectedBlockEndButton: MaterialButton
    private lateinit var selectedBlockDeleteButton: MaterialButton
    private lateinit var selectedDayViews: List<TextView>

    private var existingBlock: AppBlock? = null
    private var currentQrSecret: String = ""
    private var selectedPackages: String = ""
    private var isAllowlistMode: Boolean = false
    private var blockingStyle: String = UnlockMethodUtils.STYLE_MANUAL
    private var unlockMethod: String = UNLOCK_NONE
    private var delayMinutes: Int = 5
    private var blockPassword: String = ""
    private var typeOverText: String = ""
    private var typeOverIsRandom: Boolean = true
    private var editWindowStart: String = "09:00"
    private var editWindowEnd: String = "10:00"
    private var editWindowDays: String = "1111111"
    private var activeDays: String = "1111111"
    private var usageLimitMinutes: Int = 30
    private var usageLimitPeriod: String = "DAILY"
    private var waitTimerWaitMinutes: Int = 30
    private var waitTimerUseMinutes: Int = 5
    private var waitTimerAdaptive: Boolean = false
    private var pomodoroDurationMin: Int = 25
    private var pomodoroBreakMin: Int = 5
    private var scheduleBreakType: String = UnlockMethodUtils.BREAK_NONE
    private var scheduledAllowanceMinutes: Int = 10
    private var timerBreakMinutes: Int = 0
    private var showTimer: Boolean = false
    private var lockUntil: Long = 0L
    private var pomodoroLockEditing: Boolean = false
    private var currentTimeBlocks: MutableList<TimeBlock> = mutableListOf()
    private var nextTempId: Int = -1
    private var selectedTimeBlockId: Int? = null
    private var selectedAppsLoadJob: Job? = null
    private var isUpdatingTimerBreakPresets = false
    private val pendingAppTimers = mutableMapOf<String, Long>()
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val qrScanForSetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanned = result.data?.getStringExtra(com.king.zxing.CameraScan.SCAN_RESULT)
            if (!scanned.isNullOrBlank()) {
                currentQrSecret = scanned
                binding.tvQrSecret.text = scanned
            }
        }
    }

    private val blockingStyles: List<Pair<String, String>>
        get() {
            val base = listOf(
                UnlockMethodUtils.STYLE_MANUAL to getString(R.string.blocking_style_manual),
                UnlockMethodUtils.STYLE_SCHEDULE to getString(R.string.blocking_style_schedule),
                UnlockMethodUtils.STYLE_POMODORO to getString(R.string.blocking_style_pomodoro)
            )
            return if (isAllowlistMode) {
                base
            } else {
                base + listOf(
                    UnlockMethodUtils.STYLE_USAGE_LIMIT to getString(R.string.blocking_style_usage_limit),
                    UnlockMethodUtils.STYLE_WAIT_TIMER to getString(R.string.blocking_style_wait_timer)
                )
            }
        }

    private val unlockMethods: List<Pair<String, String>>
        get() {
            val all = listOf(
                UNLOCK_NONE to getString(R.string.unlock_method_none),
                UNLOCK_DELAY to getString(R.string.unlock_method_delay),
                UNLOCK_PASSWORD to getString(R.string.unlock_method_password),
                UNLOCK_TYPE_OVER_TEXT to getString(R.string.unlock_method_type_over),
                UNLOCK_QR_CODE to getString(R.string.unlock_method_qr_code),
                UNLOCK_EDIT_WINDOW to getString(R.string.unlock_method_edit_window),
                UNLOCK_TIMER to getString(R.string.unlock_method_timer),
                UNLOCK_WHILE_ACTIVE to getString(R.string.unlock_method_while_active)
            )
            return if (blockingStyle == UnlockMethodUtils.STYLE_POMODORO) {
                all.filter { it.first != UNLOCK_TIMER }
            } else {
                all
            }
        }

    private val scheduleBreakTypes: List<Pair<String, String>>
        get() = listOf(
            UnlockMethodUtils.BREAK_NONE to getString(R.string.schedule_break_type_none_option),
            UnlockMethodUtils.BREAK_POMODORO to getString(R.string.schedule_break_type_pomodoro_option),
            UnlockMethodUtils.BREAK_WAIT_TIMER to getString(R.string.schedule_break_type_wait_timer_option),
            UnlockMethodUtils.BREAK_USAGE_LIMIT to getString(R.string.schedule_break_type_usage_limit_option),
            UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE to getString(R.string.schedule_break_type_allowance_option)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        isAllowlistMode = intent.getBooleanExtra(EXTRA_IS_ALLOWLIST, false)
        setupUi()

        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
        if (blockId == -1) {
            currentQrSecret = UUID.randomUUID().toString()
            supportActionBar?.title = if (isAllowlistMode) "New Allowlist Block" else "New Block"
            applyCurrentStateToUi()
        } else {
            supportActionBar?.title = "Edit Block"
            lifecycleScope.launch {
                val block = dao.getById(blockId) ?: run {
                    finish()
                    return@launch
                }
                existingBlock = block
                currentQrSecret = block.qrSecret
                populateForm(block)
            }
        }
    }

    private fun setupUi() {
        selectedBlockDetailView = findViewById(R.id.selectedBlockDetail)
        selectedBlockStartButton = findViewById(R.id.btnSelectedBlockStartTime)
        selectedBlockEndButton = findViewById(R.id.btnSelectedBlockEndTime)
        selectedBlockDeleteButton = findViewById(R.id.btnDeleteSelectedTimeBlock)
        selectedDayViews = listOf(
            findViewById(R.id.tvSelectedDaySun),
            findViewById(R.id.tvSelectedDayMon),
            findViewById(R.id.tvSelectedDayTue),
            findViewById(R.id.tvSelectedDayWed),
            findViewById(R.id.tvSelectedDayThu),
            findViewById(R.id.tvSelectedDayFri),
            findViewById(R.id.tvSelectedDaySat)
        )
        selectedBlockDetailView.isClickable = true
        selectedBlockStartButton.setOnClickListener { editSelectedTimeBlock(isStart = true) }
        selectedBlockEndButton.setOnClickListener { editSelectedTimeBlock(isStart = false) }
        selectedBlockDeleteButton.setOnClickListener { deleteSelectedTimeBlock() }

        binding.rvSelectedApps.layoutManager = GridLayoutManager(this, 5)
        binding.actUnlockMethod.keyListener = null
        binding.actBlockingStyle.keyListener = null
        binding.actScheduleBreakType.keyListener = null

        binding.npDelayMinutes.minValue = 1
        binding.npDelayMinutes.maxValue = 60
        binding.npDelayMinutes.value = delayMinutes
        binding.npDelayMinutes.setOnValueChangedListener { _, _, newVal ->
            delayMinutes = newVal
        }

        binding.npUsageLimitMinutes.minValue = 1
        binding.npUsageLimitMinutes.maxValue = 480
        binding.npUsageLimitMinutes.value = usageLimitMinutes
        binding.npUsageLimitMinutes.setOnValueChangedListener { _, _, newVal ->
            usageLimitMinutes = newVal
            syncUsageLimitPickers()
        }

        binding.npWaitTimerWait.minValue = 1
        binding.npWaitTimerWait.maxValue = 120
        binding.npWaitTimerWait.value = waitTimerWaitMinutes
        binding.npWaitTimerWait.setOnValueChangedListener { _, _, newVal ->
            waitTimerWaitMinutes = newVal
            syncWaitTimerUi()
        }

        binding.npWaitTimerUse.minValue = 1
        binding.npWaitTimerUse.maxValue = 120
        binding.npWaitTimerUse.value = waitTimerUseMinutes
        binding.npWaitTimerUse.setOnValueChangedListener { _, _, newVal ->
            waitTimerUseMinutes = newVal
            syncWaitTimerUi()
        }
        binding.cbWaitTimerAdaptive.setOnCheckedChangeListener { _, isChecked ->
            waitTimerAdaptive = isChecked
            syncWaitTimerUi()
        }
        binding.cbShowTimer.setOnCheckedChangeListener { _, isChecked ->
            showTimer = isChecked
        }

        binding.npPomodoroDuration.minValue = 1
        binding.npPomodoroDuration.maxValue = 120
        binding.npPomodoroDuration.value = pomodoroDurationMin
        binding.npPomodoroDuration.setOnValueChangedListener { _, _, newVal ->
            pomodoroDurationMin = newVal
            syncPomodoroConfigUi()
        }

        binding.npPomodoroBreak.minValue = 1
        binding.npPomodoroBreak.maxValue = 60
        binding.npPomodoroBreak.value = pomodoroBreakMin
        binding.npPomodoroBreak.setOnValueChangedListener { _, _, newVal ->
            pomodoroBreakMin = newVal
            syncPomodoroConfigUi()
        }

        binding.npScheduleBreakPomodoroDuration.minValue = 1
        binding.npScheduleBreakPomodoroDuration.maxValue = 120
        binding.npScheduleBreakPomodoroDuration.value = pomodoroDurationMin
        binding.npScheduleBreakPomodoroDuration.setOnValueChangedListener { _, _, newVal ->
            pomodoroDurationMin = newVal
            syncPomodoroConfigUi()
        }

        binding.npScheduleBreakPomodoroBreak.minValue = 1
        binding.npScheduleBreakPomodoroBreak.maxValue = 60
        binding.npScheduleBreakPomodoroBreak.value = pomodoroBreakMin
        binding.npScheduleBreakPomodoroBreak.setOnValueChangedListener { _, _, newVal ->
            pomodoroBreakMin = newVal
            syncPomodoroConfigUi()
        }

        binding.npScheduleBreakWaitUse.minValue = 1
        binding.npScheduleBreakWaitUse.maxValue = 120
        binding.npScheduleBreakWaitUse.value = waitTimerUseMinutes
        binding.npScheduleBreakWaitUse.setOnValueChangedListener { _, _, newVal ->
            waitTimerUseMinutes = newVal
            syncWaitTimerUi()
        }

        binding.npScheduleBreakWaitBlock.minValue = 1
        binding.npScheduleBreakWaitBlock.maxValue = 120
        binding.npScheduleBreakWaitBlock.value = waitTimerWaitMinutes
        binding.npScheduleBreakWaitBlock.setOnValueChangedListener { _, _, newVal ->
            waitTimerWaitMinutes = newVal
            syncWaitTimerUi()
        }

        binding.cbScheduleBreakWaitAdaptive.setOnCheckedChangeListener { _, isChecked ->
            waitTimerAdaptive = isChecked
            syncWaitTimerUi()
        }

        binding.npScheduleBreakUsageLimitMinutes.minValue = 1
        binding.npScheduleBreakUsageLimitMinutes.maxValue = 480
        binding.npScheduleBreakUsageLimitMinutes.value = usageLimitMinutes
        binding.npScheduleBreakUsageLimitMinutes.setOnValueChangedListener { _, _, newVal ->
            usageLimitMinutes = newVal
            syncUsageLimitPickers()
        }

        binding.npScheduledAllowanceMinutes.minValue = 1
        binding.npScheduledAllowanceMinutes.maxValue = 120
        binding.npScheduledAllowanceMinutes.value = scheduledAllowanceMinutes
        binding.npScheduledAllowanceMinutes.setOnValueChangedListener { _, _, newVal ->
            scheduledAllowanceMinutes = newVal
            syncScheduledAllowanceUi()
        }

        binding.cbPomodoroLockEditing.setOnCheckedChangeListener { _, isChecked ->
            pomodoroLockEditing = isChecked
        }

        setupBlockingStyleDropdown()
        setupUnlockMethodDropdown()
        setupScheduleBreakDropdown()

        binding.switchTypeOverRandom.setOnCheckedChangeListener { _, isChecked ->
            typeOverIsRandom = isChecked
            updateTypeOverUi()
        }

        binding.btnEditWindowStart.setOnClickListener {
            showTimePicker(getString(R.string.unlock_edit_window_start), editWindowStart) { time ->
                editWindowStart = time
                updateEditWindowButtons()
            }
        }
        binding.btnEditWindowEnd.setOnClickListener {
            showTimePicker(getString(R.string.unlock_edit_window_end), editWindowEnd) { time ->
                editWindowEnd = time
                updateEditWindowButtons()
            }
        }
        binding.btnLockUntil.setOnClickListener { showLockUntilPicker() }

        binding.cgUsagePeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            usageLimitPeriod = if (checkedIds.firstOrNull() == R.id.chipHourly) "HOURLY" else "DAILY"
            syncUsageLimitPickers()
        }
        binding.cgScheduleBreakUsagePeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            usageLimitPeriod = if (checkedIds.firstOrNull() == R.id.chipScheduleBreakHourly) "HOURLY" else "DAILY"
            syncUsageLimitPickers()
        }

        binding.cgTimerBreakPresets.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingTimerBreakPresets || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val minutes = when (checkedIds.first()) {
                R.id.chipBreak5 -> 5
                R.id.chipBreak10 -> 10
                R.id.chipBreak15 -> 15
                R.id.chipBreak30 -> 30
                R.id.chipBreak1h -> 60
                R.id.chipBreak2h -> 120
                R.id.chipBreak4h -> 240
                R.id.chipBreak8h -> 480
                R.id.chipBreak1d -> 1440
                R.id.chipBreak1w -> 10080
                R.id.chipBreakCustom -> -1
                else -> 0
            }
            if (minutes == -1) {
                showCustomBreakDurationPicker()
            } else if (minutes > 0) {
                timerBreakMinutes = minutes
                lockUntil = System.currentTimeMillis() + minutes * 60_000L
                updateLockUntilDisplay()
            }
        }

        binding.weeklyGrid.setOnBlockInteractionListener(object : WeeklyScheduleGridView.OnBlockInteractionListener {
            override fun onBlockSelected(timeBlock: TimeBlock) {
                showSelectedBlockDetail(timeBlock)
            }

            override fun onBlockDeselected() {
                hideSelectedBlockDetail()
            }
        })

        binding.btnAddTime.setOnClickListener { showAddTimeBlockDialog() }

        binding.btnSelectApps.setOnClickListener {
            val intent = Intent(this, AppPickerActivity::class.java).apply {
                putExtra(AppPickerActivity.EXTRA_PRESELECTED, selectedPackages)
                putExtra(AppPickerActivity.EXTRA_IS_ALLOWLIST, isAllowlistMode)
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, AppPickerActivity.REQ_CODE)
        }

        binding.btnShowQr.setOnClickListener {
            QrDisplayFragment.newInstance(currentQrSecret)
                .show(supportFragmentManager, "qr_display")
        }
        binding.btnScanQrToSet.setOnClickListener {
            qrScanForSetLauncher.launch(Intent(this, com.qrzen.app.ui.lock.QrScanActivity::class.java))
        }

        binding.btnSave.setOnClickListener { saveBlock() }
    }

    private fun setupBlockingStyleDropdown() {
        refreshBlockingStyleDropdown()
        binding.actBlockingStyle.setOnItemClickListener { _, _, position, _ ->
            val newStyle = blockingStyles[position].first
            if (newStyle == UnlockMethodUtils.STYLE_POMODORO && unlockMethod == UNLOCK_TIMER) {
                unlockMethod = UNLOCK_NONE
                refreshUnlockMethodDropdown()
                updateUnlockMethodUi()
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    getString(R.string.pomodoro_timer_incompatible),
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            blockingStyle = newStyle
            refreshUnlockMethodDropdown()
            updateBlockingStyleUi()
        }
    }

    private fun refreshBlockingStyleDropdown() {
        normalizeBlockingStyle()
        binding.actBlockingStyle.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, blockingStyles.map { it.second })
        )
    }

    private fun setupUnlockMethodDropdown() {
        refreshUnlockMethodDropdown()
        binding.actUnlockMethod.setOnItemClickListener { _, _, position, _ ->
            unlockMethod = unlockMethods[position].first
            updateUnlockMethodUi()
        }
    }

    private fun setupScheduleBreakDropdown() {
        refreshScheduleBreakDropdown()
        binding.actScheduleBreakType.setOnItemClickListener { _, _, position, _ ->
            scheduleBreakType = scheduleBreakTypes[position].first
            updateScheduleBreakUi()
        }
    }

    private fun refreshUnlockMethodDropdown() {
        if (blockingStyle == UnlockMethodUtils.STYLE_POMODORO && unlockMethod == UNLOCK_TIMER) {
            unlockMethod = UNLOCK_NONE
        }
        binding.actUnlockMethod.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, unlockMethods.map { it.second })
        )
        binding.actUnlockMethod.setText(getUnlockMethodLabel(unlockMethod), false)
    }

    private fun refreshScheduleBreakDropdown() {
        normalizeScheduleBreakType()
        binding.actScheduleBreakType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, scheduleBreakTypes.map { it.second })
        )
        binding.actScheduleBreakType.setText(getScheduleBreakTypeLabel(scheduleBreakType), false)
    }

    private fun populateForm(block: AppBlock) {
        isAllowlistMode = block.isAllowlistMode
        supportActionBar?.title = if (isAllowlistMode) "Edit Allowlist Block" else "Edit Block"

        selectedPackages = block.appPackages
        blockingStyle = block.blockingStyle.ifBlank { UnlockMethodUtils.STYLE_MANUAL }
        unlockMethod = block.unlockMethod.ifBlank { UNLOCK_NONE }
        delayMinutes = block.delayMinutes.coerceIn(1, 60)
        blockPassword = block.blockPassword
        typeOverText = block.typeOverText
        typeOverIsRandom = block.typeOverIsRandom
        editWindowStart = block.editWindowStart.ifBlank { "09:00" }
        editWindowEnd = block.editWindowEnd.ifBlank { "10:00" }
        editWindowDays = block.editWindowDays.ifBlank { "1111111" }
        activeDays = block.activeDays.ifBlank { "1111111" }
        usageLimitPeriod = block.usageLimitPeriod.ifBlank { "DAILY" }
        usageLimitMinutes = block.usageLimitMinutes.coerceIn(1, usageLimitMaxForPeriod(usageLimitPeriod))
        waitTimerWaitMinutes = block.waitTimerWaitMinutes.coerceIn(1, 120)
        waitTimerUseMinutes = block.waitTimerUseMinutes.coerceIn(1, 120)
        waitTimerAdaptive = block.waitTimerAdaptive
        pomodoroDurationMin = block.pomodoroDurationMin.coerceIn(1, 120)
        pomodoroBreakMin = block.pomodoroBreakMin.coerceIn(1, 60)
        scheduleBreakType = block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }
        scheduledAllowanceMinutes = block.scheduledAllowanceMinutes.coerceIn(1, 120)
        showTimer = block.showTimer
        timerBreakMinutes = block.timerBreakMinutes
        lockUntil = block.lockUntil
        pomodoroLockEditing = block.pomodoroLockEditing

        binding.etTitle.setText(block.title)
        binding.npDelayMinutes.value = delayMinutes
        binding.etBlockPassword.setText(blockPassword)
        binding.etConfirmPassword.setText(blockPassword)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        binding.etTypeOverText.setText(typeOverText)

        setToggleStates(editWindowDayToggles(), editWindowDays)
        setToggleStates(usageLimitDayToggles(), activeDays)
        setToggleStates(waitTimerDayToggles(), activeDays)

        binding.cbPomodoroLockEditing.isChecked = pomodoroLockEditing

        applyCurrentStateToUi()

        lifecycleScope.launch {
            currentTimeBlocks = timeBlockDao.getByBlockId(block.id).toMutableList()
            nextTempId = (currentTimeBlocks.minOfOrNull { it.id } ?: 0) - 1
            binding.weeklyGrid.setTimeBlocks(currentTimeBlocks)
        }
    }

    private fun applyCurrentStateToUi() {
        refreshBlockingStyleDropdown()
        binding.btnSelectApps.text = if (isAllowlistMode) {
            getString(R.string.edit_block_select_allowed_apps)
        } else {
            getString(R.string.edit_block_select_apps)
        }
        binding.tvQrSecret.text = currentQrSecret
        binding.actBlockingStyle.setText(getBlockingStyleLabel(blockingStyle), false)
        refreshScheduleBreakDropdown()
        refreshUnlockMethodDropdown()
        binding.npDelayMinutes.value = delayMinutes.coerceIn(1, 60)
        syncUsageLimitPickers()
        syncWaitTimerUi()
        syncPomodoroConfigUi()
        syncScheduledAllowanceUi()
        binding.cbShowTimer.isChecked = showTimer
        binding.cbPomodoroLockEditing.isChecked = pomodoroLockEditing
        binding.etTypeOverText.setText(typeOverText)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        setToggleStates(usageLimitDayToggles(), activeDays)
        setToggleStates(waitTimerDayToggles(), activeDays)
        updateSelectedAppsDisplay()
        updateEditWindowButtons()
        updateLockUntilDisplay()
        updateTypeOverUi()
        updateBlockingStyleUi()
        updateScheduleBreakUi()
        updateUnlockMethodUi()
        syncTimerBreakPresetSelection()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppPickerActivity.REQ_CODE && resultCode == RESULT_OK) {
            selectedPackages = data?.getStringExtra(AppPickerActivity.EXTRA_RESULT) ?: ""
            updateSelectedAppsDisplay()
        }
    }

    private fun updateSelectedAppsDisplay() {
        val packages = getSelectedPackageList()
        binding.tvSelectedApps.text = if (isAllowlistMode) {
            if (packages.isEmpty()) "No allowed apps selected" else "${packages.size} app(s) allowed"
        } else {
            if (packages.isEmpty()) "No apps selected" else "${packages.size} app(s) selected"
        }

        selectedAppsLoadJob?.cancel()
        binding.rvSelectedApps.adapter = null
        if (packages.isEmpty()) {
            binding.rvSelectedApps.visibility = View.GONE
            return
        }

        binding.rvSelectedApps.visibility = View.VISIBLE
        selectedAppsLoadJob = lifecycleScope.launch {
            val icons = withContext(Dispatchers.IO) {
                packages.mapNotNull { pkg ->
                    try {
                        val appInfo = packageManager.getApplicationInfo(pkg, 0)
                        SelectedAppIcon(
                            packageName = pkg,
                            label = packageManager.getApplicationLabel(appInfo).toString(),
                            icon = packageManager.getApplicationIcon(appInfo),
                            timerExpiry = if (isAllowlistMode) {
                                val blockId = existingBlock?.id
                                if (blockId != null) {
                                    Prefs.getAppTimerExpiry(blockId, pkg)
                                } else {
                                    pendingAppTimers[pkg]?.let { System.currentTimeMillis() + it } ?: 0L
                                }
                            } else 0L
                        )
                    } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
                        null
                    }
                }
            }
            if (packages != getSelectedPackageList()) return@launch
            if (icons.isEmpty()) {
                binding.rvSelectedApps.visibility = View.GONE
                binding.rvSelectedApps.adapter = null
            } else {
                binding.rvSelectedApps.visibility = View.VISIBLE
                binding.rvSelectedApps.adapter = SelectedAppsAdapter(icons)
            }
        }
    }

    private fun getSelectedPackageList(): List<String> = selectedPackages.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    data class SelectedAppIcon(
        val packageName: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable,
        val timerExpiry: Long = 0L
    )

    private inner class SelectedAppsAdapter(
        private val apps: List<SelectedAppIcon>
    ) : RecyclerView.Adapter<SelectedAppsAdapter.ViewHolder>() {

        inner class ViewHolder(
            val binding: ItemEditAppGridBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: SelectedAppIcon) {
                binding.ivAppIcon.setImageDrawable(item.icon)
                binding.tvAppLabel.text = item.label

                val now = System.currentTimeMillis()
                if (item.timerExpiry > now) {
                    binding.tvTimerBadge.visibility = View.VISIBLE
                    val remaining = item.timerExpiry - now
                    binding.tvTimerBadge.text = formatTimerBadge(remaining)
                } else if (item.timerExpiry > 0L && item.timerExpiry <= now) {
                    binding.tvTimerBadge.visibility = View.VISIBLE
                    binding.tvTimerBadge.text = "00:00"
                } else {
                    binding.tvTimerBadge.visibility = View.GONE
                }

                if (isAllowlistMode) {
                    binding.root.setOnClickListener {
                        showAppTimerDialog(item)
                    }
                } else {
                    binding.root.setOnClickListener(null)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemEditAppGridBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(apps[position])
        override fun getItemCount(): Int = apps.size
    }

    private fun showAppTimerDialog(appItem: SelectedAppIcon) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_app_timer, null)
        val npHours = dialogView.findViewById<NumberPicker>(R.id.npHours)
        val npMinutes = dialogView.findViewById<NumberPicker>(R.id.npMinutes)

        npHours.minValue = 0
        npHours.maxValue = 23
        npMinutes.minValue = 0
        npMinutes.maxValue = 59

        val now = System.currentTimeMillis()
        if (appItem.timerExpiry > now) {
            val remainingMinutes = ((appItem.timerExpiry - now) / 60_000L).toInt()
            npHours.value = remainingMinutes / 60
            npMinutes.value = remainingMinutes % 60
        } else {
            npHours.value = 1
            npMinutes.value = 0
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.timer_dialog_title, appItem.label))
            .setView(dialogView)
            .setPositiveButton(R.string.timer_set) { _, _ ->
                val totalMinutes = npHours.value * 60L + npMinutes.value
                if (totalMinutes > 0) {
                    val blockId = existingBlock?.id
                    if (blockId != null) {
                        val expiry = System.currentTimeMillis() + totalMinutes * 60_000L
                        Prefs.setAppTimerExpiry(blockId, appItem.packageName, expiry)
                    } else {
                        pendingAppTimers[appItem.packageName] = totalMinutes * 60_000L
                    }
                }
                updateSelectedAppsDisplay()
            }
            .setNeutralButton(R.string.timer_clear) { _, _ ->
                val blockId = existingBlock?.id
                if (blockId != null) {
                    Prefs.setAppTimerExpiry(blockId, appItem.packageName, 0L)
                } else {
                    pendingAppTimers.remove(appItem.packageName)
                }
                updateSelectedAppsDisplay()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun formatTimerBadge(millis: Long): String {
        val totalMinutes = (millis / 60_000L).coerceAtLeast(0L)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) String.format("%d:%02d", hours, minutes)
        else String.format("%02d:00", minutes)
    }

    private fun updateEditWindowButtons() {
        binding.btnEditWindowStart.text = "${getString(R.string.unlock_edit_window_start)}: $editWindowStart"
        binding.btnEditWindowEnd.text = "${getString(R.string.unlock_edit_window_end)}: $editWindowEnd"
    }

    private fun syncUsageLimitPickers() {
        usageLimitPeriod = if (usageLimitPeriod == "HOURLY") "HOURLY" else "DAILY"
        val maxValue = usageLimitMaxForPeriod(usageLimitPeriod)
        usageLimitMinutes = usageLimitMinutes.coerceIn(1, maxValue)
        setNumberPickerValue(binding.npUsageLimitMinutes, usageLimitMinutes, maxValue)
        setNumberPickerValue(binding.npScheduleBreakUsageLimitMinutes, usageLimitMinutes, maxValue)
        val standaloneChipId = if (usageLimitPeriod == "HOURLY") R.id.chipHourly else R.id.chipDaily
        if (binding.cgUsagePeriod.checkedChipId != standaloneChipId) {
            binding.cgUsagePeriod.check(standaloneChipId)
        }
        val scheduleChipId = if (usageLimitPeriod == "HOURLY") R.id.chipScheduleBreakHourly else R.id.chipScheduleBreakDaily
        if (binding.cgScheduleBreakUsagePeriod.checkedChipId != scheduleChipId) {
            binding.cgScheduleBreakUsagePeriod.check(scheduleChipId)
        }
    }

    private fun syncWaitTimerUi() {
        waitTimerUseMinutes = waitTimerUseMinutes.coerceIn(1, 120)
        waitTimerWaitMinutes = waitTimerWaitMinutes.coerceIn(1, 120)
        setNumberPickerValue(binding.npWaitTimerUse, waitTimerUseMinutes, 120)
        setNumberPickerValue(binding.npWaitTimerWait, waitTimerWaitMinutes, 120)
        setNumberPickerValue(binding.npScheduleBreakWaitUse, waitTimerUseMinutes, 120)
        setNumberPickerValue(binding.npScheduleBreakWaitBlock, waitTimerWaitMinutes, 120)
        if (binding.cbWaitTimerAdaptive.isChecked != waitTimerAdaptive) {
            binding.cbWaitTimerAdaptive.isChecked = waitTimerAdaptive
        }
        if (binding.cbScheduleBreakWaitAdaptive.isChecked != waitTimerAdaptive) {
            binding.cbScheduleBreakWaitAdaptive.isChecked = waitTimerAdaptive
        }
    }

    private fun syncPomodoroConfigUi() {
        pomodoroDurationMin = pomodoroDurationMin.coerceIn(1, 120)
        pomodoroBreakMin = pomodoroBreakMin.coerceIn(1, 60)
        setNumberPickerValue(binding.npPomodoroDuration, pomodoroDurationMin, 120)
        setNumberPickerValue(binding.npPomodoroBreak, pomodoroBreakMin, 60)
        setNumberPickerValue(binding.npScheduleBreakPomodoroDuration, pomodoroDurationMin, 120)
        setNumberPickerValue(binding.npScheduleBreakPomodoroBreak, pomodoroBreakMin, 60)
    }

    private fun syncScheduledAllowanceUi() {
        scheduledAllowanceMinutes = scheduledAllowanceMinutes.coerceIn(1, 120)
        setNumberPickerValue(binding.npScheduledAllowanceMinutes, scheduledAllowanceMinutes, 120)
    }

    private fun setNumberPickerValue(picker: NumberPicker, value: Int, maxValue: Int) {
        if (picker.maxValue != maxValue) {
            picker.maxValue = maxValue
        }
        val clampedValue = value.coerceIn(picker.minValue, picker.maxValue)
        if (picker.value != clampedValue) {
            picker.value = clampedValue
        }
    }

    private fun usageLimitMaxForPeriod(period: String): Int = if (period == "HOURLY") 60 else 480

    private fun updateBlockingStyleUi() {
        binding.tvBlockingStyleDesc.text = when (blockingStyle) {
            UnlockMethodUtils.STYLE_SCHEDULE -> getString(R.string.blocking_style_schedule_desc)
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> getString(R.string.blocking_style_usage_limit_desc)
            UnlockMethodUtils.STYLE_WAIT_TIMER -> getString(R.string.blocking_style_wait_timer_desc)
            UnlockMethodUtils.STYLE_POMODORO -> getString(R.string.blocking_style_pomodoro_desc)
            else -> getString(R.string.blocking_style_manual_desc)
        }
        binding.llScheduleSection.visibility = if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE) View.VISIBLE else View.GONE
        binding.llUsageLimitSection.visibility = if (blockingStyle == UnlockMethodUtils.STYLE_USAGE_LIMIT) View.VISIBLE else View.GONE
        binding.llWaitTimerSection.visibility = if (blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER) View.VISIBLE else View.GONE
        binding.llPomodoroSection.visibility = if (blockingStyle == UnlockMethodUtils.STYLE_POMODORO) View.VISIBLE else View.GONE
        binding.cbShowTimer.visibility = if (
            blockingStyle == UnlockMethodUtils.STYLE_USAGE_LIMIT ||
            blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER
        ) View.VISIBLE else View.GONE
        if (blockingStyle != UnlockMethodUtils.STYLE_SCHEDULE) {
            hideSelectedBlockDetail()
        }
        updateScheduleBreakUi()
    }

    private fun updateScheduleBreakUi() {
        refreshScheduleBreakDropdown()
        binding.tvScheduleBreakDesc.visibility = View.GONE
        binding.llScheduleBreakPomodoroSection.visibility =
            if (scheduleBreakType == UnlockMethodUtils.BREAK_POMODORO) View.VISIBLE else View.GONE
        binding.llScheduleBreakWaitTimerSection.visibility =
            if (scheduleBreakType == UnlockMethodUtils.BREAK_WAIT_TIMER) View.VISIBLE else View.GONE
        binding.llScheduleBreakUsageLimitSection.visibility =
            if (scheduleBreakType == UnlockMethodUtils.BREAK_USAGE_LIMIT) View.VISIBLE else View.GONE
        binding.llScheduleBreakAllowanceSection.visibility =
            if (scheduleBreakType == UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE) View.VISIBLE else View.GONE
    }

    private fun updateUnlockMethodUi() {
        binding.llUnlockNone.visibility = if (unlockMethod == UNLOCK_NONE) View.VISIBLE else View.GONE
        binding.llUnlockDelay.visibility = if (unlockMethod == UNLOCK_DELAY) View.VISIBLE else View.GONE
        binding.llUnlockPassword.visibility = if (unlockMethod == UNLOCK_PASSWORD) View.VISIBLE else View.GONE
        binding.llUnlockTypeOver.visibility = if (unlockMethod == UNLOCK_TYPE_OVER_TEXT) View.VISIBLE else View.GONE
        binding.llUnlockQr.visibility = if (unlockMethod == UNLOCK_QR_CODE) View.VISIBLE else View.GONE
        binding.llUnlockEditWindow.visibility = if (unlockMethod == UNLOCK_EDIT_WINDOW) View.VISIBLE else View.GONE
        binding.llUnlockTimer.visibility = if (unlockMethod == UNLOCK_TIMER) View.VISIBLE else View.GONE
        binding.llUnlockWhileActive.visibility = if (unlockMethod == UNLOCK_WHILE_ACTIVE) View.VISIBLE else View.GONE
    }

    private fun updateTypeOverUi() {
        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
        binding.tvTypeOverRandomInfo.visibility = if (typeOverIsRandom) View.VISIBLE else View.GONE
        binding.tilTypeOverText.visibility = if (typeOverIsRandom) View.GONE else View.VISIBLE
    }

    private fun updateLockUntilDisplay() {
        binding.tvLockUntilValue.text = if (lockUntil > 0L) {
            formatDateTime(lockUntil)
        } else {
            "${getString(R.string.unlock_timer_lock_until)}: —"
        }
    }

    private fun showAddTimeBlockDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_time_block, null)
        val toggles = listOf(
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockMon),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockTue),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockWed),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockThu),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockFri),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockSat),
            dialogView.findViewById<ToggleButton>(R.id.toggleBlockSun)
        )
        val btnStart = dialogView.findViewById<MaterialButton>(R.id.btnAddTimeBlockStart)
        val btnEnd = dialogView.findViewById<MaterialButton>(R.id.btnAddTimeBlockEnd)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelAddTimeBlock)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmAddTimeBlock)

        var dialogStartTime = "09:00"
        var dialogEndTime = "10:00"
        btnStart.text = dialogStartTime
        btnEnd.text = dialogEndTime

        btnStart.setOnClickListener {
            showTimePicker(getString(R.string.add_time_block_from), dialogStartTime) { time ->
                dialogStartTime = time
                btnStart.text = time
            }
        }
        btnEnd.setOnClickListener {
            showTimePicker(getString(R.string.add_time_block_until), dialogEndTime) { time ->
                dialogEndTime = time
                btnEnd.text = time
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val activeDays = buildDaysString(toggles)
            if ('1' !in activeDays) {
                Toast.makeText(this, getString(R.string.add_time_block_days_label), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hasTimeBlockOverlap(dialogStartTime, dialogEndTime, activeDays)) {
                Toast.makeText(this, getString(R.string.add_time_block_overlap_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newBlock = TimeBlock(
                id = nextTempId--,
                blockId = existingBlock?.id ?: 0,
                startTime = dialogStartTime,
                endTime = dialogEndTime,
                activeDays = activeDays
            )
            currentTimeBlocks.add(newBlock)
            binding.weeklyGrid.setTimeBlocks(currentTimeBlocks)
            binding.weeklyGrid.setSelectedBlockId(newBlock.id)
            showSelectedBlockDetail(newBlock)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSelectedBlockDetail(timeBlock: TimeBlock) {
        val selectedBlock = currentTimeBlocks.firstOrNull { it.id == timeBlock.id } ?: timeBlock
        selectedTimeBlockId = selectedBlock.id
        selectedBlockDetailView.visibility = View.VISIBLE
        selectedBlockDetailView.bringToFront()
        selectedBlockStartButton.text = selectedBlock.startTime
        selectedBlockEndButton.text = selectedBlock.endTime
        selectedBlockStartButton.isEnabled = true
        selectedBlockEndButton.isEnabled = true
        highlightSelectedDays(selectedBlock.activeDays)
    }

    private fun hideSelectedBlockDetail() {
        selectedTimeBlockId = null
        selectedBlockDetailView.visibility = View.GONE
    }

    private fun getSelectedTimeBlock(): TimeBlock? {
        val blockId = selectedTimeBlockId ?: return null
        return currentTimeBlocks.firstOrNull { it.id == blockId }
    }

    private fun editSelectedTimeBlock(isStart: Boolean) {
        val selectedBlock = getSelectedTimeBlock() ?: return
        val titleRes = if (isStart) R.string.add_time_block_from else R.string.add_time_block_until
        val currentValue = if (isStart) selectedBlock.startTime else selectedBlock.endTime
        showTimePicker(getString(titleRes), currentValue) { time ->
            val updated = if (isStart) {
                selectedBlock.copy(startTime = time)
            } else {
                selectedBlock.copy(endTime = time)
            }
            if (hasTimeBlockOverlap(updated.startTime, updated.endTime, updated.activeDays, updated.id)) {
                Toast.makeText(this, getString(R.string.add_time_block_overlap_error), Toast.LENGTH_SHORT).show()
            } else {
                replaceTimeBlock(updated)
                showSelectedBlockDetail(updated)
            }
        }
    }

    private fun deleteSelectedTimeBlock() {
        val selectedBlock = getSelectedTimeBlock() ?: return
        currentTimeBlocks.removeAll { it.id == selectedBlock.id }
        binding.weeklyGrid.setTimeBlocks(currentTimeBlocks)
        binding.weeklyGrid.setSelectedBlockId(null)
        hideSelectedBlockDetail()
    }

    private fun highlightSelectedDays(activeDays: String) {
        val normalized = activeDays.padEnd(7, '0')
        val order = listOf(6, 0, 1, 2, 3, 4, 5)
        selectedDayViews.forEachIndexed { index, textView ->
            styleSelectedDay(textView, normalized.getOrNull(order[index]) == '1')
        }
    }

    private fun styleSelectedDay(textView: TextView, isActive: Boolean) {
        val backgroundColor = if (isActive) {
            MaterialColors.getColor(textView, com.google.android.material.R.attr.colorPrimaryContainer)
        } else {
            MaterialColors.getColor(textView, com.google.android.material.R.attr.colorSurfaceVariant)
        }
        val textColor = if (isActive) {
            MaterialColors.getColor(textView, com.google.android.material.R.attr.colorOnPrimaryContainer)
        } else {
            MaterialColors.getColor(textView, com.google.android.material.R.attr.colorOnSurfaceVariant)
        }
        textView.background = GradientDrawable().apply {
            cornerRadius = resources.displayMetrics.density * 12f
            setColor(backgroundColor)
        }
        textView.setTextColor(textColor)
    }

    private fun replaceTimeBlock(updated: TimeBlock) {
        currentTimeBlocks = currentTimeBlocks.map {
            if (it.id == updated.id) updated else it
        }.toMutableList()
        binding.weeklyGrid.setTimeBlocks(currentTimeBlocks)
        binding.weeklyGrid.setSelectedBlockId(updated.id)
    }

    private fun hasTimeBlockOverlap(
        startTime: String,
        endTime: String,
        activeDays: String,
        ignoreBlockId: Int? = null
    ): Boolean {
        val targetDays = activeDays.padEnd(7, '0')
        val newSegments = buildSegments(startTime, endTime)
        return currentTimeBlocks.any { existing ->
            if (ignoreBlockId != null && existing.id == ignoreBlockId) return@any false
            val existingDays = existing.activeDays.padEnd(7, '0')
            (0 until 7).any { dayIndex ->
                targetDays.getOrNull(dayIndex) == '1' && existingDays.getOrNull(dayIndex) == '1' &&
                    rangesOverlap(newSegments, buildSegments(existing.startTime, existing.endTime))
            }
        }
    }

    private fun buildSegments(startTime: String, endTime: String): List<Pair<Int, Int>> {
        val startMinutes = parseMinutes(startTime)
        val endMinutes = parseMinutes(endTime)
        return if (endMinutes <= startMinutes) {
            listOf(startMinutes to 1440, 0 to endMinutes)
        } else {
            listOf(startMinutes to endMinutes)
        }
    }

    private fun rangesOverlap(first: List<Pair<Int, Int>>, second: List<Pair<Int, Int>>): Boolean {
        return first.any { (firstStart, firstEnd) ->
            second.any { (secondStart, secondEnd) ->
                firstStart < secondEnd && secondStart < firstEnd
            }
        }
    }

    private fun showCustomBreakDurationPicker() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_app_timer, null)
        val npHours = dialogView.findViewById<NumberPicker>(R.id.npHours)
        val npMinutes = dialogView.findViewById<NumberPicker>(R.id.npMinutes)

        npHours.minValue = 0
        npHours.maxValue = 23
        npMinutes.minValue = 0
        npMinutes.maxValue = 59
        npHours.value = timerBreakMinutes / 60
        npMinutes.value = timerBreakMinutes % 60

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.timer_break_duration_label))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val totalMinutes = npHours.value * 60 + npMinutes.value
                if (totalMinutes <= 0) {
                    Toast.makeText(this, getString(R.string.timer_break_custom), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                timerBreakMinutes = totalMinutes
                lockUntil = System.currentTimeMillis() + totalMinutes * 60_000L
                updateLockUntilDisplay()
                syncTimerBreakPresetSelection()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun syncTimerBreakPresetSelection() {
        val chipId = when (timerBreakMinutes) {
            5 -> R.id.chipBreak5
            10 -> R.id.chipBreak10
            15 -> R.id.chipBreak15
            30 -> R.id.chipBreak30
            60 -> R.id.chipBreak1h
            120 -> R.id.chipBreak2h
            240 -> R.id.chipBreak4h
            480 -> R.id.chipBreak8h
            1440 -> R.id.chipBreak1d
            10080 -> R.id.chipBreak1w
            0 -> View.NO_ID
            else -> R.id.chipBreakCustom
        }
        isUpdatingTimerBreakPresets = true
        if (chipId == View.NO_ID) {
            binding.cgTimerBreakPresets.clearCheck()
        } else {
            binding.cgTimerBreakPresets.check(chipId)
        }
        isUpdatingTimerBreakPresets = false
    }

    private fun clearTimerBreakPresetSelection() {
        isUpdatingTimerBreakPresets = true
        binding.cgTimerBreakPresets.clearCheck()
        isUpdatingTimerBreakPresets = false
    }

    private fun saveBlock() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            binding.tilTitle.error = "Block name is required"
            return
        }
        binding.tilTitle.error = null
        binding.tilBlockPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilTypeOverText.error = null

        delayMinutes = binding.npDelayMinutes.value
        if (blockingStyle == UnlockMethodUtils.STYLE_USAGE_LIMIT) {
            usageLimitMinutes = binding.npUsageLimitMinutes.value
            usageLimitPeriod = if (binding.cgUsagePeriod.checkedChipId == R.id.chipHourly) "HOURLY" else "DAILY"
        } else if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE && scheduleBreakType == UnlockMethodUtils.BREAK_USAGE_LIMIT) {
            usageLimitMinutes = binding.npScheduleBreakUsageLimitMinutes.value
            usageLimitPeriod = if (binding.cgScheduleBreakUsagePeriod.checkedChipId == R.id.chipScheduleBreakHourly) "HOURLY" else "DAILY"
        }
        syncUsageLimitPickers()

        if (blockingStyle == UnlockMethodUtils.STYLE_WAIT_TIMER) {
            waitTimerWaitMinutes = binding.npWaitTimerWait.value
            waitTimerUseMinutes = binding.npWaitTimerUse.value
            waitTimerAdaptive = binding.cbWaitTimerAdaptive.isChecked
        } else if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE && scheduleBreakType == UnlockMethodUtils.BREAK_WAIT_TIMER) {
            waitTimerWaitMinutes = binding.npScheduleBreakWaitBlock.value
            waitTimerUseMinutes = binding.npScheduleBreakWaitUse.value
            waitTimerAdaptive = binding.cbScheduleBreakWaitAdaptive.isChecked
        }
        syncWaitTimerUi()

        if (blockingStyle == UnlockMethodUtils.STYLE_POMODORO) {
            pomodoroDurationMin = binding.npPomodoroDuration.value
            pomodoroBreakMin = binding.npPomodoroBreak.value
        } else if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE && scheduleBreakType == UnlockMethodUtils.BREAK_POMODORO) {
            pomodoroDurationMin = binding.npScheduleBreakPomodoroDuration.value
            pomodoroBreakMin = binding.npScheduleBreakPomodoroBreak.value
        }
        syncPomodoroConfigUi()

        if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE && scheduleBreakType == UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE) {
            scheduledAllowanceMinutes = binding.npScheduledAllowanceMinutes.value
        }
        syncScheduledAllowanceUi()
        showTimer = binding.cbShowTimer.isChecked
        blockPassword = binding.etBlockPassword.text?.toString() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
        typeOverText = binding.etTypeOverText.text?.toString()?.trim() ?: ""
        editWindowDays = buildDaysString(editWindowDayToggles())
        val activeDays = when (blockingStyle) {
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> buildDaysString(usageLimitDayToggles())
            UnlockMethodUtils.STYLE_WAIT_TIMER -> buildDaysString(waitTimerDayToggles())
            else -> "1111111"
        }

        when (unlockMethod) {
            UNLOCK_PASSWORD -> {
                if (blockPassword.isEmpty()) {
                    binding.tilBlockPassword.error = "Password is required"
                    return
                }
                if (blockPassword != confirmPassword) {
                    binding.tilConfirmPassword.error = "Passwords do not match"
                    return
                }
            }
            UNLOCK_TYPE_OVER_TEXT -> {
                if (!typeOverIsRandom && typeOverText.isEmpty()) {
                    binding.tilTypeOverText.error = "Challenge text is required"
                    return
                }
            }
            UNLOCK_TIMER -> {
                if (lockUntil <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Lock until must be in the future", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE && currentTimeBlocks.isEmpty()) {
            Toast.makeText(this, getString(R.string.schedule_add_time), Toast.LENGTH_SHORT).show()
            return
        }

        val block = AppBlock(
            id = existingBlock?.id ?: 0,
            title = title,
            appPackages = selectedPackages,
            isAllowlistMode = isAllowlistMode,
            startTime = "00:00",
            endTime = "23:59",
            activeDays = activeDays,
            qrSecret = currentQrSecret,
            unlockMethod = unlockMethod,
            delayMinutes = delayMinutes,
            blockPassword = blockPassword,
            typeOverText = typeOverText,
            typeOverIsRandom = typeOverIsRandom,
            editWindowStart = editWindowStart,
            editWindowEnd = editWindowEnd,
            editWindowDays = editWindowDays,
            lockUntil = lockUntil,
            masterPasswordEnabled = existingBlock?.masterPasswordEnabled ?: false,
            pausedUntil = existingBlock?.pausedUntil ?: 0L,
            blockNowUntil = existingBlock?.blockNowUntil ?: 0L,
            isEnabled = existingBlock?.isEnabled ?: (!isAllowlistMode),
            isPomodoroBlock = blockingStyle == UnlockMethodUtils.STYLE_POMODORO,
            pomodoroDurationMin = pomodoroDurationMin,
            pomodoroBreakMin = pomodoroBreakMin,
            isArchived = existingBlock?.isArchived ?: false,
            blockingStyle = blockingStyle,
            scheduleBreakType = scheduleBreakType,
            scheduledAllowanceMinutes = scheduledAllowanceMinutes,
            usageLimitMinutes = usageLimitMinutes,
            usageLimitPeriod = usageLimitPeriod,
            waitTimerWaitMinutes = waitTimerWaitMinutes,
            waitTimerUseMinutes = waitTimerUseMinutes,
            waitTimerAdaptive = waitTimerAdaptive,
            timerBreakMinutes = timerBreakMinutes,
            showTimer = showTimer,
            toggleLockUntil = existingBlock?.toggleLockUntil ?: 0L,
            autoDisableOnToggleLockExpiry = existingBlock?.autoDisableOnToggleLockExpiry ?: false,
            activeUntil = existingBlock?.activeUntil ?: 0L,
            pomodoroRoundsTotal = existingBlock?.pomodoroRoundsTotal ?: 0,
            pomodoroSessionStartMillis = existingBlock?.pomodoroSessionStartMillis ?: 0L,
            pomodoroLockEditing = pomodoroLockEditing
        )

        lifecycleScope.launch {
            val savedId = if (existingBlock == null) {
                dao.insert(block).toInt()
            } else {
                dao.update(block)
                block.id
            }

            if (existingBlock == null && pendingAppTimers.isNotEmpty()) {
                for ((pkg, remainingMs) in pendingAppTimers) {
                    Prefs.setAppTimerRemaining(savedId, pkg, remainingMs)
                    Prefs.setAppTimerLastFg(savedId, pkg, 0L)
                }
                pendingAppTimers.clear()
            }

            if (blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE) {
                timeBlockDao.deleteByBlockId(savedId)
                val blocksToSave = currentTimeBlocks.map { it.copy(id = 0, blockId = savedId) }
                timeBlockDao.insertAll(blocksToSave)
            }

            val savedBlock = block.copy(id = savedId)
            if (savedBlock.isEnabled && savedBlock.pausedUntil <= System.currentTimeMillis() && isBlockCurrentlyActive(savedBlock)) {
                startActivity(Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            finish()
        }
    }

    private fun isBlockCurrentlyActive(block: AppBlock): Boolean {
        return UnlockMethodUtils.isBlockCurrentlyActive(block, currentTimeBlocks)
    }

    private fun isTimeBlockCurrentlyActive(timeBlock: TimeBlock): Boolean {
        val dayIndex = currentDayIndex()
        if (timeBlock.activeDays.padEnd(7, '0').getOrNull(dayIndex) != '1') return false
        val now = LocalTime.now()
        val start = parseLocalTime(timeBlock.startTime)
        val end = parseLocalTime(timeBlock.endTime)
        return if (end > start) {
            !now.isBefore(start) && !now.isAfter(end)
        } else {
            !now.isBefore(start) || !now.isAfter(end)
        }
    }

    private fun showTimePicker(title: String, current: String, onPicked: (String) -> Unit) {
        val parts = current.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val picker = MaterialTimePicker.Builder()
            .setTitleText(title)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
        picker.addOnPositiveButtonClickListener {
            onPicked(String.format(Locale.US, "%02d:%02d", picker.hour, picker.minute))
        }
        picker.show(supportFragmentManager, "time_picker_${title.replace(" ", "_")}")
    }

    private fun showLockUntilPicker() {
        val now = System.currentTimeMillis()
        val initialSelection = if (lockUntil > now) lockUntil else now
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.unlock_timer_lock_until))
            .setSelection(toUtcDateSelection(initialSelection))
            .build()
        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            showLockUntilTimePicker(selectedDate)
        }
        datePicker.show(supportFragmentManager, "lock_until_date_picker")
    }

    private fun showLockUntilTimePicker(selectedDateUtcMillis: Long) {
        val currentLock = Calendar.getInstance().apply {
            if (lockUntil > System.currentTimeMillis()) {
                timeInMillis = lockUntil
            }
        }
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = selectedDateUtcMillis
        }
        val picker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.unlock_timer_pick_date))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentLock.get(Calendar.HOUR_OF_DAY))
            .setMinute(currentLock.get(Calendar.MINUTE))
            .build()
        picker.addOnPositiveButtonClickListener {
            val localCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, picker.hour)
                set(Calendar.MINUTE, picker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            timerBreakMinutes = 0
            clearTimerBreakPresetSelection()
            lockUntil = localCalendar.timeInMillis
            updateLockUntilDisplay()
        }
        picker.show(supportFragmentManager, "lock_until_time_picker")
    }

    private fun buildDaysString(toggles: List<ToggleButton>): String = buildString {
        toggles.forEach { append(if (it.isChecked) '1' else '0') }
    }

    private fun setToggleStates(toggles: List<ToggleButton>, days: String) {
        val paddedDays = days.padEnd(7, '0')
        toggles.forEachIndexed { index, toggle ->
            toggle.isChecked = paddedDays.getOrNull(index) == '1'
        }
    }

    private fun editWindowDayToggles(): List<ToggleButton> = listOf(
        binding.toggleEditMon,
        binding.toggleEditTue,
        binding.toggleEditWed,
        binding.toggleEditThu,
        binding.toggleEditFri,
        binding.toggleEditSat,
        binding.toggleEditSun
    )

    private fun usageLimitDayToggles(): List<ToggleButton> = listOf(
        binding.toggleUsageMon,
        binding.toggleUsageTue,
        binding.toggleUsageWed,
        binding.toggleUsageThu,
        binding.toggleUsageFri,
        binding.toggleUsageSat,
        binding.toggleUsageSun
    )

    private fun waitTimerDayToggles(): List<ToggleButton> = listOf(
        binding.toggleWaitMon,
        binding.toggleWaitTue,
        binding.toggleWaitWed,
        binding.toggleWaitThu,
        binding.toggleWaitFri,
        binding.toggleWaitSat,
        binding.toggleWaitSun
    )

    private fun normalizeBlockingStyle() {
        if (blockingStyles.none { it.first == blockingStyle }) {
            blockingStyle = UnlockMethodUtils.STYLE_MANUAL
        }
    }

    private fun normalizeScheduleBreakType() {
        if (scheduleBreakTypes.none { it.first == scheduleBreakType }) {
            scheduleBreakType = UnlockMethodUtils.BREAK_NONE
        }
    }

    private fun getBlockingStyleLabel(style: String): String {
        return blockingStyles.firstOrNull { it.first == style }?.second
            ?: getString(R.string.blocking_style_manual)
    }

    private fun getUnlockMethodLabel(method: String): String {
        return unlockMethods.firstOrNull { it.first == method }?.second
            ?: getString(R.string.unlock_method_none)
    }

    private fun getScheduleBreakTypeLabel(type: String): String {
        return scheduleBreakTypes.firstOrNull { it.first == type }?.second
            ?: getString(R.string.schedule_break_type_none_option)
    }

    private fun formatDateTime(epochMillis: Long): String {
        return SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault()).format(epochMillis)
    }

    private fun toUtcDateSelection(epochMillis: Long): Long {
        val localCalendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun currentDayIndex(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY.ordinal
            Calendar.TUESDAY -> DayOfWeek.TUESDAY.ordinal
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY.ordinal
            Calendar.THURSDAY -> DayOfWeek.THURSDAY.ordinal
            Calendar.FRIDAY -> DayOfWeek.FRIDAY.ordinal
            Calendar.SATURDAY -> DayOfWeek.SATURDAY.ordinal
            Calendar.SUNDAY -> DayOfWeek.SUNDAY.ordinal
            else -> DayOfWeek.MONDAY.ordinal
        }
    }

    private fun parseMinutes(value: String): Int {
        val time = parseLocalTime(value)
        return time.hour * 60 + time.minute
    }

    private fun parseLocalTime(value: String): LocalTime {
        return runCatching { LocalTime.parse(value, timeFormatter) }
            .getOrElse {
                val parts = value.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
                val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
                LocalTime.of(hour, minute)
            }
    }
}
