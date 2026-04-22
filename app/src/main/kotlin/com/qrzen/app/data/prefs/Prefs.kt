package com.qrzen.app.data.prefs

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

    private const val KEY_MASTER_PASSWORD = "qrzen_master_pwd"
    private const val KEY_MASTER_PWD_ENABLED = "qrzen_master_pwd_enabled"
    private const val KEY_PAUSE_ALL_UNTIL = "qrzen_pause_all_until"
    private const val KEY_ONBOARDING_DONE = "qrzen_onboarding_done"
    private const val KEY_REMOVE_NOTIF = "qrzen_remove_notif"
    private const val KEY_SILENT = "qrzen_silent"
}
