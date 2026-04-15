package com.qrzen.app.ui.block

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityEditBlockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditBlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
    }

    @Inject lateinit var dao: AppBlockDao
    private lateinit var binding: ActivityEditBlockBinding

    private var existingBlock: AppBlock? = null
    private var currentQrSecret: String = ""
    private var selectedPackages: String = ""
    private var startTime: String = "00:00"
    private var endTime: String = "23:59"
    private var editStartTime: String = "06:00"
    private var editEndTime: String = "07:00"

    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (Prefs.masterPasswordEnabled) {
            binding.cbMasterPassword.visibility = View.VISIBLE
        }

        binding.npPomodoroDuration.minValue = 1
        binding.npPomodoroDuration.maxValue = 120
        binding.npPomodoroDuration.value = 25

        binding.npPomodoroBreak.minValue = 1
        binding.npPomodoroBreak.maxValue = 60
        binding.npPomodoroBreak.value = 5

        binding.cbPomodoro.setOnCheckedChangeListener { _, checked ->
            binding.llPomodoro.visibility = if (checked) View.VISIBLE else View.GONE
        }

        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
        if (blockId == -1) {
            currentQrSecret = UUID.randomUUID().toString()
            supportActionBar?.title = "New Block"
            binding.tvQrSecret.text = currentQrSecret
            setupButtons()
        } else {
            supportActionBar?.title = "Edit Block"
            lifecycleScope.launch {
                val block = dao.getById(blockId) ?: run { finish(); return@launch }
                existingBlock = block
                currentQrSecret = block.qrSecret
                runOnUiThread { populateForm(block) }
            }
        }
    }

    private fun populateForm(block: AppBlock) {
        binding.etTitle.setText(block.title)
        selectedPackages = block.appPackages
        updateSelectedAppsDisplay()

        startTime = block.startTime
        endTime = block.endTime
        editStartTime = block.editStartTime
        editEndTime = block.editEndTime

        binding.btnStartTime.text = "Start: $startTime"
        binding.btnEndTime.text = "End: $endTime"
        binding.btnEditStart.text = "Edit from: $editStartTime"
        binding.btnEditEnd.text = "Edit until: $editEndTime"

        val days = block.activeDays.padEnd(7, '0')
        binding.toggleMon.isChecked = days[0] == '1'
        binding.toggleTue.isChecked = days[1] == '1'
        binding.toggleWed.isChecked = days[2] == '1'
        binding.toggleThu.isChecked = days[3] == '1'
        binding.toggleFri.isChecked = days[4] == '1'
        binding.toggleSat.isChecked = days[5] == '1'
        binding.toggleSun.isChecked = days[6] == '1'

        binding.tvQrSecret.text = block.qrSecret

        if (Prefs.masterPasswordEnabled) {
            binding.cbMasterPassword.isChecked = block.masterPasswordEnabled
        }

        binding.cbPomodoro.isChecked = block.isPomodoroBlock
        if (block.isPomodoroBlock) {
            binding.llPomodoro.visibility = View.VISIBLE
            binding.npPomodoroDuration.value = block.pomodoroDurationMin.coerceIn(1, 120)
            binding.npPomodoroBreak.value = block.pomodoroBreakMin.coerceIn(1, 60)
        }

        enforceEditWindow(block)
        setupButtons()
    }

    private fun enforceEditWindow(block: AppBlock) {
        val now = LocalTime.now()
        val editStart = LocalTime.parse(block.editStartTime, fmt)
        val editEnd = LocalTime.parse(block.editEndTime, fmt)
        val inEditWindow = if (editEnd.isAfter(editStart)) {
            now.isAfter(editStart) && now.isBefore(editEnd)
        } else {
            now.isAfter(editStart) || now.isBefore(editEnd)
        }
        if (!inEditWindow) {
            setFormEnabled(false)
            Snackbar.make(
                binding.root,
                getString(R.string.edit_window_locked, block.editStartTime, block.editEndTime),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etTitle.isEnabled = enabled
        binding.btnSelectApps.isEnabled = enabled
        binding.btnStartTime.isEnabled = enabled
        binding.btnEndTime.isEnabled = enabled
        binding.btnEditStart.isEnabled = enabled
        binding.btnEditEnd.isEnabled = enabled
        binding.toggleMon.isEnabled = enabled
        binding.toggleTue.isEnabled = enabled
        binding.toggleWed.isEnabled = enabled
        binding.toggleThu.isEnabled = enabled
        binding.toggleFri.isEnabled = enabled
        binding.toggleSat.isEnabled = enabled
        binding.toggleSun.isEnabled = enabled
        binding.cbMasterPassword.isEnabled = enabled
        binding.cbPomodoro.isEnabled = enabled
        binding.npPomodoroDuration.isEnabled = enabled
        binding.npPomodoroBreak.isEnabled = enabled
        binding.btnSave.isEnabled = enabled
    }

    private fun setupButtons() {
        binding.btnStartTime.setOnClickListener {
            showTimePicker("Start time", startTime) { t ->
                startTime = t
                binding.btnStartTime.text = "Start: $t"
            }
        }
        binding.btnEndTime.setOnClickListener {
            showTimePicker("End time", endTime) { t ->
                endTime = t
                binding.btnEndTime.text = "End: $t"
            }
        }
        binding.btnEditStart.setOnClickListener {
            showTimePicker("Edit window start", editStartTime) { t ->
                editStartTime = t
                binding.btnEditStart.text = "Edit from: $t"
            }
        }
        binding.btnEditEnd.setOnClickListener {
            showTimePicker("Edit window end", editEndTime) { t ->
                editEndTime = t
                binding.btnEditEnd.text = "Edit until: $t"
            }
        }

        binding.btnSelectApps.setOnClickListener {
            val intent = Intent(this, AppPickerActivity::class.java).apply {
                putExtra(AppPickerActivity.EXTRA_PRESELECTED, selectedPackages)
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
        val pkgs = selectedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        binding.tvSelectedApps.text = if (pkgs.isEmpty()) "No apps selected"
            else "${pkgs.size} app(s) selected"
    }

    private fun saveBlock() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            binding.tilTitle.error = "Block name is required"
            return
        }
        binding.tilTitle.error = null

        val activeDays = buildString {
            append(if (binding.toggleMon.isChecked) '1' else '0')
            append(if (binding.toggleTue.isChecked) '1' else '0')
            append(if (binding.toggleWed.isChecked) '1' else '0')
            append(if (binding.toggleThu.isChecked) '1' else '0')
            append(if (binding.toggleFri.isChecked) '1' else '0')
            append(if (binding.toggleSat.isChecked) '1' else '0')
            append(if (binding.toggleSun.isChecked) '1' else '0')
        }

        val isPomodoroBlock = binding.cbPomodoro.isChecked
        val block = AppBlock(
            id = existingBlock?.id ?: 0,
            title = title,
            appPackages = selectedPackages,
            startTime = startTime,
            endTime = endTime,
            activeDays = activeDays,
            editStartTime = editStartTime,
            editEndTime = editEndTime,
            qrSecret = currentQrSecret,
            masterPasswordEnabled = binding.cbMasterPassword.isChecked,
            pausedUntil = existingBlock?.pausedUntil ?: 0L,
            isEnabled = existingBlock?.isEnabled ?: true,
            isPomodoroBlock = isPomodoroBlock,
            pomodoroDurationMin = if (isPomodoroBlock) binding.npPomodoroDuration.value else 25,
            pomodoroBreakMin = if (isPomodoroBlock) binding.npPomodoroBreak.value else 5
        )

        lifecycleScope.launch {
            if (existingBlock == null) dao.insert(block) else dao.update(block)
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
            onPicked(String.format("%02d:%02d", picker.hour, picker.minute))
        }
        picker.show(supportFragmentManager, "time_picker")
    }
}
