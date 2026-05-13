package com.qrzen.app.ui.folder

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.qrzen.app.R
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.databinding.ActivityEditFolderBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditFolderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FOLDER_ID = "extra_folder_id"
    }

    @Inject lateinit var blockFolderDao: BlockFolderDao

    private lateinit var binding: ActivityEditFolderBinding

    private var existingFolder: BlockFolder? = null
    private var currentQrSecret: String = ""
    private var unlockMethod: String = UnlockMethodUtils.METHOD_NONE
    private var delayMinutes: Int = 5
    private var blockPassword: String = ""
    private var typeOverText: String = ""
    private var typeOverIsRandom: Boolean = false
    private var editWindowStart: String = "09:00"
    private var editWindowEnd: String = "10:00"
    private var editWindowDays: String = "1111111"
    private var lockUntil: Long = 0L

    private val unlockMethods = listOf(
        UnlockMethodUtils.METHOD_NONE to R.string.unlock_method_none,
        UnlockMethodUtils.METHOD_DELAY to R.string.unlock_method_delay,
        UnlockMethodUtils.METHOD_PASSWORD to R.string.unlock_method_password,
        UnlockMethodUtils.METHOD_TYPE_OVER_TEXT to R.string.unlock_method_type_over,
        UnlockMethodUtils.METHOD_QR_CODE to R.string.unlock_method_qr_code,
        UnlockMethodUtils.METHOD_EDIT_WINDOW to R.string.unlock_method_edit_window,
        UnlockMethodUtils.METHOD_TIMER to R.string.unlock_method_timer
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.menu_edit_folder)
        binding.toolbar.setOnMenuItemClickListener(::onToolbarMenuItemSelected)

        setupUi()

        val folderId = intent.getIntExtra(EXTRA_FOLDER_ID, -1)
        if (folderId == -1) {
            currentQrSecret = UUID.randomUUID().toString()
            supportActionBar?.title = getString(R.string.edit_folder_new_title)
            applyCurrentStateToUi()
        } else {
            supportActionBar?.title = getString(R.string.edit_folder_edit_title)
            lifecycleScope.launch {
                val folder = blockFolderDao.getById(folderId) ?: run {
                    finish()
                    return@launch
                }
                existingFolder = folder
                populateForm(folder)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onToolbarMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_folder -> {
                saveFolder()
                true
            }
            else -> false
        }
    }

    private fun setupUi() {
        binding.actUnlockMethod.keyListener = null

        binding.npDelayMinutes.minValue = 1
        binding.npDelayMinutes.maxValue = 60
        binding.npDelayMinutes.value = delayMinutes
        binding.npDelayMinutes.setOnValueChangedListener { _, _, newVal ->
            delayMinutes = newVal
        }

        setupUnlockMethodDropdown()

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
    }

    private fun setupUnlockMethodDropdown() {
        refreshUnlockMethodDropdown()
        binding.actUnlockMethod.setOnItemClickListener { _, _, position, _ ->
            unlockMethod = unlockMethods[position].first
            updateUnlockMethodUi()
        }
    }

    private fun refreshUnlockMethodDropdown() {
        binding.actUnlockMethod.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, unlockMethods.map { getString(it.second) })
        )
        binding.actUnlockMethod.setText(getUnlockMethodLabel(unlockMethod), false)
    }

    private fun populateForm(folder: BlockFolder) {
        currentQrSecret = folder.qrSecret.ifBlank { UUID.randomUUID().toString() }
        unlockMethod = folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }
        delayMinutes = folder.delayMinutes.coerceIn(1, 60)
        blockPassword = folder.blockPassword
        typeOverText = folder.typeOverText
        typeOverIsRandom = folder.typeOverIsRandom
        editWindowStart = folder.editWindowStart.ifBlank { "09:00" }
        editWindowEnd = folder.editWindowEnd.ifBlank { "10:00" }
        editWindowDays = folder.editWindowDays.ifBlank { "1111111" }
        lockUntil = folder.lockUntil

        binding.etTitle.setText(folder.title)
        binding.npDelayMinutes.value = delayMinutes
        binding.etBlockPassword.setText(blockPassword)
        binding.etConfirmPassword.setText(blockPassword)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        binding.etTypeOverText.setText(typeOverText)
        setToggleStates(editWindowDayToggles(), editWindowDays)
        applyCurrentStateToUi()
    }

    private fun applyCurrentStateToUi() {
        refreshUnlockMethodDropdown()
        binding.tvQrSecret.text = currentQrSecret
        binding.npDelayMinutes.value = delayMinutes.coerceIn(1, 60)
        binding.etTypeOverText.setText(typeOverText)
        binding.switchTypeOverRandom.isChecked = typeOverIsRandom
        setToggleStates(editWindowDayToggles(), editWindowDays)
        updateEditWindowButtons()
        updateLockUntilDisplay()
        updateTypeOverUi()
        updateUnlockMethodUi()
    }

    private fun updateUnlockMethodUi() {
        binding.llUnlockNone.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_NONE) View.VISIBLE else View.GONE
        binding.llUnlockDelay.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_DELAY) View.VISIBLE else View.GONE
        binding.llUnlockPassword.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_PASSWORD) View.VISIBLE else View.GONE
        binding.llUnlockTypeOver.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_TYPE_OVER_TEXT) View.VISIBLE else View.GONE
        binding.llUnlockQr.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_QR_CODE) View.VISIBLE else View.GONE
        binding.llUnlockEditWindow.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_EDIT_WINDOW) View.VISIBLE else View.GONE
        binding.llUnlockTimer.visibility = if (unlockMethod == UnlockMethodUtils.METHOD_TIMER) View.VISIBLE else View.GONE
    }

    private fun updateTypeOverUi() {
        typeOverIsRandom = binding.switchTypeOverRandom.isChecked
        binding.tvTypeOverRandomInfo.visibility = if (typeOverIsRandom) View.VISIBLE else View.GONE
        binding.tilTypeOverText.visibility = if (typeOverIsRandom) View.GONE else View.VISIBLE
    }

    private fun updateEditWindowButtons() {
        binding.btnEditWindowStart.text = "${getString(R.string.unlock_edit_window_start)}: $editWindowStart"
        binding.btnEditWindowEnd.text = "${getString(R.string.unlock_edit_window_end)}: $editWindowEnd"
    }

    private fun updateLockUntilDisplay() {
        binding.tvLockUntilValue.text = if (lockUntil > 0L) {
            formatDateTime(lockUntil)
        } else {
            "${getString(R.string.unlock_timer_lock_until)}: —"
        }
    }

    private fun saveFolder() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.edit_folder_title_required)
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
            UnlockMethodUtils.METHOD_PASSWORD -> {
                if (blockPassword.isEmpty()) {
                    binding.tilBlockPassword.error = getString(R.string.edit_folder_password_required)
                    return
                }
                if (blockPassword != confirmPassword) {
                    binding.tilConfirmPassword.error = getString(R.string.edit_folder_passwords_mismatch)
                    return
                }
            }
            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> {
                if (!typeOverIsRandom && typeOverText.isEmpty()) {
                    binding.tilTypeOverText.error = getString(R.string.edit_folder_type_over_required)
                    return
                }
            }
            UnlockMethodUtils.METHOD_TIMER -> {
                if (lockUntil <= System.currentTimeMillis()) {
                    Toast.makeText(this, R.string.edit_folder_lock_until_future, Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        if (currentQrSecret.isBlank()) {
            currentQrSecret = UUID.randomUUID().toString()
        }

        val folder = BlockFolder(
            id = existingFolder?.id ?: 0,
            title = title,
            isEnabled = existingFolder?.isEnabled ?: true,
            pausedUntil = existingFolder?.pausedUntil ?: 0L,
            isCollapsed = existingFolder?.isCollapsed ?: false,
            sortOrder = existingFolder?.sortOrder ?: 0,
            unlockMethod = unlockMethod,
            delayMinutes = delayMinutes,
            blockPassword = blockPassword,
            typeOverText = typeOverText,
            typeOverIsRandom = typeOverIsRandom,
            editWindowStart = editWindowStart,
            editWindowEnd = editWindowEnd,
            editWindowDays = editWindowDays,
            lockUntil = lockUntil,
            qrSecret = currentQrSecret,
            masterPasswordEnabled = existingFolder?.masterPasswordEnabled ?: false
        )

        lifecycleScope.launch {
            if (existingFolder == null) {
                blockFolderDao.insert(folder)
            } else {
                blockFolderDao.update(folder)
            }
            finish()
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
        picker.show(supportFragmentManager, "folder_time_picker_${title.replace(" ", "_")}")
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
        datePicker.show(supportFragmentManager, "folder_lock_until_date_picker")
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
        picker.show(supportFragmentManager, "folder_lock_until_time_picker")
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

    private fun getUnlockMethodLabel(method: String): String {
        return unlockMethods.firstOrNull { it.first == method }?.second?.let(::getString)
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
