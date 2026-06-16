package com.qrzen.app.ui.main

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.qrzen.app.R
import com.qrzen.app.data.backup.BlockBackup
import com.qrzen.app.data.backup.BlockBackupManager
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.FragmentSettingsBinding
import com.qrzen.app.service.DiagnosticNotifier
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.util.BruteForceGuard
import com.qrzen.app.util.PasswordHasher
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var blockFolderDao: BlockFolderDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var isUpdatingOverrideModeSelection = false
    private var pendingOverrideMode: String? = null
    private val masterPasswordGuard = BruteForceGuard()
    private val pauseAllOptions = listOf(
        PauseAllOption("30 minutes", 30 * 60_000L),
        PauseAllOption("1 hour", 60 * 60_000L),
        PauseAllOption("2 hours", 2 * 60 * 60_000L),
        PauseAllOption("4 hours", 4 * 60 * 60_000L),
        PauseAllOption("8 hours", 8 * 60 * 60_000L),
        PauseAllOption("12 hours", 12 * 60 * 60_000L),
        PauseAllOption("24 hours", 24 * 60 * 60_000L)
    )
    private val backupManager by lazy { BlockBackupManager(dao, blockFolderDao, timeBlockDao) }
    private var pendingExportRequest: PendingExportRequest? = null
    private val exportBlocksLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val exportRequest = pendingExportRequest ?: return@registerForActivityResult
        pendingExportRequest = null
        val uri = result.data?.data ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(exportRequest.json.toByteArray())
                    } ?: error("Unable to open destination file")
                }
            }.onSuccess {
                Snackbar.make(
                    binding.root,
                    getString(
                        R.string.settings_export_success,
                        exportRequest.blocksExported,
                        exportRequest.foldersExported
                    ),
                    Snackbar.LENGTH_LONG
                ).show()
            }.onFailure { error ->
                Snackbar.make(
                    binding.root,
                    getString(R.string.settings_export_error, error.message ?: error.javaClass.simpleName),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    private val importBlocksLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Unable to open backup file")
                }
            }.onSuccess(::showImportConfirmation).onFailure { error ->
                Snackbar.make(
                    binding.root,
                    getString(R.string.settings_import_error, error.message ?: error.javaClass.simpleName),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etMasterPwd.setText("")
        binding.swRemoveNotif.isChecked = Prefs.removeNotifications
        binding.swSilentMode.isChecked = Prefs.silentMode
        setSelectedOverrideMode(Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_NONE })
        updateMasterPasswordViews()
        updatePauseAllViews()

        binding.rgOverrideMode.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingOverrideModeSelection) return@setOnCheckedChangeListener
            val selectedMode = modeForCheckedId(checkedId) ?: return@setOnCheckedChangeListener
            val currentMode = Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_NONE }
            val hasMasterPassword = Prefs.masterPassword.isNotEmpty()

            updateOverrideModeDescription(selectedMode)

            if (selectedMode == currentMode && pendingOverrideMode == null) {
                return@setOnCheckedChangeListener
            }

            if (currentMode == Prefs.OVERRIDE_STRICT && selectedMode != Prefs.OVERRIDE_STRICT) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val canExitStrictMode = dao.getAll().all { !it.isEnabled } &&
                        blockFolderDao.getAll().all { !it.isEnabled }
                    if (!canExitStrictMode) {
                        setSelectedOverrideMode(Prefs.OVERRIDE_STRICT)
                        Snackbar.make(binding.root, getString(R.string.strict_cannot_disable), Snackbar.LENGTH_SHORT).show()
                        return@launch
                    }
                    handleOverrideModeSelection(currentMode, selectedMode, hasMasterPassword)
                }
                return@setOnCheckedChangeListener
            }

            handleOverrideModeSelection(currentMode, selectedMode, hasMasterPassword)
        }

        binding.btnSavePwd.setOnClickListener {
            val newPassword = binding.etMasterPwd.text?.toString()?.trim().orEmpty()
            val currentPassword = binding.etOldPwd.text?.toString().orEmpty()
            val existingPassword = Prefs.masterPassword
            if (newPassword.isEmpty()) {
                binding.tilMasterPwd.error = "Password cannot be empty"
                return@setOnClickListener
            }
            if (existingPassword.isNotEmpty() && !PasswordHasher.verify(currentPassword, existingPassword)) {
                binding.tilOldPwd.error = getString(R.string.settings_old_password_wrong)
                return@setOnClickListener
            }
            binding.tilOldPwd.error = null
            binding.tilMasterPwd.error = null
            Prefs.masterPassword = PasswordHasher.hash(newPassword)
            val targetMode = pendingOverrideMode ?: Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_MASTER_PASSWORD }
            Prefs.masterPasswordOverrideMode = if (targetMode == Prefs.OVERRIDE_NONE) {
                Prefs.OVERRIDE_MASTER_PASSWORD
            } else {
                targetMode
            }
            pendingOverrideMode = null
            setSelectedOverrideMode(Prefs.masterPasswordOverrideMode)
            Snackbar.make(binding.root, "Master password saved", Snackbar.LENGTH_SHORT).show()
            binding.etOldPwd.text?.clear()
            binding.etMasterPwd.text?.clear()
            updateMasterPasswordViews()
            updatePauseAllViews()
        }

        binding.btnPauseAll.setOnClickListener {
            if (Prefs.masterPassword.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.settings_master_pwd_required), Snackbar.LENGTH_SHORT).show()
            } else {
                promptForMasterPassword(
                    titleRes = R.string.settings_pause_all,
                    onVerified = { showPauseAllDurationDialog() }
                )
            }
        }

        binding.btnResumeAll.setOnClickListener { resumeAllBlocks() }
        binding.btnExportBlocks.setOnClickListener { exportBlocks() }
        binding.btnImportBlocks.setOnClickListener { importBlocks() }

        binding.swRemoveNotif.setOnCheckedChangeListener { _, checked ->
            Prefs.removeNotifications = checked
        }

        binding.swDiagnosticNotif.isChecked = Prefs.diagnosticNotifications
        binding.swDiagnosticNotif.setOnCheckedChangeListener { _, checked ->
            Prefs.diagnosticNotifications = checked
            if (checked) {
                DiagnosticNotifier.ensureChannel(requireContext())
            } else {
                DiagnosticNotifier.cancelPollState(requireContext())
            }
        }

        binding.swSilentMode.setOnCheckedChangeListener { _, isChecked ->
            Prefs.silentMode = isChecked
            if (isChecked) {
                val nm = requireContext().getSystemService(NotificationManager::class.java)
                if (!nm.isNotificationPolicyAccessGranted) {
                    Snackbar.make(
                        binding.root,
                        "Grant Do Not Disturb access for silent mode",
                        Snackbar.LENGTH_LONG
                    ).setAction("Grant") {
                        startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            if (pendingOverrideMode == null) {
                setSelectedOverrideMode(Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_NONE })
            } else {
                updateOverrideModeDescription(pendingOverrideMode!!)
            }
            updateMasterPasswordViews()
            updatePauseAllViews()
        }
    }

    private fun updateMasterPasswordViews() {
        val currentMode = pendingOverrideMode ?: Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_NONE }
        val hasMasterPassword = Prefs.masterPassword.isNotEmpty()
        binding.tilOldPwd.visibility = if (currentMode != Prefs.OVERRIDE_NONE && hasMasterPassword) View.VISIBLE else View.GONE
        binding.tilMasterPwd.visibility = if (currentMode != Prefs.OVERRIDE_NONE) View.VISIBLE else View.GONE
        binding.btnSavePwd.visibility = if (currentMode != Prefs.OVERRIDE_NONE) View.VISIBLE else View.GONE
        if (currentMode == Prefs.OVERRIDE_NONE || !hasMasterPassword) {
            binding.tilOldPwd.error = null
        }
        if (currentMode == Prefs.OVERRIDE_NONE) {
            binding.tilMasterPwd.error = null
        }
    }

    private fun updatePauseAllViews() {
        val mode = Prefs.masterPasswordOverrideMode.ifEmpty { Prefs.OVERRIDE_NONE }
        val showPauseAll = mode == Prefs.OVERRIDE_MASTER_PASSWORD
        binding.btnPauseAll.visibility = if (showPauseAll) View.VISIBLE else View.GONE
        binding.btnPauseAll.isEnabled = showPauseAll
        val pauseAllUntil = Prefs.pauseAllUntil
        val isPauseActive = pauseAllUntil > System.currentTimeMillis()
        if (isPauseActive) {
            binding.tvPauseAllStatus.visibility = View.VISIBLE
            binding.tvPauseAllStatus.text = getString(
                R.string.settings_pause_all_status,
                UnlockMethodUtils.formatDateTime(pauseAllUntil)
            )
        } else {
            binding.tvPauseAllStatus.visibility = View.GONE
            binding.tvPauseAllStatus.text = ""
        }
        binding.btnResumeAll.visibility = if (isPauseActive) View.VISIBLE else View.GONE
    }

    private fun promptDisableMasterPassword() {
        promptForMasterPassword(
            titleRes = R.string.block_master_password,
            onVerified = {
                Prefs.masterPassword = ""
                Prefs.pauseAllUntil = 0L
                Prefs.masterPasswordOverrideMode = Prefs.OVERRIDE_NONE
                pendingOverrideMode = null
                binding.etOldPwd.text?.clear()
                binding.etMasterPwd.text?.clear()
                binding.tilOldPwd.error = null
                binding.tilMasterPwd.error = null
                setSelectedOverrideMode(Prefs.OVERRIDE_NONE)
                updateMasterPasswordViews()
                updatePauseAllViews()
            }
        )
    }

    private fun handleOverrideModeSelection(currentMode: String, selectedMode: String, hasMasterPassword: Boolean) {
        when {
            selectedMode == Prefs.OVERRIDE_NONE -> {
                pendingOverrideMode = null
                if (currentMode != Prefs.OVERRIDE_NONE && hasMasterPassword) {
                    setSelectedOverrideMode(currentMode)
                    promptDisableMasterPassword()
                } else {
                    applyOverrideMode(selectedMode)
                    binding.etOldPwd.text?.clear()
                }
            }

            !hasMasterPassword -> {
                pendingOverrideMode = selectedMode
                updateMasterPasswordViews()
                updatePauseAllViews()
                Snackbar.make(binding.root, "Set a master password first", Snackbar.LENGTH_SHORT).show()
            }

            else -> {
                pendingOverrideMode = null
                applyOverrideMode(selectedMode)
            }
        }
    }

    private fun applyOverrideMode(mode: String) {
        Prefs.masterPasswordOverrideMode = mode
        setSelectedOverrideMode(mode)
        updateMasterPasswordViews()
        updatePauseAllViews()
    }

    private fun setSelectedOverrideMode(mode: String) {
        isUpdatingOverrideModeSelection = true
        binding.rgOverrideMode.check(
            when (mode) {
                Prefs.OVERRIDE_MASTER_PASSWORD -> R.id.rbOverrideMasterPassword
                Prefs.OVERRIDE_STRICT -> R.id.rbOverrideStrict
                else -> R.id.rbOverrideNone
            }
        )
        updateOverrideModeDescription(mode)
        isUpdatingOverrideModeSelection = false
    }

    private fun modeForCheckedId(checkedId: Int): String? {
        return when (checkedId) {
            R.id.rbOverrideNone -> Prefs.OVERRIDE_NONE
            R.id.rbOverrideMasterPassword -> Prefs.OVERRIDE_MASTER_PASSWORD
            R.id.rbOverrideStrict -> Prefs.OVERRIDE_STRICT
            else -> null
        }
    }

    private fun updateOverrideModeDescription(mode: String) {
        binding.tvOverrideModeDesc.text = getString(
            when (mode) {
                Prefs.OVERRIDE_MASTER_PASSWORD -> R.string.override_mode_master_password_desc
                Prefs.OVERRIDE_STRICT -> R.string.override_mode_strict_desc
                else -> R.string.override_mode_none_desc
            }
        )
    }

    private fun promptForMasterPassword(titleRes: Int, onVerified: () -> Unit) {
        val initialLockout = masterPasswordGuard.checkAllowed()
        if (initialLockout != null) {
            Snackbar.make(
                binding.root,
                formatLockoutMessage(initialLockout),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        val passwordInput = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.settings_old_password_hint)
            maxLines = 1
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setView(passwordInput)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val enteredPassword = passwordInput.text?.toString().orEmpty()
                val lockoutRemaining = masterPasswordGuard.checkAllowed()
                when {
                    lockoutRemaining != null -> passwordInput.error = formatLockoutMessage(lockoutRemaining)
                    enteredPassword.isBlank() -> passwordInput.error = getString(R.string.settings_master_pwd_required)
                    !PasswordHasher.verify(enteredPassword, Prefs.masterPassword) -> {
                        masterPasswordGuard.recordFailure()
                        passwordInput.error = masterPasswordGuard.checkAllowed()?.let(::formatLockoutMessage)
                            ?: getString(R.string.settings_old_password_wrong)
                    }
                    else -> {
                        masterPasswordGuard.reset()
                        passwordInput.error = null
                        dialog.dismiss()
                        onVerified()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun formatLockoutMessage(remainingMillis: Long): String {
        return "Too many attempts. Try again in ${UnlockMethodUtils.formatCountdown(remainingMillis)}"
    }

    private fun showPauseAllDurationDialog() {
        val labels = pauseAllOptions.map { it.label }.toTypedArray()
        var selectedIndex = 0
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_pause_all_duration)
            .setSingleChoiceItems(labels, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                pauseAllBlocks(pauseAllOptions[selectedIndex])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun pauseAllBlocks(option: PauseAllOption) {
        viewLifecycleOwner.lifecycleScope.launch {
            val until = System.currentTimeMillis() + option.durationMs
            Prefs.pauseAllUntil = until
            dao.getAll().forEach { block ->
                if (until > block.pausedUntil) {
                    dao.setPausedUntil(block.id, until)
                }
            }
            WidgetRefresh.refresh(requireContext().applicationContext)
            updatePauseAllViews()
            Snackbar.make(
                binding.root,
                getString(R.string.settings_pause_all_confirm, option.label),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun resumeAllBlocks() {
        viewLifecycleOwner.lifecycleScope.launch {
            Prefs.pauseAllUntil = 0L
            dao.getAll().forEach { block ->
                if (block.pausedUntil > System.currentTimeMillis()) {
                    dao.setPausedUntil(block.id, 0L)
                }
            }
            WidgetRefresh.refresh(requireContext().applicationContext)
            updatePauseAllViews()
            Snackbar.make(binding.root, getString(R.string.settings_resume_all_confirm), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun exportBlocks() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { backupManager.export() }
            }.onSuccess { json ->
                val backup = BlockBackup.fromJson(JSONObject(json))
                if (backup.blocks.isEmpty() && backup.folders.isEmpty()) {
                    Snackbar.make(binding.root, getString(R.string.settings_export_empty), Snackbar.LENGTH_SHORT).show()
                    return@onSuccess
                }
                pendingExportRequest = PendingExportRequest(
                    json = json,
                    blocksExported = backup.blocks.size,
                    foldersExported = backup.folders.size
                )
                exportBlocksLauncher.launch(
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        putExtra(Intent.EXTRA_TITLE, "qrzen-blocks-${LocalDate.now()}.json")
                    }
                )
            }.onFailure { error ->
                Snackbar.make(
                    binding.root,
                    getString(R.string.settings_export_error, error.message ?: error.javaClass.simpleName),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun importBlocks() {
        importBlocksLauncher.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "*/*"))
            }
        )
    }

    private fun showImportConfirmation(json: String) {
        runCatching {
            BlockBackup.fromJson(JSONObject(json)).also { backup ->
                require(backup.version == 1) { "Unsupported backup version: ${backup.version}" }
            }
        }.onSuccess { backup ->
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_import_confirm_title)
                .setMessage(
                    getString(
                        R.string.settings_import_confirm_message,
                        backup.blocks.size,
                        backup.folders.size
                    )
                )
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    performImport(json)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }.onFailure { error ->
            Snackbar.make(
                binding.root,
                getString(R.string.settings_import_error, error.message ?: error.javaClass.simpleName),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun performImport(json: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { backupManager.import(json) }
            }.onSuccess { result ->
                WidgetRefresh.refresh(requireContext().applicationContext)
                Snackbar.make(
                    binding.root,
                    getString(
                        R.string.settings_import_success,
                        result.blocksImported,
                        result.foldersImported
                    ),
                    Snackbar.LENGTH_LONG
                ).show()
            }.onFailure { error ->
                Snackbar.make(
                    binding.root,
                    getString(R.string.settings_import_error, error.message ?: error.javaClass.simpleName),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class PauseAllOption(val label: String, val durationMs: Long)
    private data class PendingExportRequest(
        val json: String,
        val blocksExported: Int,
        val foldersExported: Int
    )
}
