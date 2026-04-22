package com.qrzen.app.ui.block

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.ActivityEditBlockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    }

    @Inject lateinit var dao: AppBlockDao
    private lateinit var binding: ActivityEditBlockBinding

    private var existingBlock: AppBlock? = null
    private var currentQrSecret: String = ""
    private var selectedPackages: String = ""
    private var isAllowlistMode: Boolean = false
    private var startTime: String = "00:00"
    private var endTime: String = "23:59"
    private var unlockMethod: String = UNLOCK_NONE
    private var delayMinutes: Int = 5
    private var blockPassword: String = ""
    private var typeOverText: String = ""
    private var typeOverIsRandom: Boolean = true
    private var editWindowStart: String = "09:00"
    private var editWindowEnd: String = "10:00"
    private var editWindowDays: String = "1111111"
    private var lockUntil: Long = 0L

    private val unlockMethods by lazy {
        listOf(
            UNLOCK_NONE to getString(R.string.unlock_method_none),
            UNLOCK_DELAY to getString(R.string.unlock_method_delay),
            UNLOCK_PASSWORD to getString(R.string.unlock_method_password),
            UNLOCK_TYPE_OVER_TEXT to getString(R.string.unlock_method_type_over),
            UNLOCK_QR_CODE to getString(R.string.unlock_method_qr_code),
            UNLOCK_EDIT_WINDOW to getString(R.string.unlock_method_edit_window),
            UNLOCK_TIMER to getString(R.string.unlock_method_timer)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupUi()
        applyCurrentStateToUi()

        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
        if (blockId == -1) {
            isAllowlistMode = intent.getBooleanExtra(EXTRA_IS_ALLOWLIST, false)
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
        setupUnlockMethodDropdown()

        binding.npDelayMinutes.minValue = 1
        binding.npDelayMinutes.maxValue = 60
        binding.npDelayMinutes.value = delayMinutes

        binding.npPomodoroDuration.minValue = 1
        binding.npPomodoroDuration.maxValue = 120
        binding.npPomodoroDuration.value = 25

        binding.npPomodoroBreak.minValue = 1
        binding.npPomodoroBreak.maxValue = 60
        binding.npPomodoroBreak.value = 5

        binding.cbPomodoro.setOnCheckedChangeListener { _, checked ->
            binding.llPomodoro.visibility = if (checked) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.switchTypeOverRandom.setOnCheckedChangeListener { _, isChecked ->
            typeOverIsRandom = isChecked
            updateTypeOverUi()
        }

        binding.btnStartTime.setOnClickListener {
            showTimePicker("Start time", startTime) { time ->
                startTime = time
                updateScheduleButtons()
            }
        }
        binding.btnEndTime.setOnClickListener {
            showTimePicker("End time", endTime) { time ->
                endTime = time
                updateScheduleButtons()
            }
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

        binding.btnSave.setOnClickListener { saveBlock() }
    }

    private fun setupUnlockMethodDropdown() {
        val labels = unlockMethods.map { it.second }
        binding.actUnlockMethod.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        )
        binding.actUnlockMethod.keyListener = null
        binding.actUnlockMethod.setOnItemClickListener { _, _, position, _ ->
            unlockMethod = unlockMethods[position].first
            updateUnlockMethodUi()
        }
    }

    private fun populateForm(block: AppBlock) {
        isAllowlistMode = block.isAllowlistMode
        supportActionBar?.title = if (isAllowlistMode) "Edit Allowlist Block" else "Edit Block"

        selectedPackages = block.appPackages
        startTime = block.startTime
        endTime = block.endTime
        unlockMethod = block.unlockMethod.ifBlank { UNLOCK_NONE }
        delayMinutes = block.delayMinutes.coerceIn(1, 60)
        blockPassword = block.blockPassword
        typeOverText = block.typeOverText
        typeOverIsRandom = block.typeOverIsRandom
        editWindowStart = block.editWindowStart.ifBlank { "09:00" }
        editWindowEnd = block.editWindowEnd.ifBlank { "10:00" }
        editWindowDays = block.editWindowDays.ifBlank { "1111111" }
        lockUntil = block.lockUntil

        binding.etTitle.setText(block.title)
        binding.npDelayMinutes.value = delayMinutes
        binding.etBlockPassword.setText(blockPassword)
        binding.etConfirmPassword.setText(blockPassword)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        binding.etTypeOverText.setText(typeOverText)

        setToggleStates(scheduleDayToggles(), block.activeDays)
        setToggleStates(editWindowDayToggles(), editWindowDays)

        binding.cbPomodoro.isChecked = block.isPomodoroBlock
        if (block.isPomodoroBlock) {
            binding.npPomodoroDuration.value = block.pomodoroDurationMin.coerceIn(1, 120)
            binding.npPomodoroBreak.value = block.pomodoroBreakMin.coerceIn(1, 60)
        }

        applyCurrentStateToUi()
    }

    private fun applyCurrentStateToUi() {
        binding.btnSelectApps.text = if (isAllowlistMode) {
            getString(R.string.edit_block_select_allowed_apps)
        } else {
            getString(R.string.edit_block_select_apps)
        }
        binding.tvQrSecret.text = currentQrSecret
        binding.actUnlockMethod.setText(getUnlockMethodLabel(unlockMethod), false)
        binding.npDelayMinutes.value = delayMinutes.coerceIn(1, 60)
        binding.etTypeOverText.setText(typeOverText)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        updateSelectedAppsDisplay()
        updateScheduleButtons()
        updateEditWindowButtons()
        updateLockUntilDisplay()
        updateTypeOverUi()
        updateUnlockMethodUi()
        binding.llPomodoro.visibility = if (binding.cbPomodoro.isChecked) android.view.View.VISIBLE else android.view.View.GONE
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
        val packages = selectedPackages.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        binding.tvSelectedApps.text = if (isAllowlistMode) {
            if (packages.isEmpty()) "No allowed apps selected" else "${packages.size} app(s) allowed"
        } else {
            if (packages.isEmpty()) "No apps selected" else "${packages.size} app(s) selected"
        }
    }

    private fun updateScheduleButtons() {
        binding.btnStartTime.text = "Start: $startTime"
        binding.btnEndTime.text = "End: $endTime"
    }

    private fun updateEditWindowButtons() {
        binding.btnEditWindowStart.text = "${getString(R.string.unlock_edit_window_start)}: $editWindowStart"
        binding.btnEditWindowEnd.text = "${getString(R.string.unlock_edit_window_end)}: $editWindowEnd"
    }

    private fun updateUnlockMethodUi() {
        binding.llUnlockNone.visibility = if (unlockMethod == UNLOCK_NONE) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockDelay.visibility = if (unlockMethod == UNLOCK_DELAY) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockPassword.visibility = if (unlockMethod == UNLOCK_PASSWORD) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockTypeOver.visibility = if (unlockMethod == UNLOCK_TYPE_OVER_TEXT) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockQr.visibility = if (unlockMethod == UNLOCK_QR_CODE) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockEditWindow.visibility = if (unlockMethod == UNLOCK_EDIT_WINDOW) android.view.View.VISIBLE else android.view.View.GONE
        binding.llUnlockTimer.visibility = if (unlockMethod == UNLOCK_TIMER) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun updateTypeOverUi() {
        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
        binding.tvTypeOverRandomInfo.visibility = if (typeOverIsRandom) android.view.View.VISIBLE else android.view.View.GONE
        binding.tilTypeOverText.visibility = if (typeOverIsRandom) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun updateLockUntilDisplay() {
        binding.tvLockUntilValue.text = if (lockUntil > 0L) {
            formatDateTime(lockUntil)
        } else {
            "${getString(R.string.unlock_timer_lock_until)}: —"
        }
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
        blockPassword = binding.etBlockPassword.text?.toString() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
        typeOverText = binding.etTypeOverText.text?.toString()?.trim() ?: ""
        editWindowDays = buildDaysString(editWindowDayToggles())

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

        val activeDays = buildDaysString(scheduleDayToggles())
        val isPomodoroBlock = binding.cbPomodoro.isChecked
        val block = AppBlock(
            id = existingBlock?.id ?: 0,
            title = title,
            appPackages = selectedPackages,
            isAllowlistMode = isAllowlistMode,
            startTime = startTime,
            endTime = endTime,
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
            isEnabled = existingBlock?.isEnabled ?: true,
            isPomodoroBlock = isPomodoroBlock,
            pomodoroDurationMin = if (isPomodoroBlock) binding.npPomodoroDuration.value else 25,
            pomodoroBreakMin = if (isPomodoroBlock) binding.npPomodoroBreak.value else 5,
            isArchived = existingBlock?.isArchived ?: false
        )

        lifecycleScope.launch {
            if (existingBlock == null) {
                dao.insert(block)
            } else {
                dao.update(block)
            }
            if (block.isEnabled && block.pausedUntil <= System.currentTimeMillis() && isBlockCurrentlyActive(block)) {
                startActivity(Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            finish()
        }
    }

    private fun isBlockCurrentlyActive(block: AppBlock): Boolean {
        val now = LocalTime.now()
        val start = LocalTime.parse(block.startTime, DateTimeFormatter.ofPattern("HH:mm"))
        val end = LocalTime.parse(block.endTime, DateTimeFormatter.ofPattern("HH:mm"))
        val timeOk = if (end.isAfter(start)) !now.isBefore(start) && !now.isAfter(end)
        else !now.isBefore(start) || !now.isAfter(end)
        if (!timeOk) return false
        val cal = Calendar.getInstance()
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return block.activeDays.getOrNull(dayIndex) == '1'
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

    private fun scheduleDayToggles(): List<ToggleButton> = listOf(
        binding.toggleMon,
        binding.toggleTue,
        binding.toggleWed,
        binding.toggleThu,
        binding.toggleFri,
        binding.toggleSat,
        binding.toggleSun
    )

    private fun editWindowDayToggles(): List<ToggleButton> = listOf(
        binding.toggleEditMon,
        binding.toggleEditTue,
        binding.toggleEditWed,
        binding.toggleEditThu,
        binding.toggleEditFri,
        binding.toggleEditSat,
        binding.toggleEditSun
    )

    private fun getUnlockMethodLabel(method: String): String {
        return unlockMethods.firstOrNull { it.first == method }?.second
            ?: getString(R.string.unlock_method_none)
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
}
