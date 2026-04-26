package com.qrzen.app.ui.main

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.qrzen.app.ui.unlock.UnlockChallengeActivity
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private data class PendingUnlockAction(
        val block: AppBlock,
        val action: String,
        val toggleEnabledState: Boolean? = null
    )

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: BlockAdapter
    private var pendingUnlockAction: PendingUnlockAction? = null

    private val unlockChallengeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val pending = pendingUnlockAction ?: return@registerForActivityResult
        pendingUnlockAction = null
        if (result.resultCode == Activity.RESULT_OK) {
            completePendingUnlockAction(pending)
        }
    }

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
            onToggle = { block, enabled ->
                if (enabled) {
                    if (block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO) {
                        showPomodoroActivationDialog(block)
                        false
                    } else {
                        val isManualAllowlist = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
                            block.isAllowlistMode
                        if (isManualAllowlist) {
                            showAllowlistDurationPicker(block)
                            false
                        } else {
                            viewModel.setEnabled(block, true)
                            goToHomeIfBlockActive(block)
                            true
                        }
                    }
                } else {
                    if (block.toggleLockUntil > System.currentTimeMillis()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.lock_timer_locked, UnlockMethodUtils.formatDateTime(block.toggleLockUntil)),
                            Toast.LENGTH_SHORT
                        ).show()
                        false
                    } else {
                        val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
                            UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE
                        if (isManualNoMethod) {
                            viewModel.disableAndClearTimers(block)
                            true
                        } else {
                            requestUnlock(block, UnlockChallengeActivity.ACTION_TOGGLE, enabled)
                        }
                    }
                }
            },
            onPause = { block ->
                requestUnlock(block, UnlockChallengeActivity.ACTION_PAUSE)
            },
            onBlockNow = { block -> showBlockNowDurationPicker(block) },
            onEdit = { block ->
                requestUnlock(block, UnlockChallengeActivity.ACTION_EDIT)
            },
            onArchive = { block ->
                requestUnlock(block, UnlockChallengeActivity.ACTION_ARCHIVE)
            },
            onDelete = { block ->
                requestUnlock(block, UnlockChallengeActivity.ACTION_DELETE)
            },
            onRestartFromPause = { block ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Restart Block")
                    .setMessage("Restart '${block.title}' now? This will end the pause and resume blocking.")
                    .setPositiveButton("Restart") { _, _ ->
                        viewModel.unpause(block)
                        goToHomeIfBlockActive(block)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onLockWithTimer = { block -> showLockWithTimerDialog(block) }
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

    private fun requestUnlock(block: AppBlock, action: String, toggleEnabledState: Boolean? = null): Boolean {
        val pending = PendingUnlockAction(block, action, toggleEnabledState)
        if (shouldSkipUnlock(block, action, toggleEnabledState)) {
            completePendingUnlockAction(pending)
            return true
        }
        val method = UnlockMethodUtils.getNormalizedMethod(block)
        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) {
            viewLifecycleOwner.lifecycleScope.launch {
                val isActive = viewModel.isBlockCurrentlyActive(block)
                if (!isActive) {
                    completePendingUnlockAction(pending)
                } else {
                    pendingUnlockAction = pending
                    unlockChallengeLauncher.launch(
                        UnlockChallengeActivity.createIntent(requireContext(), block.id, action)
                    )
                }
            }
            return false
        }
        pendingUnlockAction = pending
        unlockChallengeLauncher.launch(UnlockChallengeActivity.createIntent(requireContext(), block.id, action))
        return false
    }

    private fun shouldSkipUnlock(block: AppBlock, action: String, toggleEnabledState: Boolean?): Boolean {
        if (action == UnlockChallengeActivity.ACTION_TOGGLE && toggleEnabledState == true) return true
        if (!block.isEnabled && action == UnlockChallengeActivity.ACTION_EDIT) return true
        if (!block.isEnabled && (action == UnlockChallengeActivity.ACTION_ARCHIVE || action == UnlockChallengeActivity.ACTION_DELETE)) return true
        if (block.toggleLockUntil > System.currentTimeMillis()) return false
        val method = UnlockMethodUtils.getNormalizedMethod(block)
        if (method == UnlockMethodUtils.METHOD_WHILE_ACTIVE) return false
        if (method == UnlockMethodUtils.METHOD_TIMER && UnlockMethodUtils.isTimerExpired(block)) return true
        return method == UnlockMethodUtils.METHOD_NONE
    }

    private fun completePendingUnlockAction(pending: PendingUnlockAction) {
        when (pending.action) {
            UnlockChallengeActivity.ACTION_EDIT -> {
                startActivity(Intent(requireContext(), EditBlockActivity::class.java).apply {
                    putExtra(EditBlockActivity.EXTRA_BLOCK_ID, pending.block.id)
                })
            }
            UnlockChallengeActivity.ACTION_PAUSE -> showPauseDurationPicker(pending.block)
            UnlockChallengeActivity.ACTION_TOGGLE -> {
                pending.toggleEnabledState?.let { enabled ->
                    viewModel.setEnabled(pending.block, enabled)
                }
            }
            UnlockChallengeActivity.ACTION_ARCHIVE -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Archive Block")
                    .setMessage("Archive '${pending.block.title}'? It will be hidden but can be restored later.")
                    .setPositiveButton("Archive") { _, _ -> viewModel.archive(pending.block) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            UnlockChallengeActivity.ACTION_DELETE -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Block")
                    .setMessage("Delete '${pending.block.title}'? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(pending.block) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun showPauseDurationPicker(block: AppBlock) {
        val isPomodoroActive = block.blockingStyle == UnlockMethodUtils.STYLE_POMODORO &&
            block.pomodoroRoundsTotal > 0
        val pomodoroState = if (isPomodoroActive) UnlockMethodUtils.computePomodoroState(block) else null
        val sessionRemainingMs = pomodoroState?.sessionRemainingMs ?: Long.MAX_VALUE

        val durations = mutableListOf<Pair<String, Long>>()
        val candidates = listOf(
            "15 minutes" to 15 * 60_000L,
            "30 minutes" to 30 * 60_000L,
            "1 hour" to 60 * 60_000L,
            "2 hours" to 2 * 60 * 60_000L
        )
        for ((label, ms) in candidates) {
            if (ms <= sessionRemainingMs) durations.add(label to ms)
        }
        if (!isPomodoroActive) {
            durations.add("Rest of day" to millisUntilMidnight())
            durations.add("Indefinitely" to -1L)
        } else {
            durations.add(getString(R.string.pomodoro_end_early) to -2L)
        }

        val options = durations.map { it.first }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pause '${block.title}'")
            .setItems(options) { _, which ->
                val (_, durationMs) = durations[which]
                when (durationMs) {
                    -1L -> viewModel.setEnabled(block, false)
                    -2L -> viewModel.disableAndClearTimers(block)
                    else -> if (durationMs > 0L) viewModel.pause(block, durationMs)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPomodoroActivationDialog(block: AppBlock) {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val roundsLabel = android.widget.TextView(requireContext()).apply {
            text = getString(R.string.pomodoro_rounds_label)
            textSize = 14f
        }
        dialogView.addView(roundsLabel)

        val roundsPicker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 20
            value = 4
        }
        dialogView.addView(roundsPicker)

        val lockCheckBox = CheckBox(requireContext()).apply {
            text = getString(R.string.pomodoro_lock_editing_label)
            isChecked = block.pomodoroLockEditing
        }
        dialogView.addView(lockCheckBox)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.pomodoro_activation_title, block.title))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val rounds = roundsPicker.value
                val lockEditing = lockCheckBox.isChecked
                val focusMs = block.pomodoroDurationMin * 60_000L
                val breakMs = block.pomodoroBreakMin * 60_000L
                val totalMs = focusMs * rounds + breakMs * (rounds - 1)
                val endTime = UnlockMethodUtils.formatDateTime(System.currentTimeMillis() + totalMs)

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.pomodoro_confirm_title))
                    .setMessage(
                        getString(
                            R.string.pomodoro_confirm_message,
                            rounds,
                            block.pomodoroDurationMin,
                            block.pomodoroBreakMin,
                            endTime
                        )
                    )
                    .setPositiveButton(getString(R.string.pomodoro_start)) { _, _ ->
                        viewModel.startPomodoroSession(block, rounds, lockEditing)
                        startActivity(Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showBlockNowDurationPicker(block: AppBlock) {
        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
        AlertDialog.Builder(requireContext())
            .setTitle("Block '${block.title}' now for...")
            .setItems(options) { _, which ->
                val durationMs = when (which) {
                    0 -> 15 * 60_000L
                    1 -> 30 * 60_000L
                    2 -> 60 * 60_000L
                    3 -> 2 * 60 * 60_000L
                    4 -> 4 * 60 * 60_000L
                    5 -> millisUntilMidnight()
                    else -> 0L
                }
                if (durationMs > 0L) {
                    viewModel.blockNow(block, durationMs)
                    startActivity(Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAllowlistDurationPicker(block: AppBlock) {
        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "Rest of day")
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.allowlist_duration_title, block.title))
            .setItems(options) { _, which ->
                val durationMs = when (which) {
                    0 -> 15 * 60_000L
                    1 -> 30 * 60_000L
                    2 -> 60 * 60_000L
                    3 -> 2 * 60 * 60_000L
                    4 -> 4 * 60 * 60_000L
                    5 -> millisUntilMidnight()
                    else -> 0L
                }
                if (durationMs > 0L) {
                    viewModel.enableWithActiveUntil(block, durationMs)
                    startActivity(Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLockWithTimerDialog(block: AppBlock) {
        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "8 hours", "Rest of day")
        var selectedIndex = 0

        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val checkBox = if (!block.isAllowlistMode) {
            CheckBox(requireContext()).apply {
                text = getString(R.string.lock_with_timer_auto_off)
                isChecked = false
            }.also { dialogView.addView(it) }
        } else {
            null
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.lock_with_timer_title, block.title))
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val durationMs = when (selectedIndex) {
                    0 -> 15 * 60_000L
                    1 -> 30 * 60_000L
                    2 -> 60 * 60_000L
                    3 -> 2 * 60 * 60_000L
                    4 -> 4 * 60 * 60_000L
                    5 -> 8 * 60 * 60_000L
                    6 -> millisUntilMidnight()
                    else -> 0L
                }
                if (durationMs > 0L) {
                    val autoDisable = block.isAllowlistMode || (checkBox?.isChecked == true)
                    viewModel.lockWithTimer(block, durationMs, autoDisable)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
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

    private fun goToHomeIfBlockActive(block: AppBlock) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.isBlockCurrentlyActive(block)) {
                startActivity(Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
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
