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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

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
            onPause = { block -> showPauseDurationPicker(block) },
            onBlockNow = { block -> viewModel.blockNow(block) },
            onEdit = { block ->
                startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
                    putExtra(EditBlockActivity.EXTRA_BLOCK_ID, block.id)
                })
            },
            onArchive = { block ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Archive Block")
                    .setMessage("Archive '${block.title}'? It will be hidden but can be restored later.")
                    .setPositiveButton("Archive") { _, _ -> viewModel.archive(block) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onDelete = { block ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Block")
                    .setMessage("Delete '${block.title}'? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(block) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onRestartFromPause = { block ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Restart Block")
                    .setMessage("Restart '${block.title}' now? This will end the pause and resume blocking.")
                    .setPositiveButton("Restart") { _, _ -> viewModel.unpause(block) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
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

    private fun showPauseDurationPicker(block: AppBlock) {
        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "Rest of day", "Indefinitely")
        AlertDialog.Builder(requireContext())
            .setTitle("Pause '${block.title}'")
            .setItems(options) { _, which ->
                val durationMs = when (which) {
                    0 -> 15 * 60_000L
                    1 -> 30 * 60_000L
                    2 -> 60 * 60_000L
                    3 -> 2 * 60 * 60_000L
                    4 -> millisUntilMidnight()
                    5 -> Long.MAX_VALUE
                    else -> 0L
                }
                if (durationMs > 0L) viewModel.pause(block, durationMs)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun millisUntilMidnight(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis - System.currentTimeMillis()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
