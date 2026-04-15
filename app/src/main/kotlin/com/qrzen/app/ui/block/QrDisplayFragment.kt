package com.qrzen.app.ui.block

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.king.zxing.util.CodeUtils
import com.qrzen.app.databinding.DialogQrDisplayBinding
import java.io.File
import java.io.FileOutputStream

class QrDisplayFragment : DialogFragment() {

    companion object {
        private const val ARG_SECRET = "arg_secret"

        fun newInstance(secret: String): QrDisplayFragment {
            return QrDisplayFragment().apply {
                arguments = Bundle().apply { putString(ARG_SECRET, secret) }
            }
        }
    }

    private var _binding: DialogQrDisplayBinding? = null
    private val binding get() = _binding!!
    private var qrBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQrDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val secret = arguments?.getString(ARG_SECRET) ?: return

        val bmp = CodeUtils.createQRCode(secret, 512)
        qrBitmap = bmp
        binding.ivQrCode.setImageBitmap(bmp)
        binding.tvSecret.text = secret

        binding.btnShare.setOnClickListener { shareQrBitmap(bmp, secret) }
        binding.btnSave.setOnClickListener { saveQrToDownloads(bmp, secret) }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun shareQrBitmap(bitmap: Bitmap, secret: String) {
        val ctx = requireContext()
        val file = File(ctx.cacheDir, "qrzen_$secret.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share QR Code"))
    }

    private fun saveQrToDownloads(bitmap: Bitmap, secret: String) {
        val fileName = "qrzen_$secret.png"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "image/png")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
                resolver.openOutputStream(uri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            Toast.makeText(requireContext(), "QR code saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
