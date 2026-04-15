package com.qrzen.app

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QrZenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
