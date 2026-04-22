package com.qrzen.app.ui.main

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.ItemBlockBinding
import com.qrzen.app.ui.unlock.UnlockMethodUtils

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

    inner class ViewHolder(val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        private var countDownTimer: CountDownTimer? = null

        fun bind(block: AppBlock) {
            countDownTimer?.cancel()
            countDownTimer = null

            binding.tvTitle.text = block.title
            binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
            val modePrefix = if (block.isAllowlistMode) "Allowlist" else "Blocklist"
            binding.tvDays.text = "$modePrefix · ${UnlockMethodUtils.formatDays(block.activeDays)}"

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
