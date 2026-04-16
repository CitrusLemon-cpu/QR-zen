package com.qrzen.app.ui.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.qrzen.app.R
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityPermissionBinding
import com.qrzen.app.ui.main.MainActivity

/**
 * Permission onboarding. Steps:
 * 0 - Welcome
 * 1 - Accessibility Service
 * 2 - Notification Listener
 * 3 - Notifications (Android 13+ only)
 * 4 - Device Admin
 * 5 - Battery Optimization exemption
 * 6 - Draw Over Other Apps (SYSTEM_ALERT_WINDOW)
 * 7 - Usage Access (PACKAGE_USAGE_STATS)
 */
class PermissionActivity : AppCompatActivity() {
    companion object {
        private const val REQ_POST_NOTIF = 3001
    }

    private lateinit var binding: ActivityPermissionBinding
    private var currentStep = 0

    data class Step(
        val title: String,
        val description: String,
        val grantLabel: String = "Grant Permission",
        val onGrant: PermissionActivity.() -> Unit = {}
    )

    private val steps by lazy {
        buildList {
            add(
                Step(
                "Welcome to QR Zen",
                "QR Zen helps you stay focused by blocking distracting apps. A few permissions are needed to make everything work. Tap Next to continue.",
                grantLabel = "Next"
            ))
            add(
                Step(
                "Accessibility Access",
                "QR Zen uses Accessibility Service to detect when a blocked app is in the foreground and show the block overlay.",
                onGrant = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            ))
            add(
                Step(
                "Notification Access",
                "Allows QR Zen to suppress notifications from blocked apps while a session is active.",
                onGrant = {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            ))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    Step(
                        getString(R.string.perm_notif_title),
                        getString(R.string.perm_notif_desc),
                        onGrant = {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                REQ_POST_NOTIF
                            )
                        }
                    )
                )
            }
            add(
                Step(
                "Device Admin",
                "Device Admin prevents QR Zen from being uninstalled while a block is active, making it harder to circumvent.",
                onGrant = {
                    val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(
                            android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            android.content.ComponentName(this@PermissionActivity, com.qrzen.app.admin.QrZenDeviceAdmin::class.java)
                        )
                    }
                    startActivity(intent)
                }
            ))
            add(
                Step(
                "Battery Optimization",
                "Exclude QR Zen from battery optimization so it keeps running in the background and survives aggressive power-saving modes.",
                onGrant = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            ))
            add(
                Step(
                "Display Over Apps",
                "Allows QR Zen to show the block overlay on top of other apps when a blocked app is detected.",
                onGrant = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            ))
            add(
                Step(
                "Usage Access",
                "Allows QR Zen to see which apps you use so it can track screen time and enforce usage limits.",
                grantLabel = "Grant & Finish",
                onGrant = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            ))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showStep(0)

        binding.btnGrant.setOnClickListener {
            steps[currentStep].onGrant(this)
            if (currentStep < steps.size - 1) {
                showStep(currentStep + 1)
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            if (currentStep < steps.size - 1) {
                showStep(currentStep + 1)
            } else {
                finishOnboarding()
            }
        }
    }

    private fun showStep(step: Int) {
        currentStep = step
        binding.tvStepTitle.text = steps[step].title
        binding.tvStepDesc.text = steps[step].description
        binding.btnGrant.text = steps[step].grantLabel
        binding.tvProgress.text = "${step + 1} / ${steps.size}"
        binding.btnSkip.text = if (step == steps.size - 1) "Finish" else "Skip"
    }

    private fun finishOnboarding() {
        Prefs.onboardingComplete = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
