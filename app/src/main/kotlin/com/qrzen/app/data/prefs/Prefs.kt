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

    fun getAllowlistUsageRemaining(blockId: Int): Long {
        return kv.decodeLong("${KEY_USAGE_REMAINING_PREFIX}$blockId", -1L)
    }

    fun setAllowlistUsageRemaining(blockId: Int, remainingMs: Long) {
        kv.encode("${KEY_USAGE_REMAINING_PREFIX}$blockId", remainingMs)
    }

    fun getAllowlistUsageLastFg(blockId: Int): Long {
        return kv.decodeLong("${KEY_USAGE_LAST_FG_PREFIX}$blockId", 0L)
    }

    fun setAllowlistUsageLastFg(blockId: Int, timestamp: Long) {
        kv.encode("${KEY_USAGE_LAST_FG_PREFIX}$blockId", timestamp)
    }

    fun hasAllowlistUsageTimer(blockId: Int): Boolean {
        return getAllowlistUsageRemaining(blockId) >= 0L
    }

    fun clearAllowlistUsageTimer(blockId: Int) {
        kv.removeValueForKey("${KEY_USAGE_REMAINING_PREFIX}$blockId")
        kv.removeValueForKey("${KEY_USAGE_LAST_FG_PREFIX}$blockId")
    }

    fun getAppTimerExpiry(packageName: String): Long {
        val now = System.currentTimeMillis()
        val remaining = getAppTimerRemaining(packageName)
        if (remaining > 0L) return now + remaining
        if (remaining == 0L) return 1L
        val oldExpiry = kv.decodeLong("${KEY_APP_TIMER_PREFIX}$packageName", 0L)
        if (oldExpiry > 0L) {
            val migratedRemaining = (oldExpiry - now).coerceAtLeast(0L)
            kv.encode("${KEY_APP_TIMER_REMAINING_PREFIX}$packageName", migratedRemaining)
            kv.removeValueForKey("${KEY_APP_TIMER_PREFIX}$packageName")
            return if (migratedRemaining > 0L) System.currentTimeMillis() + migratedRemaining else 1L
        }
        return 0L
    }

    fun setAppTimerExpiry(packageName: String, expiryMillis: Long) {
        if (expiryMillis <= 0L) {
            kv.removeValueForKey("${KEY_APP_TIMER_PREFIX}$packageName")
            kv.removeValueForKey("${KEY_APP_TIMER_REMAINING_PREFIX}$packageName")
            kv.removeValueForKey("${KEY_APP_TIMER_LAST_FG_PREFIX}$packageName")
        } else {
            val remaining = (expiryMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            kv.encode("${KEY_APP_TIMER_REMAINING_PREFIX}$packageName", remaining)
            kv.encode("${KEY_APP_TIMER_LAST_FG_PREFIX}$packageName", 0L)
            kv.removeValueForKey("${KEY_APP_TIMER_PREFIX}$packageName")
        }
    }

    fun getAppTimerRemaining(packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_REMAINING_PREFIX}$packageName", -1L)
    }

    fun setAppTimerRemaining(packageName: String, remainingMs: Long) {
        kv.encode("${KEY_APP_TIMER_REMAINING_PREFIX}$packageName", remainingMs)
    }

    fun getAppTimerLastFg(packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_LAST_FG_PREFIX}$packageName", 0L)
    }

    fun setAppTimerLastFg(packageName: String, timestamp: Long) {
        kv.encode("${KEY_APP_TIMER_LAST_FG_PREFIX}$packageName", timestamp)
    }

    fun isAppTimerExpired(packageName: String): Boolean {
        val remaining = getAppTimerRemaining(packageName)
        if (remaining == 0L) return true
        if (remaining > 0L) return false
        val expiry = kv.decodeLong("${KEY_APP_TIMER_PREFIX}$packageName", 0L)
        return expiry > 0L && System.currentTimeMillis() >= expiry
    }

    fun clearAllAppTimers() {
        val allKeys = kv.allKeys() ?: return
        allKeys
            .filter {
                it.startsWith(KEY_APP_TIMER_PREFIX) ||
                    it.startsWith(KEY_APP_TIMER_REMAINING_PREFIX) ||
                    it.startsWith(KEY_APP_TIMER_LAST_FG_PREFIX)
            }
            .forEach { kv.removeValueForKey(it) }
    }

    private const val KEY_MASTER_PASSWORD = "qrzen_master_pwd"
    private const val KEY_MASTER_PWD_ENABLED = "qrzen_master_pwd_enabled"
    private const val KEY_PAUSE_ALL_UNTIL = "qrzen_pause_all_until"
    private const val KEY_ONBOARDING_DONE = "qrzen_onboarding_done"
    private const val KEY_REMOVE_NOTIF = "qrzen_remove_notif"
    private const val KEY_SILENT = "qrzen_silent"
    private const val KEY_APP_TIMER_PREFIX = "qrzen_app_timer_"
    private const val KEY_APP_TIMER_REMAINING_PREFIX = "qrzen_app_timer_remaining_"
    private const val KEY_APP_TIMER_LAST_FG_PREFIX = "qrzen_app_timer_last_fg_"
    private const val KEY_USAGE_REMAINING_PREFIX = "allowlist_usage_remaining_"
    private const val KEY_USAGE_LAST_FG_PREFIX = "allowlist_usage_last_fg_"
}
