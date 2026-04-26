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
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import com.qrzen.app.util.BruteForceGuard
import com.qrzen.app.util.PasswordHasher
import com.qrzen.app.util.SilentModeHelper
import com.qrzen.app.widget.WidgetRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AllowlistOverlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_IDS = "extra_block_ids"
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_BLOCKED_PKG = "extra_blocked_pkg"
    }

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var timeBlockDao: TimeBlockDao
    @Inject lateinit var blockEventDao: BlockEventDao

    private lateinit var binding: ActivityAllowlistOverlayBinding
    private lateinit var unlockRenderer: UnlockChallengeRenderer
    private var activeBlocks: MutableList<AppBlock> = mutableListOf()
    private var selectedBlockIndex: Int = 0
    private var displayedCountdownIndex: Int = 0
    private var countdownTimers: MutableMap<Int, CountDownTimer> = mutableMapOf()
    private var timeBlocksByBlockId: Map<Int, List<TimeBlock>> = emptyMap()
    private val sessionRemovedApps = mutableSetOf<String>()
    private var pauseSheetShown = false
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val masterPasswordGuard = BruteForceGuard()
    private var masterPasswordLockoutRunnable: Runnable? = null

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
        val blockIds = intent.getIntArrayExtra(EXTRA_BLOCK_IDS)
            ?: intent.getIntExtra(EXTRA_BLOCK_ID, -1).let { if (it == -1) intArrayOf() else intArrayOf(it) }
        if (blockIds.isEmpty()) {
            finish()
            return
        }
        loadBlocks(blockIds)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sessionRemovedApps.clear()
        cancelAllCountdowns()
        val blockIds = intent.getIntArrayExtra(EXTRA_BLOCK_IDS)
            ?: intent.getIntExtra(EXTRA_BLOCK_ID, -1).let { if (it == -1) intArrayOf() else intArrayOf(it) }
        if (blockIds.isEmpty()) {
            finish()
            return
        }
        loadBlocks(blockIds)
    }

    private fun loadBlocks(blockIds: IntArray) {
        pauseSheetShown = false
        lifecycleScope.launch {
            val now = System.currentTimeMillis()
            val loadedBlocks = mutableListOf<AppBlock>()
            for (blockId in blockIds) {
                val block = dao.getById(blockId)
                if (block != null && block.isEnabled && !block.isArchived && block.pausedUntil <= now) {
                    loadedBlocks += block
                }
            }
            activeBlocks = loadedBlocks.toMutableList()
            if (activeBlocks.isEmpty()) {
                finish()
                return@launch
            }
            if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
                setupHeader()
                showPauseDurationSheet(activeBlocks.first())
                return@launch
            }
            val intersectedApps = withContext(Dispatchers.IO) { buildIntersectedAllowedApps() }
            val timeBlocksMap = withContext(Dispatchers.IO) { loadTimeBlocksMap(activeBlocks) }
            setupUi(intersectedApps, timeBlocksMap)
        }
    }

    private suspend fun loadTimeBlocksMap(blocks: List<AppBlock>): Map<Int, List<TimeBlock>> {
        val map = linkedMapOf<Int, List<TimeBlock>>()
        for (block in blocks) {
            map[block.id] = timeBlockDao.getByBlockId(block.id)
        }
        return map
    }

    private fun buildIntersectedAllowedApps(): List<AllowedAppItem> {
        if (activeBlocks.isEmpty()) return emptyList()
        val pm = packageManager
        val allowedSets = activeBlocks.map { block ->
            block.appPackages.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .filterNot(Prefs::isAppTimerExpired)
                .filterNot { it in sessionRemovedApps }
                .toSet()
        }
        val intersection = allowedSets.reduce { acc, set -> acc.intersect(set) }
        return intersection.mapNotNull { pkg ->
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
        }.sortedBy { it.label.lowercase() }
    }

    private fun setupUi(allowedApps: List<AllowedAppItem>, timeBlocksMap: Map<Int, List<TimeBlock>>) {
        timeBlocksByBlockId = timeBlocksMap
        binding.tvError.visibility = View.GONE
        binding.tvError.text = ""
        setupHeader()
        setupCountdowns()
        setupBlockSelector(timeBlocksMap)
        (binding.rvAllowedApps.adapter as? AllowedAppAdapter)?.cancelAllTimers()
        binding.rvAllowedApps.adapter = AllowedAppAdapter(
            allowedApps,
            onClick = { launchAllowedApp(it) },
            onLongPress = { showRemoveAppDialog(it) },
            onTimerExpired = { refreshOverlay() }
        )

        SilentModeHelper.applySilentMode(this)
    }

    private fun setupHeader() {
        binding.tvBlockTitle.text = if (activeBlocks.size == 1) {
            activeBlocks.first().title
        } else {
            getString(R.string.allowlist_multi_title, activeBlocks.size)
        }
        binding.tvTimeRange.visibility = View.GONE
    }

    private fun setupCountdowns() {
        cancelAllCountdowns()
        if (activeBlocks.isEmpty()) return
        displayedCountdownIndex = displayedCountdownIndex.coerceIn(0, activeBlocks.lastIndex)
        val expiredBlockIds = mutableSetOf<Int>()
        for (block in activeBlocks) {
            val millis = calculateMillisUntilBlockEnd(block)
            if (millis <= 0L) {
                expiredBlockIds += block.id
                continue
            }
            if (millis > 86_400_000L * 30) {
                continue
            }
            countdownTimers[block.id] = object : CountDownTimer(millis, 1_000L) {
                override fun onTick(millisUntilFinished: Long) {
                    if (displayedCountdownIndex < activeBlocks.size && activeBlocks[displayedCountdownIndex].id == block.id) {
                        binding.tvCountdown.text = formatCountdown(millisUntilFinished)
                    }
                }

                override fun onFinish() {
                    activeBlocks.removeAll { it.id == block.id }
                    timeBlocksByBlockId = timeBlocksByBlockId - block.id
                    countdownTimers.remove(block.id)
                    if (activeBlocks.isEmpty()) {
                        SilentModeHelper.restoreRinger(this@AllowlistOverlayActivity)
                        finish()
                    } else {
                        displayedCountdownIndex = displayedCountdownIndex.coerceAtMost(activeBlocks.size - 1)
                        refreshOverlay()
                    }
                }
            }.start()
        }
        if (expiredBlockIds.isNotEmpty()) {
            activeBlocks.removeAll { it.id in expiredBlockIds }
            timeBlocksByBlockId = timeBlocksByBlockId.filterKeys { it !in expiredBlockIds }
            if (activeBlocks.isEmpty()) {
                SilentModeHelper.restoreRinger(this)
                finish()
            } else {
                displayedCountdownIndex = displayedCountdownIndex.coerceAtMost(activeBlocks.size - 1)
                refreshOverlay()
            }
            return
        }
        updateCountdownDisplay()
        binding.tvCountdown.setOnClickListener {
            if (activeBlocks.size > 1) {
                displayedCountdownIndex = (displayedCountdownIndex + 1) % activeBlocks.size
                updateCountdownDisplay()
            }
        }
        binding.tvTimeRange.setOnClickListener {
            if (activeBlocks.size > 1) {
                displayedCountdownIndex = (displayedCountdownIndex + 1) % activeBlocks.size
                updateCountdownDisplay()
            }
        }
    }

    private fun updateCountdownDisplay() {
        if (activeBlocks.isEmpty()) return
        val block = activeBlocks[displayedCountdownIndex]
        binding.tvTimeRange.visibility = View.VISIBLE
        binding.tvTimeRange.text = if (activeBlocks.size > 1) {
            getString(R.string.allowlist_tap_switch, block.title)
        } else {
            block.title
        }
        val millis = calculateMillisUntilBlockEnd(block)
        if (millis <= 0L) {
            binding.tvCountdown.text = formatCountdown(0L)
            refreshOverlay()
            return
        }
        if (millis > 86_400_000L * 30) {
            binding.tvCountdown.text = "--:--:--"
        } else {
            binding.tvCountdown.text = formatCountdown(millis)
        }
    }

    private fun calculateMillisUntilBlockEnd(block: AppBlock): Long {
        val now = System.currentTimeMillis()
        if (block.activeUntil > now && block.activeUntil != Long.MAX_VALUE) {
            return block.activeUntil - now
        }
        if (block.blockNowUntil > now && block.blockNowUntil != Long.MAX_VALUE) {
            return block.blockNowUntil - now
        }
        if (block.blockingStyle == UnlockMethodUtils.STYLE_MANUAL) {
            if (block.toggleLockUntil > now && block.toggleLockUntil != Long.MAX_VALUE) {
                return block.toggleLockUntil - now
            }
            return Long.MAX_VALUE / 2
        }
        if (block.blockingStyle == UnlockMethodUtils.STYLE_SCHEDULE) {
            val nowDt = LocalDateTime.now()
            val timeBlocks = timeBlocksByBlockId[block.id].orEmpty()
            val activeEnd = timeBlocks.mapNotNull { timeBlock ->
                findActiveTimeBlockEnd(timeBlock, nowDt)
            }.minOrNull()
            if (activeEnd != null) {
                return Duration.between(nowDt, activeEnd).toMillis().coerceAtLeast(0L)
            }
        }
        return calculateMillisUntilEnd(block.endTime)
    }

    private fun findActiveTimeBlockEnd(timeBlock: TimeBlock, now: LocalDateTime): LocalDateTime? {
        val start = LocalTime.parse(timeBlock.startTime, timeFormatter)
        val end = LocalTime.parse(timeBlock.endTime, timeFormatter)
        val candidateDates = listOf(now.toLocalDate().minusDays(1), now.toLocalDate())
        for (date in candidateDates) {
            if (!isDayActive(timeBlock.activeDays, date)) continue
            val startDateTime = date.atTime(start)
            val endDateTime = if (end <= start) date.plusDays(1).atTime(end) else date.atTime(end)
            if (!now.isBefore(startDateTime) && now.isBefore(endDateTime)) {
                return endDateTime
            }
        }
        return null
    }

    private fun isDayActive(activeDays: String, date: LocalDate): Boolean {
        val dayIndex = date.dayOfWeek.value - 1
        return activeDays.padEnd(7, '0').getOrNull(dayIndex) == '1'
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

    private fun setupBlockSelector(timeBlocksMap: Map<Int, List<TimeBlock>>) {
        if (activeBlocks.isEmpty()) return
        if (activeBlocks.size <= 1) {
            binding.spinnerBlockSelector.visibility = View.GONE
            selectedBlockIndex = 0
            val block = activeBlocks.first()
            renderUnlockChallenge(block, timeBlocksMap[block.id] ?: emptyList())
            return
        }
        binding.spinnerBlockSelector.visibility = View.VISIBLE
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            activeBlocks.map { it.title }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBlockSelector.adapter = adapter
        binding.spinnerBlockSelector.setSelection(selectedBlockIndex.coerceIn(0, activeBlocks.lastIndex), false)
        binding.spinnerBlockSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBlockIndex = position
                val block = activeBlocks[position]
                renderUnlockChallenge(block, timeBlocksMap[block.id] ?: emptyList())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        val initialBlock = activeBlocks[selectedBlockIndex.coerceIn(0, activeBlocks.lastIndex)]
        renderUnlockChallenge(initialBlock, timeBlocksMap[initialBlock.id] ?: emptyList())
    }

    private fun renderUnlockChallenge(block: AppBlock, timeBlocks: List<TimeBlock>) {
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
    }

    private fun formatCountdown(millis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis).coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
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
                refreshOverlay()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showPauseDurationSheet(block: AppBlock) {
        if (pauseSheetShown) return
        pauseSheetShown = true
        val sheet = BottomSheetDialog(this)
        val sb = BottomSheetPauseDurationBinding.inflate(LayoutInflater.from(this))
        sheet.setContentView(sb.root)
        sheet.setOnDismissListener { pauseSheetShown = false }
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
            activeBlocks.removeAll { it.id == block.id }
            timeBlocksByBlockId = timeBlocksByBlockId - block.id
            countdownTimers.remove(block.id)?.cancel()
            if (activeBlocks.isEmpty()) {
                SilentModeHelper.restoreRinger(this@AllowlistOverlayActivity)
                finish()
            } else {
                selectedBlockIndex = 0
                displayedCountdownIndex = displayedCountdownIndex.coerceAtMost(activeBlocks.size - 1)
                refreshOverlay()
            }
        }
    }

    private fun refreshOverlay() {
        lifecycleScope.launch {
            val now = System.currentTimeMillis()
            val previousSelectedId = activeBlocks.getOrNull(selectedBlockIndex)?.id
            val previousDisplayedId = activeBlocks.getOrNull(displayedCountdownIndex)?.id
            val refreshedBlocks = mutableListOf<AppBlock>()
            for (block in activeBlocks) {
                val refreshed = dao.getById(block.id)
                if (refreshed != null && refreshed.isEnabled && !refreshed.isArchived && refreshed.pausedUntil <= now) {
                    refreshedBlocks += refreshed
                }
            }
            activeBlocks = refreshedBlocks.toMutableList()
            if (activeBlocks.isEmpty()) {
                SilentModeHelper.restoreRinger(this@AllowlistOverlayActivity)
                finish()
                return@launch
            }
            selectedBlockIndex = activeBlocks.indexOfFirst { it.id == previousSelectedId }
                .takeIf { it >= 0 } ?: 0
            displayedCountdownIndex = activeBlocks.indexOfFirst { it.id == previousDisplayedId }
                .takeIf { it >= 0 } ?: displayedCountdownIndex.coerceAtMost(activeBlocks.size - 1)
            val intersectedApps = withContext(Dispatchers.IO) { buildIntersectedAllowedApps() }
            val timeBlocksMap = withContext(Dispatchers.IO) { loadTimeBlocksMap(activeBlocks) }
            pauseSheetShown = false
            (binding.rvAllowedApps.adapter as? AllowedAppAdapter)?.cancelAllTimers()
            setupUi(intersectedApps, timeBlocksMap)
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
        val initialLockout = masterPasswordGuard.checkAllowed()
        if (initialLockout != null) {
            showMasterPasswordLockout(initialLockout)
            return
        }
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.block_master_password)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.block_master_password)
            .setView(et)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val lockoutRemaining = masterPasswordGuard.checkAllowed()
                if (lockoutRemaining != null) {
                    dialog.dismiss()
                    showMasterPasswordLockout(lockoutRemaining)
                    return@setOnClickListener
                }
                if (PasswordHasher.verify(et.text.toString(), Prefs.masterPassword)) {
                    masterPasswordGuard.reset()
                    clearMasterPasswordError()
                    dialog.dismiss()
                    showPauseDurationSheet(block)
                } else {
                    masterPasswordGuard.recordFailure()
                    val updatedLockout = masterPasswordGuard.checkAllowed()
                    if (updatedLockout != null) {
                        et.error = formatLockoutMessage(updatedLockout)
                        dialog.dismiss()
                        showMasterPasswordLockout(updatedLockout)
                    } else {
                        et.error = getString(R.string.challenge_password_wrong)
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = getString(R.string.challenge_password_wrong)
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showMasterPasswordLockout(remainingMillis: Long) {
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = formatLockoutMessage(remainingMillis)
        binding.btnMasterPassword.isEnabled = false
        masterPasswordLockoutRunnable?.let(binding.btnMasterPassword::removeCallbacks)
        masterPasswordLockoutRunnable = Runnable {
            val remaining = masterPasswordGuard.checkAllowed()
            if (remaining != null) {
                showMasterPasswordLockout(remaining)
            } else {
                binding.btnMasterPassword.isEnabled = true
            }
        }.also { binding.btnMasterPassword.postDelayed(it, remainingMillis) }
    }

    private fun clearMasterPasswordError() {
        binding.tvError.visibility = View.GONE
        binding.tvError.text = ""
        binding.btnMasterPassword.isEnabled = true
        masterPasswordLockoutRunnable?.let(binding.btnMasterPassword::removeCallbacks)
        masterPasswordLockoutRunnable = null
    }

    private fun formatLockoutMessage(remainingMillis: Long): String {
        return "Too many attempts. Try again in ${UnlockMethodUtils.formatCountdown(remainingMillis)}"
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
        masterPasswordLockoutRunnable?.let(binding.btnMasterPassword::removeCallbacks)
        cancelAllCountdowns()
        (binding.rvAllowedApps.adapter as? AllowedAppAdapter)?.cancelAllTimers()
        unlockRenderer.clear()
        super.onDestroy()
    }

    private fun cancelAllCountdowns() {
        countdownTimers.values.forEach { it.cancel() }
        countdownTimers.clear()
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
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
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
