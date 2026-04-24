package com.qrzen.app.ui.allowlist

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.king.zxing.CameraScan
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.BlockEventDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockEvent
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityAllowlistOverlayBinding
import com.qrzen.app.databinding.BottomSheetPauseDurationBinding
import com.qrzen.app.databinding.ItemAllowedAppBinding
import com.qrzen.app.ui.lock.QrScanActivity
import com.qrzen.app.ui.unlock.UnlockChallengeRenderer
import com.qrzen.app.util.SilentModeHelper
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AllowlistOverlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_BLOCKED_PKG = "extra_blocked_pkg"
    }

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var blockEventDao: BlockEventDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private lateinit var binding: ActivityAllowlistOverlayBinding
    private lateinit var unlockRenderer: UnlockChallengeRenderer
    private var currentBlock: AppBlock? = null
    private val sessionRemovedApps = mutableSetOf<String>()
    private var countDownTimer: CountDownTimer? = null
    private var pauseSheetShown = false
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        unlockRenderer.handleQrScanResult(result.data?.getStringExtra(CameraScan.SCAN_RESULT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        binding = ActivityAllowlistOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        unlockRenderer = UnlockChallengeRenderer(this, binding.challengeContainer, binding.tvError)
        binding.rvAllowedApps.layoutManager = GridLayoutManager(this, 3)
        loadBlock(intent.getIntExtra(EXTRA_BLOCK_ID, -1))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sessionRemovedApps.clear()
        loadBlock(intent.getIntExtra(EXTRA_BLOCK_ID, -1))
    }

    private fun loadBlock(blockId: Int) {
        pauseSheetShown = false
        lifecycleScope.launch {
            val block = dao.getById(blockId) ?: run {
                finish()
                return@launch
            }
            currentBlock = block
            if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
                binding.tvBlockTitle.text = block.title
                binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
                showPauseDurationSheet(block)
                return@launch
            }
            val allowedApps = withContext(Dispatchers.IO) { buildAllowedApps(block) }
            val timeBlocks = timeBlockDao.getByBlockId(block.id)
            setupUi(block, timeBlocks, allowedApps)
        }
    }

    private fun setupUi(block: AppBlock, timeBlocks: List<TimeBlock>, allowedApps: List<AllowedAppItem>) {
        binding.tvBlockTitle.text = block.title
        binding.tvTimeRange.text = "${block.startTime} – ${block.endTime}"
        binding.tvError.visibility = View.GONE
        binding.tvError.text = ""
        binding.rvAllowedApps.adapter = AllowedAppAdapter(
            allowedApps,
            onClick = { launchAllowedApp(it) },
            onLongPress = { showRemoveAppDialog(it) },
            onTimerExpired = { refreshAllowedApps() }
        )
        unlockRenderer.render(
            block = block,
            timeBlocks = timeBlocks,
            showGoBackButton = false,
            onRequestQrScan = {
                qrScanLauncher.launch(Intent(this, QrScanActivity::class.java))
            },
            onUnlocked = {
                showPauseDurationSheet(block)
            }
        )
        val showMasterPwd = block.masterPasswordEnabled && Prefs.masterPasswordEnabled
        binding.btnMasterPassword.visibility = if (showMasterPwd) View.VISIBLE else View.GONE
        binding.btnMasterPassword.setOnClickListener { showMasterPasswordDialog(block) }
        SilentModeHelper.applySilentMode(this)
        startCountdown(block)
    }

    private suspend fun buildAllowedApps(block: AppBlock): List<AllowedAppItem> {
        val pm = packageManager
        return block.appPackages
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot(Prefs::isAppTimerExpired)
            .filterNot { it in sessionRemovedApps }
            .mapNotNull { pkg ->
                try {
                    val info = pm.getApplicationInfo(pkg, 0)
                    AllowedAppItem(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(info),
                        timerExpiry = Prefs.getAppTimerExpiry(pkg)
                    )
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun startCountdown(block: AppBlock) {
        countDownTimer?.cancel()
        val millis = calculateMillisUntilEnd(block.endTime)
        if (millis <= 0L) {
            finish()
            return
        }
        binding.tvCountdown.text = formatCountdown(millis)
        countDownTimer = object : CountDownTimer(millis, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = formatCountdown(millisUntilFinished)
            }

            override fun onFinish() {
                binding.tvCountdown.text = formatCountdown(0L)
                finish()
            }
        }.start()
    }

    private fun calculateMillisUntilEnd(endTime: String): Long {
        val now = LocalDateTime.now()
        val end = LocalTime.parse(endTime, timeFormatter)
        var endDateTime = now.withHour(end.hour).withMinute(end.minute).withSecond(0).withNano(0)
        if (!endDateTime.isAfter(now)) {
            endDateTime = endDateTime.plusDays(1)
        }
        return Duration.between(now, endDateTime).toMillis().coerceAtLeast(0L)
    }

    private fun formatCountdown(millis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis).coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun launchAllowedApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        SilentModeHelper.restoreRinger(this)
        startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    private fun showRemoveAppDialog(appItem: AllowedAppItem) {
        AlertDialog.Builder(this)
            .setTitle(appItem.label)
            .setMessage(getString(R.string.overlay_remove_app_message))
            .setPositiveButton(R.string.overlay_remove_app_confirm) { _, _ ->
                sessionRemovedApps.add(appItem.packageName)
                refreshAllowedApps()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun refreshAllowedApps() {
        val block = currentBlock ?: return
        lifecycleScope.launch {
            val allowedApps = withContext(Dispatchers.IO) { buildAllowedApps(block) }
            (binding.rvAllowedApps.adapter as? AllowedAppAdapter)?.cancelAllTimers()
            binding.rvAllowedApps.adapter = AllowedAppAdapter(
                allowedApps,
                onClick = { launchAllowedApp(it) },
                onLongPress = { showRemoveAppDialog(it) },
                onTimerExpired = { refreshAllowedApps() }
            )
        }
    }

    private fun showPauseDurationSheet(block: AppBlock) {
        if (pauseSheetShown) return
        pauseSheetShown = true
        val sheet = BottomSheetDialog(this)
        val sb = BottomSheetPauseDurationBinding.inflate(LayoutInflater.from(this))
        sheet.setContentView(sb.root)
        sb.btn15min.setOnClickListener { applyPause(block, 15 * 60_000L); sheet.dismiss() }
        sb.btn30min.setOnClickListener { applyPause(block, 30 * 60_000L); sheet.dismiss() }
        sb.btn1hr.setOnClickListener { applyPause(block, 60 * 60_000L); sheet.dismiss() }
        sb.btn2hr.setOnClickListener { applyPause(block, 2 * 60 * 60_000L); sheet.dismiss() }
        sb.btnRestOfDay.setOnClickListener { applyPause(block, millisUntilMidnight()); sheet.dismiss() }
        sb.btnIndefinitely.setOnClickListener {
            lifecycleScope.launch {
                dao.update(block.copy(isEnabled = false, pausedUntil = 0L))
                WidgetRefresh.refresh(applicationContext)
                SilentModeHelper.restoreRinger(this@AllowlistOverlayActivity)
                finish()
            }
            sheet.dismiss()
        }
        sheet.show()
    }

    private fun applyPause(block: AppBlock, durationMs: Long) {
        val until = if (durationMs == Long.MAX_VALUE) Long.MAX_VALUE else System.currentTimeMillis() + durationMs
        val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PKG) ?: ""
        lifecycleScope.launch {
            dao.setPausedUntil(block.id, until)
            WidgetRefresh.refresh(applicationContext)
            blockEventDao.insert(
                BlockEvent(
                    blockId = block.id,
                    blockTitle = block.title,
                    packageName = blockedPkg,
                    eventType = "PAUSED"
                )
            )
            SilentModeHelper.restoreRinger(this@AllowlistOverlayActivity)
            finish()
        }
    }

    private fun millisUntilMidnight(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }

    private fun showMasterPasswordDialog(block: AppBlock) {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.block_master_password)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.block_master_password)
            .setView(et)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (et.text.toString() == Prefs.masterPassword) {
                    binding.tvError.visibility = View.GONE
                    showPauseDurationSheet(block)
                } else {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = getString(R.string.challenge_password_wrong)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun goToLauncher() {
        SilentModeHelper.restoreRinger(this)
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goToLauncher()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        (binding.rvAllowedApps.adapter as? AllowedAppAdapter)?.cancelAllTimers()
        unlockRenderer.clear()
        super.onDestroy()
    }

    data class AllowedAppItem(
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val timerExpiry: Long = 0L
    )

    class AllowedAppAdapter(
        private val apps: List<AllowedAppItem>,
        private val onClick: (String) -> Unit,
        private val onLongPress: (AllowedAppItem) -> Unit,
        private val onTimerExpired: () -> Unit
    ) : RecyclerView.Adapter<AllowedAppAdapter.ViewHolder>() {

        private val timerHandlers = mutableMapOf<String, CountDownTimer>()

        inner class ViewHolder(private val binding: ItemAllowedAppBinding) : RecyclerView.ViewHolder(binding.root) {
            var boundPackageName: String? = null

            fun bind(item: AllowedAppItem) {
                boundPackageName?.let { pkg ->
                    timerHandlers.remove(pkg)?.cancel()
                }
                boundPackageName = item.packageName

                binding.ivAppIcon.setImageDrawable(item.icon)
                binding.tvAppLabel.text = item.label
                binding.root.setOnClickListener { onClick(item.packageName) }
                binding.root.setOnLongClickListener {
                    onLongPress(item)
                    true
                }

                val now = System.currentTimeMillis()
                if (item.timerExpiry > now) {
                    binding.tvTimerOverlay.visibility = View.VISIBLE
                    val remaining = item.timerExpiry - now
                    binding.tvTimerOverlay.text = formatTimerOverlay(remaining)
                    timerHandlers[item.packageName] = object : CountDownTimer(remaining, 1_000L) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.tvTimerOverlay.text = formatTimerOverlay(millisUntilFinished)
                        }

                        override fun onFinish() {
                            binding.tvTimerOverlay.text = formatTimerOverlay(0L)
                            binding.tvTimerOverlay.visibility = View.GONE
                            timerHandlers.remove(item.packageName)
                            onTimerExpired()
                        }
                    }.start()
                } else {
                    binding.tvTimerOverlay.visibility = View.GONE
                    binding.tvTimerOverlay.text = ""
                }
            }
        }

        private fun formatTimerOverlay(millis: Long): String {
            val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
            val hours = totalSeconds / 3_600L
            val minutes = (totalSeconds % 3_600L) / 60L
            val seconds = totalSeconds % 60L
            return if (hours > 0L) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

        fun cancelAllTimers() {
            timerHandlers.values.forEach { it.cancel() }
            timerHandlers.clear()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemAllowedAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(apps[position])
        }

        override fun getItemCount(): Int = apps.size

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.boundPackageName?.let { pkg ->
                timerHandlers.remove(pkg)?.cancel()
            }
            holder.boundPackageName = null
        }
    }
}
