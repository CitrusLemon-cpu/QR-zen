package com.qrzen.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.ui.main.MainActivity
import com.qrzen.app.ui.permission.PermissionActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val next = if (Prefs.onboardingComplete) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, PermissionActivity::class.java)
        }
        startActivity(next)
        finish()
    }
}
