package com.qrzen.app.data.prefs

import com.qrzen.app.util.PasswordHasher
import com.tencent.mmkv.MMKV

object Prefs {
    const val OVERRIDE_NONE = "none"
    const val OVERRIDE_MASTER_PASSWORD = "master_password"
    const val OVERRIDE_STRICT = "strict"

    private val kv: MMKV get() = MMKV.defaultMMKV()

    var masterPassword: String
        get() = kv.decodeString(KEY_MASTER_PASSWORD, "") ?: ""
        set(v) { kv.encode(KEY_MASTER_PASSWORD, v) }

    var masterPasswordOverrideMode: String
        get() = kv.decodeString(KEY_MASTER_PWD_OVERRIDE_MODE, "") ?: ""
        set(v) { kv.encode(KEY_MASTER_PWD_OVERRIDE_MODE, v) }

    var masterPasswordEnabled: Boolean
        get() = masterPasswordOverrideMode != OVERRIDE_NONE
        set(v) { masterPasswordOverrideMode = if (v) OVERRIDE_MASTER_PASSWORD else OVERRIDE_NONE }

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

    var diagnosticNotifications: Boolean
        get() = kv.decodeBool(KEY_DIAGNOSTIC_NOTIF, false)
        set(v) { kv.encode(KEY_DIAGNOSTIC_NOTIF, v) }

    fun migrateMasterPasswordIfNeeded() {
        val current = masterPassword
        if (current.isNotEmpty() && !PasswordHasher.isHashed(current)) {
            masterPassword = PasswordHasher.hash(current)
        }
    }

    fun migrateMasterPasswordOverrideMode() {
        if (masterPasswordOverrideMode.isNotEmpty()) return
        masterPasswordOverrideMode = if (masterPasswordEnabled && masterPassword.isNotEmpty()) {
            OVERRIDE_MASTER_PASSWORD
        } else {
            OVERRIDE_NONE
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

    fun getScheduleWtUsedMs(blockId: Int): Long {
        return kv.decodeLong("sched_wt_used_$blockId", 0L)
    }

    fun setScheduleWtUsedMs(blockId: Int, ms: Long) {
        kv.encode("sched_wt_used_$blockId", ms)
    }

    fun getScheduleWtBlockingUntil(blockId: Int): Long {
        return kv.decodeLong("sched_wt_blocking_$blockId", 0L)
    }

    fun setScheduleWtBlockingUntil(blockId: Int, until: Long) {
        kv.encode("sched_wt_blocking_$blockId", until)
    }

    fun getScheduleWtLastTick(blockId: Int): Long {
        return kv.decodeLong("sched_wt_last_tick_$blockId", 0L)
    }

    fun setScheduleWtLastTick(blockId: Int, ms: Long) {
        kv.encode("sched_wt_last_tick_$blockId", ms)
    }

    fun clearScheduleWtState(blockId: Int) {
        kv.removeValueForKey("sched_wt_used_$blockId")
        kv.removeValueForKey("sched_wt_blocking_$blockId")
        kv.removeValueForKey("sched_wt_last_tick_$blockId")
    }

    fun clearWaitTimerState(blockId: Int) {
        kv.removeValueForKey("wait_timer_blocking_$blockId")
        kv.removeValueForKey("wait_timer_remaining_$blockId")
        kv.removeValueForKey("wait_timer_last_update_$blockId")
        kv.removeValueForKey("wait_timer_in_app_$blockId")
    }

    fun getSchedAllowanceRemaining(blockId: Int): Long {
        return kv.decodeLong("sched_allow_remaining_$blockId", -1L)
    }

    fun setSchedAllowanceRemaining(blockId: Int, ms: Long) {
        kv.encode("sched_allow_remaining_$blockId", ms)
    }

    fun getSchedAllowanceWindowStart(blockId: Int): Long {
        return kv.decodeLong("sched_allow_window_$blockId", 0L)
    }

    fun setSchedAllowanceWindowStart(blockId: Int, ms: Long) {
        kv.encode("sched_allow_window_$blockId", ms)
    }

    fun getSchedAllowanceLastTick(blockId: Int): Long {
        return kv.decodeLong("sched_allow_tick_$blockId", 0L)
    }

    fun setSchedAllowanceLastTick(blockId: Int, ms: Long) {
        kv.encode("sched_allow_tick_$blockId", ms)
    }

    fun clearSchedAllowanceState(blockId: Int) {
        kv.removeValueForKey("sched_allow_remaining_$blockId")
        kv.removeValueForKey("sched_allow_window_$blockId")
        kv.removeValueForKey("sched_allow_tick_$blockId")
    }

    fun getAppTimerRemaining(blockId: Int, packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_$packageName", -1L)
    }

    fun setAppTimerRemaining(blockId: Int, packageName: String, remainingMs: Long) {
        kv.encode("${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_$packageName", remainingMs)
    }

    fun getAppTimerOriginal(blockId: Int, packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_ORIGINAL_PREFIX}${blockId}_$packageName", -1L)
    }

    fun setAppTimerOriginal(blockId: Int, packageName: String, durationMs: Long) {
        if (durationMs <= 0L) {
            kv.removeValueForKey("${KEY_APP_TIMER_ORIGINAL_PREFIX}${blockId}_$packageName")
        } else {
            kv.encode("${KEY_APP_TIMER_ORIGINAL_PREFIX}${blockId}_$packageName", durationMs)
        }
    }

    fun getAppTimerLastFg(blockId: Int, packageName: String): Long {
        return kv.decodeLong("${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_$packageName", 0L)
    }

    fun setAppTimerLastFg(blockId: Int, packageName: String, timestamp: Long) {
        kv.encode("${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_$packageName", timestamp)
    }

    fun isAppTimerExpired(blockId: Int, packageName: String): Boolean {
        val remaining = getAppTimerRemaining(blockId, packageName)
        return remaining == 0L
    }

    fun getAppTimerExpiry(blockId: Int, packageName: String): Long {
        val remaining = getAppTimerRemaining(blockId, packageName)
        if (remaining > 0L) return System.currentTimeMillis() + remaining
        if (remaining == 0L) return 1L
        return 0L
    }

    fun setAppTimerExpiry(blockId: Int, packageName: String, expiryMillis: Long) {
        if (expiryMillis <= 0L) {
            kv.removeValueForKey("${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_$packageName")
            kv.removeValueForKey("${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_$packageName")
        } else {
            val remaining = (expiryMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            kv.encode("${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_$packageName", remaining)
            kv.encode("${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_$packageName", 0L)
        }
    }

    fun getAppTimerWindowStart(blockId: Int): Long {
        return kv.decodeLong("${KEY_APP_TIMER_WINDOW_PREFIX}$blockId", 0L)
    }

    fun setAppTimerWindowStart(blockId: Int, ms: Long) {
        kv.encode("${KEY_APP_TIMER_WINDOW_PREFIX}$blockId", ms)
    }

    fun resetAppTimersForBlock(blockId: Int) {
        val allKeys = kv.allKeys() ?: return
        val originalPrefix = "${KEY_APP_TIMER_ORIGINAL_PREFIX}${blockId}_"
        val remainingPrefix = "${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_"
        val lastFgPrefix = "${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_"

        val resetPackages = mutableSetOf<String>()
        allKeys.filter { it.startsWith(originalPrefix) }.forEach { key ->
            val packageName = key.removePrefix(originalPrefix)
            val originalDuration = kv.decodeLong(key, -1L)
            if (originalDuration > 0L) {
                setAppTimerRemaining(blockId, packageName, originalDuration)
                setAppTimerLastFg(blockId, packageName, 0L)
                resetPackages.add(packageName)
            }
        }

        allKeys.filter { it.startsWith(remainingPrefix) }.forEach { key ->
            val packageName = key.removePrefix(remainingPrefix)
            if (packageName !in resetPackages) {
                val remaining = kv.decodeLong(key, -1L)
                if (remaining == 0L) {
                    kv.removeValueForKey(key)
                    kv.removeValueForKey("${lastFgPrefix}$packageName")
                }
            }
        }
    }

    fun clearAppTimersForBlock(blockId: Int) {
        val allKeys = kv.allKeys() ?: return
        allKeys.filter {
            it.startsWith("${KEY_APP_TIMER_REMAINING_PREFIX}${blockId}_") ||
                it.startsWith("${KEY_APP_TIMER_LAST_FG_PREFIX}${blockId}_") ||
                it.startsWith("${KEY_APP_TIMER_ORIGINAL_PREFIX}${blockId}_")
        }.forEach { kv.removeValueForKey(it) }
        kv.removeValueForKey("${KEY_APP_TIMER_WINDOW_PREFIX}$blockId")
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
                    it.startsWith(KEY_APP_TIMER_LAST_FG_PREFIX) ||
                    it.startsWith(KEY_APP_TIMER_ORIGINAL_PREFIX) ||
                    it.startsWith(KEY_APP_TIMER_WINDOW_PREFIX)
            }
            .forEach { kv.removeValueForKey(it) }
    }

    private const val KEY_MASTER_PASSWORD = "qrzen_master_pwd"
    private const val KEY_MASTER_PWD_OVERRIDE_MODE = "qrzen_master_pwd_override_mode"
    private const val KEY_PAUSE_ALL_UNTIL = "qrzen_pause_all_until"
    private const val KEY_ONBOARDING_DONE = "qrzen_onboarding_done"
    private const val KEY_REMOVE_NOTIF = "qrzen_remove_notif"
    private const val KEY_SILENT = "qrzen_silent"
    private const val KEY_DIAGNOSTIC_NOTIF = "qrzen_diagnostic_notif"
    private const val KEY_APP_TIMER_PREFIX = "qrzen_app_timer_"
    private const val KEY_APP_TIMER_REMAINING_PREFIX = "qrzen_app_timer_remaining_"
    private const val KEY_APP_TIMER_LAST_FG_PREFIX = "qrzen_app_timer_last_fg_"
    private const val KEY_APP_TIMER_ORIGINAL_PREFIX = "qrzen_app_timer_original_"
    private const val KEY_APP_TIMER_WINDOW_PREFIX = "app_timer_window_"
    private const val KEY_USAGE_REMAINING_PREFIX = "allowlist_usage_remaining_"
    private const val KEY_USAGE_LAST_FG_PREFIX = "allowlist_usage_last_fg_"
}
