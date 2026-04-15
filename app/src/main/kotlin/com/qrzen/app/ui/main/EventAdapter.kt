package com.qrzen.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter : ListAdapter<BlockEvent, EventAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BlockEvent>() {
            override fun areItemsTheSame(a: BlockEvent, b: BlockEvent) = a.id == b.id
            override fun areContentsTheSame(a: BlockEvent, b: BlockEvent) = a == b
        }
        private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
    }

    inner class ViewHolder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: BlockEvent) {
            val date = Date(event.timestamp)
            binding.tvTime.text = "${dateFmt.format(date)} ${timeFmt.format(date)}"
            binding.tvTitle.text = event.blockTitle
            binding.tvType.text = event.eventType
            binding.tvType.setTextColor(
                if (event.eventType == "BLOCKED")
                    binding.root.context.getColor(android.R.color.holo_red_light)
                else
                    binding.root.context.getColor(android.R.color.holo_green_dark)
            )
            binding.tvPackage.text = event.packageName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
