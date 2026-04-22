package com.qrzen.app.ui.block

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityEditBlockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditBlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_IS_ALLOWLIST = "extra_is_allowlist"
    }

    @Inject lateinit var dao: AppBlockDao
    private lateinit var binding: ActivityEditBlockBinding

    private var existingBlock: AppBlock? = null
    private var currentQrSecret: String = ""
    private var selectedPackages: String = ""
    private var isAllowlistMode: Boolean = false
    private var startTime: String = "00:00"
    private var endTime: String = "23:59"

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
            isAllowlistMode = intent.getBooleanExtra(EXTRA_IS_ALLOWLIST, false)
            currentQrSecret = UUID.randomUUID().toString()
            supportActionBar?.title = if (isAllowlistMode) "New Allowlist Block" else "New Block"
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
        isAllowlistMode = block.isAllowlistMode
        supportActionBar?.title = if (isAllowlistMode) "Edit Allowlist Block" else "Edit Block"
        binding.etTitle.setText(block.title)
        selectedPackages = block.appPackages
        updateSelectedAppsDisplay()

        startTime = block.startTime
        endTime = block.endTime

        binding.btnStartTime.text = "Start: $startTime"
        binding.btnEndTime.text = "End: $endTime"

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

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnSelectApps.text = if (isAllowlistMode) {
            getString(R.string.edit_block_select_allowed_apps)
        } else {
            getString(R.string.edit_block_select_apps)
        }
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
        binding.tvSelectedApps.text = if (isAllowlistMode) {
            if (pkgs.isEmpty()) "No allowed apps selected" else "${pkgs.size} app(s) allowed"
        } else {
            if (pkgs.isEmpty()) "No apps selected" else "${pkgs.size} app(s) selected"
        }
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
            isAllowlistMode = isAllowlistMode,
            startTime = startTime,
            endTime = endTime,
            activeDays = activeDays,
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
