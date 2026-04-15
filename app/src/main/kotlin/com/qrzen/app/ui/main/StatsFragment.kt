package com.qrzen.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter()
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentEvents.collect { events ->
                    updateStats(events)
                    adapter.submitList(events)
                    binding.tvEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun updateStats(events: List<BlockEvent>) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEvents = events.filter { it.timestamp >= todayStart }
        val blockedToday = todayEvents.count { it.eventType == "BLOCKED" }
        val pausedToday = todayEvents.count { it.eventType == "PAUSED" }

        binding.tvBlockedCount.text = blockedToday.toString()
        binding.tvPausedCount.text = pausedToday.toString()

        val topApp = events
            .filter { it.eventType == "BLOCKED" }
            .groupBy { it.packageName }
            .maxByOrNull { it.value.size }
        binding.tvTopApp.text = topApp?.key?.substringAfterLast('.') ?: "—"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
