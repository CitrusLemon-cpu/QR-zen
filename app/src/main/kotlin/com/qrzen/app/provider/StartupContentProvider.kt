package com.qrzen.app.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.qrzen.app.service.BackgroundService

/**
 * Initialized by Android at app startup with initOrder=100, which means
 * this runs BEFORE Application.onCreate() and BEFORE Hilt DI is set up.
 *
 * This is the second key ultra-battery-saver survival mechanism: the service
 * is started at the earliest possible point in the app's process lifecycle,
 * even on a fresh boot when the system tries to start the app quickly.
 * Combined with RebootReceiver's 25-broadcast coverage, this ensures QR Zen
 * can resume enforcing blocks immediately after a reboot or kill.
 */
class StartupContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let { BackgroundService.start(it) }
        return true
    }

    override fun query(uri: Uri, p: Array<String>?, s: String?, sA: Array<String>?, so: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, sA: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, s: String?, sA: Array<String>?): Int = 0
}
