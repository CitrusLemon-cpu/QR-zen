package com.qrzen.app.ui.main

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qrzen.app.R
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.databinding.ItemBlockBinding
import com.qrzen.app.databinding.ItemFolderBinding
import com.qrzen.app.databinding.ItemSelectedAppIconBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class HomeListItem {
    data class FolderHeader(val folder: BlockFolder, val blockCount: Int) : HomeListItem()
    data class BlockItem(val block: AppBlock, val isInFolder: Boolean) : HomeListItem()
}

class HomeListAdapter(
    private val timeBlockDao: TimeBlockDao,
    private val onToggle: (AppBlock, Boolean) -> Boolean,
    private val onPause: (AppBlock) -> Unit,
    private val onBlockNow: (AppBlock) -> Unit,
    private val onEdit: (AppBlock) -> Unit,
    private val onArchive: (AppBlock) -> Unit,
    private val onDelete: (AppBlock) -> Unit,
    private val onRestartFromPause: (AppBlock) -> Unit,
    private val onLockWithTimer: (AppBlock) -> Unit,
    private val onMoveToFolder: (AppBlock) -> Unit,
    private val onFolderToggle: (BlockFolder, Boolean) -> Boolean,
    private val onFolderExpandCollapse: (BlockFolder) -> Unit,
    private val onFolderEdit: (BlockFolder) -> Unit,
    private val onFolderPause: (BlockFolder) -> Unit,
    private val onFolderDelete: (BlockFolder) -> Unit
) : ListAdapter<HomeListItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VIEW_TYPE_FOLDER = 1
        private const val VIEW_TYPE_BLOCK = 2

        val DIFF = object : DiffUtil.ItemCallback<HomeListItem>() {
            override fun areItemsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
                return when {
                    oldItem is HomeListItem.FolderHeader && newItem is HomeListItem.FolderHeader ->
                        oldItem.folder.id == newItem.folder.id
                    oldItem is HomeListItem.BlockItem && newItem is HomeListItem.BlockItem ->
                        oldItem.block.id == newItem.block.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
                return oldItem == newItem
            }
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

    inner class FolderViewHolder(private val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root) {
        private var pauseTimer: CountDownTimer? = null

        fun bind(item: HomeListItem.FolderHeader) {
            val folder = item.folder
            pauseTimer?.cancel()
            pauseTimer = null

            binding.tvFolderTitle.text = folder.title
            binding.tvFolderInfo.text = buildFolderInfo(binding.root.context, folder, item.blockCount)
            binding.ivFolderLock.visibility = if (
                folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE } == UnlockMethodUtils.METHOD_NONE
            ) {
                View.GONE
            } else {
                View.VISIBLE
            }
            binding.ivExpandCollapse.setImageResource(
                if (folder.isCollapsed) R.drawable.ic_expand_more else R.drawable.ic_expand_less
            )

            setupPauseTimer(folder)

            binding.switchFolderEnabled.setOnCheckedChangeListener(null)
            binding.switchFolderEnabled.isChecked = folder.isEnabled
            bindFolderToggleListener(folder)

            binding.root.setOnClickListener { onFolderExpandCollapse(folder) }
            binding.ivExpandCollapse.setOnClickListener { onFolderExpandCollapse(folder) }
            binding.btnFolderOverflow.setOnClickListener { showFolderPopupMenu(it, folder) }
        }

        private fun bindFolderToggleListener(folder: BlockFolder) {
            binding.switchFolderEnabled.setOnCheckedChangeListener { _, isChecked ->
                val accepted = onFolderToggle(folder, isChecked)
                if (!accepted) {
                    binding.switchFolderEnabled.setOnCheckedChangeListener(null)
                    binding.switchFolderEnabled.isChecked = folder.isEnabled
                    bindFolderToggleListener(folder)
                }
            }
        }

        private fun setupPauseTimer(folder: BlockFolder) {
            val now = System.currentTimeMillis()
            if (folder.pausedUntil == Long.MAX_VALUE) {
                binding.tvFolderPauseTimer.visibility = View.VISIBLE
                binding.tvFolderPauseTimer.text = "⏸ Paused indefinitely"
                return
            }
            if (folder.pausedUntil > now) {
                val remaining = folder.pausedUntil - now
                binding.tvFolderPauseTimer.visibility = View.VISIBLE
                binding.tvFolderPauseTimer.text = "⏸ Paused – ${formatDuration(remaining)} remaining"
                pauseTimer = object : CountDownTimer(remaining, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvFolderPauseTimer.text = "⏸ Paused – ${formatDuration(millisUntilFinished)} remaining"
                    }

                    override fun onFinish() {
                        binding.tvFolderPauseTimer.visibility = View.GONE
                    }
                }.start()
            } else {
                binding.tvFolderPauseTimer.visibility = View.GONE
            }
        }

        private fun showFolderPopupMenu(anchor: View, folder: BlockFolder) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_folder_overflow, popup.menu)
            popup.menu.findItem(R.id.action_pause_folder)?.title = if (folder.pausedUntil > System.currentTimeMillis()) {
                "Unpause"
            } else {
                "Pause"
            }
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_pause_folder -> {
                        onFolderPause(folder)
                        true
                    }
                    R.id.action_edit_folder -> {
                        onFolderEdit(folder)
                        true
                    }
                    R.id.action_delete_folder -> {
                        onFolderDelete(folder)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        fun cancelTimer() {
            pauseTimer?.cancel()
            pauseTimer = null
        }
    }

    inner class BlockViewHolder(private val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var countDownTimer: CountDownTimer? = null
        private var blockNowTimer: CountDownTimer? = null
        private var lockTimer: CountDownTimer? = null
        private var activeTimer: CountDownTimer? = null
        private var pomodoroTimer: CountDownTimer? = null
        private var usageStatusTimer: CountDownTimer? = null
        private var iconLoadJob: Job? = null
        private var usageQueryJob: Job? = null
        private var boundBlockId: Int? = null
        private var boundPackages: List<String> = emptyList()

        fun bind(item: HomeListItem.BlockItem) {
            val block = item.block

            countDownTimer?.cancel()
            countDownTimer = null
            blockNowTimer?.cancel()
            blockNowTimer = null
            lockTimer?.cancel()
            lockTimer = null
            activeTimer?.cancel()
            activeTimer = null
            pomodoroTimer?.cancel()
            pomodoroTimer = null
            usageStatusTimer?.cancel()
            usageStatusTimer = null
            iconLoadJob?.cancel()
            iconLoadJob = null
            usageQueryJob?.cancel()
            usageQueryJob = null

            updateIndent(item.isInFolder)

            boundBlockId = block.id
            binding.tvTitle.text = block.title
            val modePrefix = if (block.isAllowlistMode) "Allowlist" else "Blocklist"
            when (block.blockingStyle) {
                UnlockMethodUtils.STYLE_MANUAL -> {
                    binding.tvTimeRange.text = "Manual"
                    binding.tvDays.text = modePrefix
                }
                UnlockMethodUtils.STYLE_SCHEDULE -> {
                    val timeRangeText = when (block.scheduleBreakType) {
                        UnlockMethodUtils.BREAK_POMODORO -> {
                            usageQueryJob = scope.launch {
                                val timeBlocks = withContext(Dispatchers.IO) {
                                    timeBlockDao.getByBlockId(block.id)
                                }
                                bindSchedulePomodoroPhase(block, timeBlocks)
                            }
                            "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
                        }
                        UnlockMethodUtils.BREAK_WAIT_TIMER -> {
                            val adaptiveSuffix = if (block.waitTimerAdaptive) " · adaptive" else ""
                            "Scheduled · Wait Timer (${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block$adaptiveSuffix)"
                        }
                        UnlockMethodUtils.BREAK_USAGE_LIMIT -> {
                            val period = if (block.usageLimitPeriod == "HOURLY") "hour" else "day"
                            "Scheduled · Usage Limit (${block.usageLimitMinutes} min/$period)"
                        }
                        UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE ->
                            "Scheduled · Allowance (${block.scheduledAllowanceMinutes} min/window)"
                        else -> "Scheduled"
                    }
                    binding.tvTimeRange.text = timeRangeText
                    binding.tvDays.text = modePrefix
                }
                UnlockMethodUtils.STYLE_USAGE_LIMIT -> {
                    val period = if (block.usageLimitPeriod == "HOURLY") "per hour" else "per day"
                    binding.tvTimeRange.text = "${block.usageLimitMinutes} min $period"
                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"

                    usageQueryJob = scope.launch {
                        val remainingText = withContext(Dispatchers.IO) {
                            computeUsageLimitRemaining(binding.root.context, block)
                        }
                        binding.tvTimeRange.text = remainingText
                    }
                }
                UnlockMethodUtils.STYLE_WAIT_TIMER -> {
                    val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
                    binding.tvTimeRange.text = "${block.waitTimerUseMinutes}m use / ${block.waitTimerWaitMinutes}m block ($modeLabel)"
                    binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"

                    usageQueryJob = scope.launch {
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
                                    binding.tvTimeRange.text = computeWaitTimerStatus(binding.root.context, block).text
                                }
                            }.start()
                        }
                    }
                }
                UnlockMethodUtils.STYLE_POMODORO -> {
                    val state = UnlockMethodUtils.computePomodoroState(block)
                    if (state.isSessionActive) {
                        if (state.isInFocus) {
                            binding.tvTimeRange.text = "🎯 Focus ${state.currentRound}/${state.totalRounds}"
                        } else {
                            binding.tvTimeRange.text = "☕ Break ${state.currentRound}/${state.totalRounds}"
                        }
                    } else {
                        binding.tvTimeRange.text = "${block.pomodoroDurationMin}m focus / ${block.pomodoroBreakMin}m break"
                    }
                    binding.tvDays.text = modePrefix
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
            setupLockTimer(block)
            setupActiveTimer(block)
            setupPomodoroTimer(block)

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

            val pm = binding.root.context.packageManager
            iconLoadJob = scope.launch {
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

        private fun updateIndent(isInFolder: Boolean) {
            val layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)
                ?: RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.marginStart = dpToPx(if (isInFolder) 24 else 8)
            layoutParams.marginEnd = dpToPx(8)
            binding.root.layoutParams = layoutParams
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

        private fun setupLockTimer(block: AppBlock) {
            val now = System.currentTimeMillis()
            if (block.toggleLockUntil > now) {
                binding.tvLockTimer.visibility = View.VISIBLE
                val remaining = block.toggleLockUntil - now
                binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(remaining)}"
                lockTimer = object : CountDownTimer(remaining, 1000L) {
                    override fun onTick(ms: Long) {
                        binding.tvLockTimer.text = "🔒 Locked for ${formatDuration(ms)}"
                    }

                    override fun onFinish() {
                        binding.tvLockTimer.visibility = View.GONE
                    }
                }.start()
            } else {
                binding.tvLockTimer.visibility = View.GONE
            }
        }

        private fun setupActiveTimer(block: AppBlock) {
            val now = System.currentTimeMillis()
            if (block.activeUntil > now) {
                binding.tvActiveTimer.visibility = View.VISIBLE
                val remaining = block.activeUntil - now
                val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                val endsAt = sdf.format(java.util.Date(block.activeUntil))
                binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(remaining)} (until $endsAt)"
                activeTimer = object : CountDownTimer(remaining, 1000L) {
                    override fun onTick(ms: Long) {
                        binding.tvActiveTimer.text = "⏱ Active for ${formatDuration(ms)} (until $endsAt)"
                    }

                    override fun onFinish() {
                        binding.tvActiveTimer.visibility = View.GONE
                    }
                }.start()
            } else {
                binding.tvActiveTimer.visibility = View.GONE
            }
        }

        private fun setupPomodoroTimer(block: AppBlock) {
            if (block.blockingStyle != UnlockMethodUtils.STYLE_POMODORO) {
                return
            }
            val state = UnlockMethodUtils.computePomodoroState(block)
            if (!state.isSessionActive) {
                if (block.blockNowUntil <= System.currentTimeMillis()) {
                    binding.tvBlockNowTimer.visibility = View.GONE
                }
                return
            }
            binding.tvBlockNowTimer.visibility = View.VISIBLE
            val label = if (state.isInFocus) "🎯" else "☕"
            binding.tvBlockNowTimer.text = "$label ${formatDuration(state.periodRemainingMs)} remaining"
            pomodoroTimer = object : CountDownTimer(state.periodRemainingMs, 1000L) {
                override fun onTick(ms: Long) {
                    binding.tvBlockNowTimer.text = "$label ${formatDuration(ms)} remaining"
                }

                override fun onFinish() {
                    binding.tvBlockNowTimer.visibility = View.GONE
                }
            }.start()
        }

        private fun bindSchedulePomodoroPhase(block: AppBlock, timeBlocks: List<TimeBlock>) {
            if (boundBlockId != block.id) return
            val phase = UnlockMethodUtils.computeSchedulePomodoroPhase(block, timeBlocks)
            if (!phase.isActive || phase.phaseRemainingMs <= 0L) {
                binding.tvTimeRange.text = "Scheduled · Pomodoro (${block.pomodoroDurationMin}m/${block.pomodoroBreakMin}m)"
                return
            }

            val phaseEmoji = if (phase.isInFocus) "\uD83C\uDFAF" else "\u2615"
            val phaseWord = if (phase.isInFocus) "Focus" else "Break"
            binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(phase.phaseRemainingMs)}"

            usageStatusTimer?.cancel()
            usageStatusTimer = object : CountDownTimer(phase.phaseRemainingMs, 1000L) {
                override fun onTick(ms: Long) {
                    binding.tvTimeRange.text = "$phaseEmoji $phaseWord ${formatPhaseDuration(ms)}"
                }

                override fun onFinish() {
                    scope.launch {
                        bindSchedulePomodoroPhase(block, timeBlocks)
                    }
                }
            }.start()
        }

        private fun showPopupMenu(anchor: View, block: AppBlock) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_block_overflow, popup.menu)

            val now = System.currentTimeMillis()
            val isPaused = block.pausedUntil > now || block.pausedUntil == Long.MAX_VALUE
            popup.menu.findItem(R.id.action_pause)?.title = if (isPaused) "Unpause" else "Pause"

            val isManualNoMethod = block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL &&
                UnlockMethodUtils.getNormalizedMethod(block) == UnlockMethodUtils.METHOD_NONE

            popup.menu.findItem(R.id.action_pause)?.isVisible = !isManualNoMethod
            popup.menu.findItem(R.id.action_block_now)?.isVisible = !isManualNoMethod
            popup.menu.findItem(R.id.action_lock_with_timer)?.isVisible = isManualNoMethod

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
                    R.id.action_lock_with_timer -> {
                        onLockWithTimer(block)
                        true
                    }
                    R.id.action_move_to_folder -> {
                        onMoveToFolder(block)
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
            lockTimer?.cancel()
            lockTimer = null
            activeTimer?.cancel()
            activeTimer = null
            pomodoroTimer?.cancel()
            pomodoroTimer = null
            usageStatusTimer?.cancel()
            usageStatusTimer = null
            iconLoadJob?.cancel()
            iconLoadJob = null
            usageQueryJob?.cancel()
            usageQueryJob = null
            boundBlockId = null
        }
    }

    private data class WaitTimerStatus(val text: String, val blockingRemainingMs: Long)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeListItem.FolderHeader -> VIEW_TYPE_FOLDER
            is HomeListItem.BlockItem -> VIEW_TYPE_BLOCK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOLDER -> FolderViewHolder(ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> BlockViewHolder(ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeListItem.FolderHeader -> (holder as FolderViewHolder).bind(item)
            is HomeListItem.BlockItem -> (holder as BlockViewHolder).bind(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is FolderViewHolder -> holder.cancelTimer()
            is BlockViewHolder -> holder.cancelTimer()
        }
    }

    private fun buildFolderInfo(context: Context, folder: BlockFolder, blockCount: Int): String {
        val blockCountText = if (blockCount == 1) "1 block" else "$blockCount blocks"
        val lockText = when (folder.unlockMethod.ifBlank { UnlockMethodUtils.METHOD_NONE }) {
            UnlockMethodUtils.METHOD_DELAY -> "${context.getString(R.string.unlock_method_delay)} lock"
            UnlockMethodUtils.METHOD_PASSWORD -> "${context.getString(R.string.unlock_method_password)} lock"
            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> "${context.getString(R.string.unlock_method_type_over)} lock"
            UnlockMethodUtils.METHOD_QR_CODE -> "${context.getString(R.string.unlock_method_qr_code)} lock"
            UnlockMethodUtils.METHOD_EDIT_WINDOW -> "${context.getString(R.string.unlock_method_edit_window)} lock"
            UnlockMethodUtils.METHOD_TIMER -> "${context.getString(R.string.unlock_method_timer)} lock"
            else -> null
        }
        return if (lockText.isNullOrBlank()) blockCountText else "$blockCountText · $lockText"
    }

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
        val events = usageStatsManager.queryEvents(startTime, now)
        val event = android.app.usage.UsageEvents.Event()
        val foregroundStartTimes = mutableMapOf<String, Long>()
        var totalUsageMs = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg !in packages) continue
            when (event.eventType) {
                android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    foregroundStartTimes[pkg] = event.timeStamp
                }
                android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = foregroundStartTimes.remove(pkg)
                    if (start != null) {
                        totalUsageMs += (event.timeStamp - start).coerceAtLeast(0L)
                    }
                }
            }
        }
        for ((_, start) in foregroundStartTimes) {
            totalUsageMs += (now - start).coerceAtLeast(0L)
        }

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

    private fun computeWaitTimerStatus(@Suppress("UNUSED_PARAMETER") context: Context, block: AppBlock): WaitTimerStatus {
        val now = System.currentTimeMillis()
        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
        val blockingUntilKey = "wait_timer_blocking_${block.id}"
        val remainingKey = "wait_timer_remaining_${block.id}"
        val blockingUntil = kv.decodeLong(blockingUntilKey, 0L)

        if (blockingUntil > now) {
            val remainingMs = blockingUntil - now
            val remainingMin = remainingMs / 60_000L
            return WaitTimerStatus("Blocked for ${remainingMin}m", remainingMs)
        }

        val remaining = kv.decodeLong(remainingKey, -1L)
        val maxMs = block.waitTimerUseMinutes * 60_000L
        val actualRemaining = if (remaining < 0L) maxMs else remaining
        val remainingMin = actualRemaining / 60_000L
        val remainingSec = (actualRemaining % 60_000L) / 1000L

        return if (actualRemaining <= 0L) {
            WaitTimerStatus("Block pending", 0L)
        } else {
            val modeLabel = if (block.waitTimerAdaptive) "Adaptive" else "Normal"
            WaitTimerStatus("${remainingMin}m ${remainingSec}s left ($modeLabel)", 0L)
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

    private fun formatPhaseDuration(ms: Long): String {
        val min = ms / 60_000L
        val sec = (ms % 60_000L) / 1000L
        return "${min}m ${sec}s"
    }

    private fun dpToPx(value: Int): Int {
        return (value * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
    }
}
