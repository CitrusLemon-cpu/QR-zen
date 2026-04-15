package com.qrzen.app.ui.main

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPrefs()
        setupListeners()
    }

    private fun loadPrefs() {
        binding.swMasterPwd.isChecked = Prefs.masterPasswordEnabled
        binding.tilMasterPwd.visibility = if (Prefs.masterPasswordEnabled) View.VISIBLE else View.GONE
        binding.btnSavePwd.visibility = if (Prefs.masterPasswordEnabled) View.VISIBLE else View.GONE
        binding.etMasterPwd.setText(Prefs.masterPassword)
        binding.swRemoveNotif.isChecked = Prefs.removeNotifications
        binding.swSilentMode.isChecked = Prefs.silentMode
    }

    private fun setupListeners() {
        binding.swMasterPwd.setOnCheckedChangeListener { _, checked ->
            Prefs.masterPasswordEnabled = checked
            binding.tilMasterPwd.visibility = if (checked) View.VISIBLE else View.GONE
            binding.btnSavePwd.visibility = if (checked) View.VISIBLE else View.GONE
        }

        binding.btnSavePwd.setOnClickListener {
            val pwd = binding.etMasterPwd.text?.toString()?.trim() ?: ""
            if (pwd.isEmpty()) {
                binding.tilMasterPwd.error = "Password cannot be empty"
                return@setOnClickListener
            }
            binding.tilMasterPwd.error = null
            Prefs.masterPassword = pwd
            Snackbar.make(binding.root, "Master password saved", Snackbar.LENGTH_SHORT).show()
        }

        binding.swRemoveNotif.setOnCheckedChangeListener { _, checked ->
            Prefs.removeNotifications = checked
        }

        binding.swSilentMode.setOnCheckedChangeListener { _, checked ->
            Prefs.silentMode = checked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
