package com.qrzen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qrzen.app.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PackageInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

        val packageName = intent.data?.schemeSpecificPart ?: return
        if (packageName == context.packageName) return

        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val dao = entryPoint.appBlockDao()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val blocks = dao.getAutoAddNewAppsBlocks()
                for (block in blocks) {
                    val existingPackages = block.appPackages
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()

                    if (packageName !in existingPackages) {
                        val updatedPackages = if (block.appPackages.isBlank()) {
                            packageName
                        } else {
                            "${block.appPackages},$packageName"
                        }
                        dao.update(block.copy(appPackages = updatedPackages))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
