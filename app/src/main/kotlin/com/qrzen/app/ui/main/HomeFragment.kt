package com.qrzen.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.databinding.FragmentHomeBinding
import com.qrzen.app.ui.block.EditBlockActivity
import com.qrzen.app.ui.block.QrDisplayFragment
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
            startActivity(Intent(requireContext(), EditBlockActivity::class.java))
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
    }

    private fun showBlockOptions(block: AppBlock) {
        val options = arrayOf("Edit", "Delete", "Show QR Code")
        AlertDialog.Builder(requireContext())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(requireContext(), EditBlockActivity::class.java).apply {
                            putExtra(EditBlockActivity.EXTRA_BLOCK_ID, block.id)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Block")
                            .setMessage("Delete '${block.title}'? This cannot be undone.")
                            .setPositiveButton("Delete") { _, _ -> viewModel.delete(block) }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    2 -> {
                        QrDisplayFragment.newInstance(block.qrSecret)
                            .show(childFragmentManager, "qr_display")
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
