package com.qrzen.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.ItemBlockBinding

class BlockAdapter(
    private val onToggle: (AppBlock, Boolean) -> Unit,
    private val onLongPress: (AppBlock) -> Unit
) : ListAdapter<AppBlock, BlockAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppBlock>() {
            override fun areItemsTheSame(a: AppBlock, b: AppBlock) = a.id == b.id
            override fun areContentsTheSame(a: AppBlock, b: AppBlock) = a == b
        }
    }

    inner class ViewHolder(val binding: ItemBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(block: AppBlock) {
            binding.tvTitle.text = block.title
            binding.tvTimeRange.text = "${block.startTime} \u2013 ${block.endTime}"
            binding.tvDays.text = formatDays(block.activeDays)
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = block.isEnabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(block, checked)
            }
            binding.root.setOnLongClickListener {
                onLongPress(block)
                true
            }
        }
    }

    private fun formatDays(activeDays: String): String {
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val active = activeDays.mapIndexedNotNull { i, c ->
            if (c == '1') names.getOrNull(i) else null
        }
        return if (active.isEmpty()) "No days" else active.joinToString(", ")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
