package com.qrzen.app.ui.main

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.FragmentHomeBinding
import com.qrzen.app.ui.block.EditBlockActivity
import com.qrzen.app.ui.block.QrDisplayFragment
import com.qrzen.app.ui.pomodoro.PomodoroActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: BlockAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BlockAdapter(
            onToggle = { block, enabled -> viewModel.setEnabled(block, enabled) },
            onLongPress = { block -> showBlockOptions(block) }
        )
        binding.rvBlocks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBlocks.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val options = arrayOf(getString(R.string.block_type_blocklist), getString(R.string.block_type_allowlist))
            var selectedIndex = 0
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.block_type_title))
                .setSingleChoiceItems(options, 0) { _, which -> selectedIndex = which }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
                        putExtra(EditBlockActivity.EXTRA_IS_ALLOWLIST, selectedIndex == 1)
                    })
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.blocks.collect { blocks ->
                    adapter.submitList(blocks)
                    binding.tvEmpty.visibility = if (blocks.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvBlocks.visibility = if (blocks.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        binding.cardServiceWarning.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        updateServiceWarning()
    }

    override fun onResume() {
        super.onResume()
        updateServiceWarning()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == requireContext().packageName
        }
    }

    private fun updateServiceWarning() {
        val enabled = isAccessibilityServiceEnabled()
        binding.cardServiceWarning.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun showBlockOptions(block: AppBlock) {
        val baseOptions = mutableListOf("Edit", "Delete", "Show QR Code")
        if (block.isPomodoroBlock) baseOptions.add("Start Pomodoro")
        val options = baseOptions.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
                            putExtra(EditBlockActivity.EXTRA_BLOCK_ID, block.id)
                        })
                    }
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Block")
                            .setMessage("Delete '${block.title}'? This cannot be undone.")
                            .setPositiveButton("Delete") { _, _ -> viewModel.delete(block) }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    2 -> QrDisplayFragment.newInstance(block.qrSecret).show(childFragmentManager, "qr")
                    3 -> {
                        startActivity(
                            Intent(requireContext(), PomodoroActivity::class.java).apply {
                                putExtra(PomodoroActivity.EXTRA_BLOCK_ID, block.id)
                            }
                        )
                    }
                }
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
