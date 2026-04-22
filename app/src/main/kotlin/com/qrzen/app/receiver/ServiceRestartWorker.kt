package com.qrzen.app.receiver

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.qrzen.app.service.BackgroundService
import java.util.concurrent.TimeUnit

class ServiceRestartWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        BackgroundService.start(applicationContext)
        AlarmKeepaliveReceiver.schedule(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "qrzen_service_restart"

        fun ensureScheduled(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceRestartWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
