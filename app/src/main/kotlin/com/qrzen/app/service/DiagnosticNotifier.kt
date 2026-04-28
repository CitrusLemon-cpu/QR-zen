package com.qrzen.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.prefs.Prefs

object DiagnosticNotifier {

    private const val CHANNEL_ID = "qrzen_diagnostic"
    private const val POLL_NOTIF_ID = 4999
    private var nextNotifId = 5000

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Block Diagnostics",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Debug info when blocks trigger"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    fun notifyBlockTriggered(
        context: Context,
        source: String,
        detectedPkg: String,
        appLabel: String,
        triggerType: String,
        matchedBlocks: List<AppBlock>,
        cachedImePackages: Set<String>,
        freshImePackages: Set<String>,
        exemptReason: String?,
        extraInfo: String?
    ) {
        if (!Prefs.diagnosticNotifications) return
        ensureChannel(context)

        val blockNames = matchedBlocks.joinToString(", ") { "${it.title}(id=${it.id}, style=${it.blockingStyle})" }
        val isIme = detectedPkg in freshImePackages || detectedPkg in cachedImePackages

        val title = "🔍 Block: $appLabel"
        val lines = buildList {
            add("Pkg: $detectedPkg")
            add("Source: $source | Type: $triggerType")
            add("Matched: $blockNames")
            add("Is IME/keyboard: $isIme")
            add("Cached IMEs: ${cachedImePackages.joinToString(", ")}")
            add("Fresh IMEs: ${freshImePackages.joinToString(", ")}")
            add("IME mismatch: ${if (cachedImePackages != freshImePackages) "YES" else "NO"}")
            if (exemptReason != null) add("Should be exempt: $exemptReason")
            if (extraInfo != null) add(extraInfo)
        }
        val bigText = lines.joinToString("\n")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("$source | $triggerType | $detectedPkg")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(nextNotifId++, notification)
    }

    fun notifyPollState(
        context: Context,
        source: String,
        detectedPkg: String?,
        appLabel: String?,
        cachedImePackages: Set<String>,
        freshImePackages: Set<String>,
        isExempt: Boolean,
        exemptReason: String?,
        activeBlockCount: Int,
        blocklistMatchName: String?,
        allowlistResult: String?
    ) {
        if (!Prefs.diagnosticNotifications) return
        ensureChannel(context)

        val lines = buildList {
            add("Fg: ${appLabel ?: "(none)"} [$detectedPkg]")
            add("Source: $source | Exempt: $isExempt${if (exemptReason != null) " ($exemptReason)" else ""}")
            add("Active blocks: $activeBlockCount")
            add("Is IME: ${detectedPkg != null && (detectedPkg in cachedImePackages || detectedPkg in freshImePackages)}")
            add("Cached IMEs: ${cachedImePackages.joinToString(", ")}")
            add("Fresh IMEs: ${freshImePackages.joinToString(", ")}")
            add("IME mismatch: ${if (cachedImePackages != freshImePackages) "YES" else "NO"}")
            if (blocklistMatchName != null) add("Blocklist hit: $blocklistMatchName")
            if (allowlistResult != null) add("Allowlist: $allowlistResult")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔍 Poll: ${appLabel ?: "idle"}")
            .setContentText("${detectedPkg ?: "no fg"} | blocks=$activeBlockCount")
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(POLL_NOTIF_ID, notification)
    }

    fun cancelPollState(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(POLL_NOTIF_ID)
    }
}
