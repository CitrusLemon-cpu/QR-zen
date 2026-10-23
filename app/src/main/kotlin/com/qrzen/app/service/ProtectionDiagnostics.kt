package com.qrzen.app.service

import com.tencent.mmkv.MMKV
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProtectionDiagnostics {
    private const val KEY = "protection_diagnostics_log"
    private const val MAX_LINES = 120

    @Synchronized
    fun record(message: String) {
        val kv = MMKV.defaultMMKV()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val existing = kv.decodeString(KEY, "").orEmpty().lineSequence().filter { it.isNotBlank() }.toList()
        val updated = (existing + "$timestamp  $message").takeLast(MAX_LINES)
        kv.encode(KEY, updated.joinToString("\n"))
    }

    fun read(): String = MMKV.defaultMMKV().decodeString(KEY, "").orEmpty()
}
