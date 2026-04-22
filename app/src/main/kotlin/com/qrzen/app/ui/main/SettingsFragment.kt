package com.qrzen.app.ui.main

import android.app.NotificationManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.FragmentSettingsBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    @Inject lateinit var dao: AppBlockDao

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var isUpdatingMasterPasswordSwitch = false
    private val pauseAllOptions = listOf(
        PauseAllOption("30 minutes", 30 * 60_000L),
        PauseAllOption("1 hour", 60 * 60_000L),
        PauseAllOption("2 hours", 2 * 60 * 60_000L),
        PauseAllOption("4 hours", 4 * 60 * 60_000L),
        PauseAllOption("8 hours", 8 * 60 * 60_000L),
        PauseAllOption("12 hours", 12 * 60 * 60_000L),
        PauseAllOption("24 hours", 24 * 60 * 60_000L)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swMasterPwd.isChecked = Prefs.masterPasswordEnabled
        binding.etMasterPwd.setText(Prefs.masterPassword)
        binding.swRemoveNotif.isChecked = Prefs.removeNotifications
        binding.swSilentMode.isChecked = Prefs.silentMode
        updateMasterPasswordViews()
        updatePauseAllViews()

        binding.swMasterPwd.setOnCheckedChangeListener { _, checked ->
            if (isUpdatingMasterPasswordSwitch) return@setOnCheckedChangeListener
            if (!checked && Prefs.masterPassword.isNotEmpty()) {
                setMasterPasswordSwitchChecked(true)
                promptDisableMasterPassword()
                return@setOnCheckedChangeListener
            }
            Prefs.masterPasswordEnabled = checked
            if (!checked) {
                binding.etOldPwd.text?.clear()
            }
            updateMasterPasswordViews()
            updatePauseAllViews()
        }

        binding.btnSavePwd.setOnClickListener {
            val newPassword = binding.etMasterPwd.text?.toString()?.trim().orEmpty()
            val currentPassword = binding.etOldPwd.text?.toString().orEmpty()
            val existingPassword = Prefs.masterPassword
            if (newPassword.isEmpty()) {
                binding.tilMasterPwd.error = "Password cannot be empty"
                return@setOnClickListener
            }
            if (existingPassword.isNotEmpty() && currentPassword != existingPassword) {
                binding.tilOldPwd.error = getString(R.string.settings_old_password_wrong)
                return@setOnClickListener
            }
            binding.tilOldPwd.error = null
            binding.tilMasterPwd.error = null
            Prefs.masterPassword = newPassword
            Prefs.masterPasswordEnabled = true
            Snackbar.make(binding.root, "Master password saved", Snackbar.LENGTH_SHORT).show()
            binding.etOldPwd.text?.clear()
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

        binding.swRemoveNotif.setOnCheckedChangeListener { _, checked ->
            Prefs.removeNotifications = checked
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
            updateMasterPasswordViews()
            updatePauseAllViews()
        }
    }

    private fun updateMasterPasswordViews() {
        val masterPasswordEnabled = Prefs.masterPasswordEnabled
        val hasMasterPassword = Prefs.masterPassword.isNotEmpty()
        binding.tilOldPwd.visibility = if (masterPasswordEnabled && hasMasterPassword) View.VISIBLE else View.GONE
        binding.tilMasterPwd.visibility = if (masterPasswordEnabled) View.VISIBLE else View.GONE
        binding.btnSavePwd.visibility = if (masterPasswordEnabled) View.VISIBLE else View.GONE
        if (!masterPasswordEnabled || !hasMasterPassword) {
            binding.tilOldPwd.error = null
        }
        if (!masterPasswordEnabled) {
            binding.tilMasterPwd.error = null
        }
    }

    private fun updatePauseAllViews() {
        val masterPasswordEnabled = Prefs.masterPasswordEnabled
        binding.btnPauseAll.visibility = if (masterPasswordEnabled) View.VISIBLE else View.GONE
        binding.btnPauseAll.isEnabled = masterPasswordEnabled
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
                Prefs.masterPasswordEnabled = false
                binding.etOldPwd.text?.clear()
                binding.etMasterPwd.text?.clear()
                binding.tilOldPwd.error = null
                binding.tilMasterPwd.error = null
                setMasterPasswordSwitchChecked(false)
                updateMasterPasswordViews()
                updatePauseAllViews()
            }
        )
    }

    private fun promptForMasterPassword(titleRes: Int, onVerified: () -> Unit) {
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
                when {
                    enteredPassword.isBlank() -> passwordInput.error = getString(R.string.settings_master_pwd_required)
                    enteredPassword != Prefs.masterPassword -> passwordInput.error = getString(R.string.settings_old_password_wrong)
                    else -> {
                        passwordInput.error = null
                        dialog.dismiss()
                        onVerified()
                    }
                }
            }
        }
        dialog.show()
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

    private fun setMasterPasswordSwitchChecked(checked: Boolean) {
        isUpdatingMasterPasswordSwitch = true
        binding.swMasterPwd.isChecked = checked
        isUpdatingMasterPasswordSwitch = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class PauseAllOption(val label: String, val durationMs: Long)
}
