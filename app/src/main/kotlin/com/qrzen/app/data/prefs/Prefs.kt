package com.qrzen.app.data.prefs

import com.qrzen.app.util.PasswordHasher
import com.tencent.mmkv.MMKV

object Prefs {
    private val kv: MMKV get() = MMKV.defaultMMKV()

    var masterPassword: String
        get() = kv.decodeString(KEY_MASTER_PASSWORD, "") ?: ""
        set(v) { kv.encode(KEY_MASTER_PASSWORD, v) }

    var masterPasswordEnabled: Boolean
        get() = kv.decodeBool(KEY_MASTER_PWD_ENABLED, false)
        set(v) { kv.encode(KEY_MASTER_PWD_ENABLED, v) }

    /** Epoch millis until which all blocks are paused via master password override */
    var pauseAllUntil: Long
        get() = kv.decodeLong(KEY_PAUSE_ALL_UNTIL, 0L)
        set(v) { kv.encode(KEY_PAUSE_ALL_UNTIL, v) }

    var onboardingComplete: Boolean
        get() = kv.decodeBool(KEY_ONBOARDING_DONE, false)
        set(v) { kv.encode(KEY_ONBOARDING_DONE, v) }

    var removeNotifications: Boolean
        get() = kv.decodeBool(KEY_REMOVE_NOTIF, false)
        set(v) { kv.encode(KEY_REMOVE_NOTIF, v) }

    var silentMode: Boolean
        get() = kv.decodeBool(KEY_SILENT, false)
        set(v) { kv.encode(KEY_SILENT, v) }

    fun migrateMasterPasswordIfNeeded() {
        val current = masterPassword
        if (current.isNotEmpty() && !PasswordHasher.isHashed(current)) {
            masterPassword = PasswordHasher.hash(current)
        }
    }

    fun getAppTimerExpiry(packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_PREFIX}$packageName", 0L)
    }

    fun setAppTimerExpiry(packageName: String, expiryMillis: Long) {
        if (expiryMillis <= 0L) {
            kv.removeValueForKey("${KEY_APP_TIMER_PREFIX}$packageName")
        } else {
            kv.encode("${KEY_APP_TIMER_PREFIX}$packageName", expiryMillis)
        }
    }

    fun isAppTimerExpired(packageName: String): Boolean {
        val expiry = getAppTimerExpiry(packageName)
        return expiry > 0L && System.currentTimeMillis() >= expiry
    }

    fun clearAllAppTimers() {
        val allKeys = kv.allKeys() ?: return
        allKeys
            .filter { it.startsWith(KEY_APP_TIMER_PREFIX) }
            .forEach { kv.removeValueForKey(it) }
    }

    private const val KEY_MASTER_PASSWORD = "qrzen_master_pwd"
    private const val KEY_MASTER_PWD_ENABLED = "qrzen_master_pwd_enabled"
    private const val KEY_PAUSE_ALL_UNTIL = "qrzen_pause_all_until"
    private const val KEY_ONBOARDING_DONE = "qrzen_onboarding_done"
    private const val KEY_REMOVE_NOTIF = "qrzen_remove_notif"
    private const val KEY_SILENT = "qrzen_silent"
    private const val KEY_APP_TIMER_PREFIX = "qrzen_app_timer_"
}
