package com.qrzen.app.ui.lock

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.qrzen.app.databinding.ActivityLockScreenBinding
import com.qrzen.app.databinding.BottomSheetPauseDurationBinding
import com.qrzen.app.ui.unlock.UnlockChallengeRenderer
import com.qrzen.app.util.SilentModeHelper
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_BLOCKED_PKG = "extra_blocked_pkg"
    }

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var blockEventDao: BlockEventDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private lateinit var binding: ActivityLockScreenBinding
    private lateinit var unlockRenderer: UnlockChallengeRenderer
    private var currentBlock: AppBlock? = null
    private var pauseSheetShown = false
    private var waitTimerCountdown: CountDownTimer? = null

    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        unlockRenderer.handleQrScanResult(result.data?.getStringExtra(CameraScan.SCAN_RESULT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        unlockRenderer = UnlockChallengeRenderer(this, binding.challengeContainer, binding.tvError)
        loadBlock(intent.getIntExtra(EXTRA_BLOCK_ID, -1))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
            val timeBlocks = timeBlockDao.getByBlockId(block.id)
            if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
                binding.tvBlockTitle.text = block.title
                binding.tvBlockMessage.text = getString(R.string.lock_screen_message)
                showPauseDurationSheet(block)
                return@launch
            }
            setupUi(block, timeBlocks)
        }
    }

    private fun setupUi(block: AppBlock, timeBlocks: List<TimeBlock>) {
        binding.tvBlockTitle.text = block.title
        binding.tvBlockMessage.text = getString(R.string.lock_screen_message)
        setupWaitTimerCountdown(block)
        if (block.blockNowUntil > System.currentTimeMillis() && block.blockNowUntil != Long.MAX_VALUE) {
            val remaining = block.blockNowUntil - System.currentTimeMillis()
            binding.tvBlockMessage.text = "Block Now ends in ${formatCountdown(remaining)}"
            waitTimerCountdown?.cancel()
            waitTimerCountdown = object : CountDownTimer(remaining, 1_000L) {
                override fun onTick(ms: Long) {
                    binding.tvBlockMessage.text = "Block Now ends in ${formatCountdown(ms)}"
                }
                override fun onFinish() {
                    binding.tvBlockMessage.text = "Block Now ended"
                    finish()
                }
            }.start()
        }
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
        binding.btnGoHome.setOnClickListener { goToLauncher() }
        SilentModeHelper.applySilentMode(this)
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
                SilentModeHelper.restoreRinger(this@LockScreenActivity)
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
            SilentModeHelper.restoreRinger(this@LockScreenActivity)
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

    private fun setupWaitTimerCountdown(block: AppBlock) {
        waitTimerCountdown?.cancel()
        waitTimerCountdown = null
        if (block.blockingStyle != com.qrzen.app.ui.unlock.UnlockMethodUtils.STYLE_WAIT_TIMER) return
        val kv = com.tencent.mmkv.MMKV.defaultMMKV()
        val blockingUntil = kv.decodeLong("wait_timer_blocking_${block.id}", 0L)
        val now = System.currentTimeMillis()
        if (blockingUntil <= now) return
        val remaining = blockingUntil - now
        binding.tvBlockMessage.text = "Resets in ${formatCountdown(remaining)}"
        waitTimerCountdown = object : CountDownTimer(remaining, 1_000L) {
            override fun onTick(ms: Long) {
                binding.tvBlockMessage.text = "Resets in ${formatCountdown(ms)}"
            }
            override fun onFinish() {
                binding.tvBlockMessage.text = "Block reset"
                finish()
            }
        }.start()
    }

    private fun formatCountdown(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
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
        waitTimerCountdown?.cancel()
        unlockRenderer.clear()
        super.onDestroy()
    }
}
