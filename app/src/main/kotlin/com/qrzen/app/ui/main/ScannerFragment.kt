package com.qrzen.app.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.king.zxing.CameraScan
import com.qrzen.app.R
import com.qrzen.app.databinding.FragmentScannerBinding
import com.qrzen.app.ui.lock.QrScanActivity

class ScannerFragment : Fragment() {
    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private var scannedContent: String? = null

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val content = result.data?.getStringExtra(CameraScan.SCAN_RESULT)
            if (!content.isNullOrBlank()) {
                scannedContent = content
                renderResult()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scannedContent = savedInstanceState?.getString(STATE_SCANNED_CONTENT)
        binding.btnOpenScanner.setOnClickListener { openScanner() }
        binding.btnScanAgain.setOnClickListener { openScanner() }
        binding.btnCopyResult.setOnClickListener { copyResult() }
        renderResult()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SCANNED_CONTENT, scannedContent)
    }

    private fun openScanner() {
        scanLauncher.launch(Intent(requireContext(), QrScanActivity::class.java))
    }

    private fun copyResult() {
        val content = scannedContent ?: return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.scanner_result_label), content))
        Toast.makeText(requireContext(), R.string.scanner_copied, Toast.LENGTH_SHORT).show()
    }

    private fun renderResult() {
        val content = scannedContent
        binding.resultCard.visibility = if (content == null) View.GONE else View.VISIBLE
        binding.btnOpenScanner.visibility = if (content == null) View.VISIBLE else View.GONE
        binding.tvScanResult.text = content.orEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val STATE_SCANNED_CONTENT = "scanned_content"
    }
}
