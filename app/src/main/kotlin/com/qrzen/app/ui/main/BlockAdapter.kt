package com.qrzen.app.ui.main

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.ItemBlockBinding
import com.qrzen.app.databinding.ItemSelectedAppIconBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockAdapter(
    private val onToggle: (AppBlock, Boolean) -> Boolean,
    private val onPause: (AppBlock) -> Unit,
    private val onBlockNow: (AppBlock) -> Unit,
    private val onEdit: (AppBlock) -> Unit,
    private val onArchive: (AppBlock) -> Unit,
    private val onDelete: (AppBlock) -> Unit,
    private val onRestartFromPause: (AppBlock) -> Unit
) : ListAdapter<AppBlock, BlockAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppBlock>() {
            override fun areItemsTheSame(a: AppBlock, b: AppBlock) = a.id == b.id
            override fun areContentsTheSame(a: AppBlock, b: AppBlock) = a == b
        }
    }

    data class BlockAppIcon(
        val packageName: String,
        val icon: android.graphics.drawable.Drawable
    )

    private class BlockAppsIconAdapter(
        private val apps: List<BlockAppIcon>
    ) : RecyclerView.Adapter<BlockAppsIconAdapter.ViewHolder>() {
        class ViewHolder(val binding: ItemSelectedAppIconBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: BlockAppIcon) {
                binding.ivSelectedAppIcon.setImageDrawable(item.icon)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemSelectedAppIconBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(apps[position])

        override fun getItemCount(): Int = apps.size
    }

    inner class ViewHolder(val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        private var countDownTimer: CountDownTimer? = null
        private var blockNowTimer: CountDownTimer? = null
        private var usageStatusTimer: CountDownTimer? = null
        private var iconLoadJob: Job? = null
        private var usageQueryJob: Job? = null
        private var boundPackages: List<String> = emptyList()

        fun bind(block: AppBlock) {
            countDownTimer?.cancel()
            countDownTimer = null
            blockNowTimer?.cancel()
            blockNowTimer = null
            usageStatusTimer?.cancel()
            usageStatusTimer = null
            iconLoadJob?.cancel()
            iconLoadJob = null
            usageQueryJob?.cancel()
            usageQueryJob = null

            binding.tvTitle.text = block.title
            val modePrefix = if (block.isAllowlistMode) "Allowlist" else "Blocklist"
            when (block.blockingStyle) {
                UnlockMethodUtils.STYLE_MANUAL -> {
                    binding.tvTimeRange.text = "Manual"
                    binding.tvDays.text = modePrefix
                }
                UnlockMethodUtils.STYLE_SCHEDULE -> {
                    binding.tvTimeRange.text = "Scheduled"
                    binding.tvDays.text = modePrefix
                }
                UnlockMethodUtils.STYLE_USAGE_LIMIT -> {
                    val period = if (block.usageLimitPeriod == "HOURLY") "per hour" else "per day"
                    binding.tvTimeRange.text = "${block.usageLimitMinutes} min $period"
                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"

                    val lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
                    if (lifecycleOwner != null) {
                        usageQueryJob = lifecycleOwner.lifecycleScope.launch {
                            val remainingText = withContext(Dispatchers.IO) {
                                computeUsageLimitRemaining(binding.root.context, block)
                            }
                            binding.tvTimeRange.text = remainingText
                        }
                    }
                }
                UnlockMethodUtils.STYLE_WAIT_TIMER -> {
                    binding.tvTimeRange.text = "Wait ${block.waitTimerWaitMinutes}m after ${block.waitTimerUseMinutes}m use"
                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"

                    val lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
                    if (lifecycleOwner != null) {
                        usageQueryJob = lifecycleOwner.lifecycleScope.launch {
                            val status = withContext(Dispatchers.IO) {
                                computeWaitTimerStatus(binding.root.context, block)
                            }
                            binding.tvTimeRange.text = status.text

                            if (status.blockingRemainingMs > 0L) {
                                usageStatusTimer?.cancel()
                                usageStatusTimer = object : CountDownTimer(status.blockingRemainingMs, 1000L) {
                                    override fun onTick(ms: Long) {
                                        val min = ms / 60_000
                                        val sec = (ms % 60_000) / 1000
                                        binding.tvTimeRange.text = "Blocked for ${min}m ${sec}s"
                                    }

                                    override fun onFinish() {
                                        binding.tvTimeRange.text = "${block.waitTimerUseMinutes}m of use left"
                                    }
                                }.start()
                            }
                        }
                    }
>>>>>>> origin/main
                }
                else -> {
                    binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"
                }
            }

            val unlockSummary = UnlockMethodUtils.getUnlockMethodSummary(binding.root.context, block)
            binding.tvUnlockMethod.visibility = if (unlockSummary.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.tvUnlockMethod.text = unlockSummary.orEmpty()

            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = block.isEnabled
            bindToggleListener(block)

            binding.btnOverflow.setOnClickListener { view ->
                showPopupMenu(view, block)
            }

            setupPauseTimer(block)
            setupBlockNowTimer(block)

            val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            boundPackages = packages
            if (packages.isEmpty()) {
                binding.rvBlockApps.visibility = View.GONE
                binding.rvBlockApps.adapter = null
                return
            }

            binding.rvBlockApps.visibility = View.VISIBLE
            binding.rvBlockApps.adapter = null
            if (binding.rvBlockApps.layoutManager == null) {
                binding.rvBlockApps.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            }

            val lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
            if (lifecycleOwner == null) {
                binding.rvBlockApps.visibility = View.GONE
                binding.rvBlockApps.adapter = null
                return
            }

            val pm = binding.root.context.packageManager
            iconLoadJob = lifecycleOwner.lifecycleScope.launch {
                val icons = withContext(Dispatchers.IO) {
                    packages.mapNotNull { pkg ->
                        try {
                            val appInfo = pm.getApplicationInfo(pkg, 0)
                            BlockAppIcon(pkg, pm.getApplicationIcon(appInfo))
                        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
                            null
                        }
                    }
                }
                if (packages != boundPackages) return@launch
                if (icons.isEmpty()) {
                    binding.rvBlockApps.visibility = View.GONE
                    binding.rvBlockApps.adapter = null
                } else {
                    binding.rvBlockApps.visibility = View.VISIBLE
                    binding.rvBlockApps.adapter = BlockAppsIconAdapter(icons)
                }
            }
        }

        private fun bindToggleListener(block: AppBlock) {
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                val accepted = onToggle(block, checked)
                if (!accepted) {
                    binding.switchEnabled.setOnCheckedChangeListener(null)
                    binding.switchEnabled.isChecked = block.isEnabled
                    bindToggleListener(block)
                }
            }
        }

        private fun setupPauseTimer(block: AppBlock) {
            val now = System.currentTimeMillis()
            val isPaused = block.pausedUntil > now
            val isIndefinite = block.pausedUntil == Long.MAX_VALUE

            if (isPaused || isIndefinite) {
                binding.tvPauseTimer.visibility = View.VISIBLE
                binding.tvPauseTimer.setOnClickListener {
                    onRestartFromPause(block)
                }

                if (isIndefinite) {
                    binding.tvPauseTimer.text = "⏸ Paused indefinitely • Tap to restart"
                } else {
                    val remaining = block.pausedUntil - now
                    binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(remaining)} remaining"
                    countDownTimer = object : CountDownTimer(remaining, 1000L) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.tvPauseTimer.text = "⏸ Paused – ${formatDuration(millisUntilFinished)} remaining"
                        }

                        override fun onFinish() {
                            binding.tvPauseTimer.visibility = View.GONE
                        }
                    }.start()
                }
            } else {
                binding.tvPauseTimer.visibility = View.GONE
            }
        }

        private fun setupBlockNowTimer(block: AppBlock) {
            val now = System.currentTimeMillis()
            if (block.blockNowUntil > now) {
                binding.tvBlockNowTimer.visibility = View.VISIBLE
                val remaining = block.blockNowUntil - now
                binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(remaining)}"
                blockNowTimer = object : CountDownTimer(remaining, 1000L) {
                    override fun onTick(ms: Long) {
                        binding.tvBlockNowTimer.text = "⏱ Blocking for ${formatDuration(ms)}"
                    }

                    override fun onFinish() {
                        binding.tvBlockNowTimer.visibility = View.GONE
                    }
                }.start()
            } else {
                binding.tvBlockNowTimer.visibility = View.GONE
            }
        }

        private fun showPopupMenu(anchor: View, block: AppBlock) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_block_overflow, popup.menu)

            val now = System.currentTimeMillis()
            val isPaused = block.pausedUntil > now || block.pausedUntil == Long.MAX_VALUE
            popup.menu.findItem(R.id.action_pause)?.title = if (isPaused) "Unpause" else "Pause"

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_pause -> {
                        if (isPaused) onRestartFromPause(block) else onPause(block)
                        true
                    }
                    R.id.action_block_now -> {
                        onBlockNow(block)
                        true
                    }
                    R.id.action_edit -> {
                        onEdit(block)
                        true
                    }
                    R.id.action_archive -> {
                        onArchive(block)
                        true
                    }
                    R.id.action_delete -> {
                        onDelete(block)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        fun cancelTimer() {
            countDownTimer?.cancel()
            countDownTimer = null
            blockNowTimer?.cancel()
            blockNowTimer = null
            usageStatusTimer?.cancel()
            usageStatusTimer = null
            iconLoadJob?.cancel()
            iconLoadJob = null
            usageQueryJob?.cancel()
            usageQueryJob = null
        }
    }

    private data class WaitTimerStatus(val text: String, val blockingRemainingMs: Long)

    private fun computeUsageLimitRemaining(context: Context, block: AppBlock): String {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
            ?: return "${block.usageLimitMinutes}m left"

        val now = System.currentTimeMillis()
        val startTime = when (block.usageLimitPeriod) {
            "HOURLY" -> now - 3_600_000L
            else -> {
                java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val usageStats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_BEST,
            startTime,
            now
        )
        val totalUsageMs = usageStats
            ?.filter { it.packageName in packages }
            ?.sumOf { it.totalTimeInForeground } ?: 0L

        val limitMs = block.usageLimitMinutes * 60_000L
        val remainingMs = (limitMs - totalUsageMs).coerceAtLeast(0L)
        val remainingMin = remainingMs / 60_000L

        val periodLabel = if (block.usageLimitPeriod == "HOURLY") "this hour" else "today"
        return if (remainingMs <= 0L) {
            "Limit reached"
        } else {
            "${remainingMin}m left $periodLabel"
        }
    }

    private fun computeWaitTimerStatus(context: Context, block: AppBlock): WaitTimerStatus {
        val now = System.currentTimeMillis()
        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
        val blockingUntilKey = "wait_timer_blocking_${block.id}"
        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)

        if (blockingUntil > now) {
            val remainingMs = blockingUntil - now
            val remainingMin = remainingMs / 60_000L
            return WaitTimerStatus("Blocked for ${remainingMin}m", remainingMs)
        }

        val lastResetKey = "wait_timer_reset_${block.id}"
        val lastReset = kv.decodeLong(lastResetKey, now)

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
            ?: return WaitTimerStatus("${block.waitTimerUseMinutes}m of use left", 0L)

        val packages = block.appPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val usageStats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_BEST,
            lastReset,
            now
        )
        val totalUsageMs = usageStats
            ?.filter { it.packageName in packages }
            ?.sumOf { it.totalTimeInForeground } ?: 0L

        val thresholdMs = block.waitTimerUseMinutes * 60_000L
        val remainingUseMs = (thresholdMs - totalUsageMs).coerceAtLeast(0L)
        val remainingUseMin = remainingUseMs / 60_000L

        return if (remainingUseMs <= 0L) {
            WaitTimerStatus("Block pending", 0L)
        } else {
            WaitTimerStatus("${remainingUseMin}m of use left", 0L)
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%d:%02d", minutes, seconds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelTimer()
    }
}
