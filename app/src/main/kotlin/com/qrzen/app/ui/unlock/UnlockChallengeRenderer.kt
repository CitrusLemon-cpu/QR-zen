package com.qrzen.app.ui.unlock

import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.databinding.ViewUnlockDelayBinding
import com.qrzen.app.databinding.ViewUnlockInfoBinding
import com.qrzen.app.databinding.ViewUnlockPasswordBinding
import com.qrzen.app.databinding.ViewUnlockQrBinding
import com.qrzen.app.databinding.ViewUnlockTypeOverBinding

class UnlockChallengeRenderer(
    private val activity: FragmentActivity,
    private val container: FrameLayout,
    private val errorView: TextView
) {
    private var timer: CountDownTimer? = null
    private var currentQrBlock: AppBlock? = null
    private var onQrSuccess: (() -> Unit)? = null
    private var typeOverSessionText: String? = null

    fun render(
        block: AppBlock,
        timeBlocks: List<TimeBlock> = emptyList(),
        showGoBackButton: Boolean,
        onRequestQrScan: () -> Unit,
        onUnlocked: () -> Unit,
        onGoBack: (() -> Unit)? = null
    ) {
        clear()
        hideError()
        when (UnlockMethodUtils.getNormalizedMethod(block)) {
            UnlockMethodUtils.METHOD_NONE -> showNoneChallenge(onUnlocked)
            UnlockMethodUtils.METHOD_DELAY -> showDelay(block, onUnlocked)
            UnlockMethodUtils.METHOD_PASSWORD -> showPassword(block, onUnlocked)
            UnlockMethodUtils.METHOD_TYPE_OVER_TEXT -> showTypeOverText(block, onUnlocked)
            UnlockMethodUtils.METHOD_QR_CODE -> showQr(block, onRequestQrScan, onUnlocked)
            UnlockMethodUtils.METHOD_EDIT_WINDOW -> {
                val availability = UnlockMethodUtils.getEditWindowAvailability(block)
                if (availability.isAvailable) {
                    onUnlocked()
                } else {
                    showEditWindowInfo(block, availability.nextAvailableMillis, showGoBackButton, onGoBack)
                }
            }
            UnlockMethodUtils.METHOD_TIMER -> {
                if (UnlockMethodUtils.isTimerExpired(block)) {
                    onUnlocked()
                } else {
                    showTimerInfo(block, showGoBackButton, onGoBack)
                }
            }
            UnlockMethodUtils.METHOD_WHILE_ACTIVE -> {
                if (UnlockMethodUtils.isBlockCurrentlyActive(block, timeBlocks)) {
                    showWhileActiveInfo(block, showGoBackButton, onGoBack)
                } else {
                    onUnlocked()
                }
            }
            else -> onUnlocked()
        }
    }

    private fun showNoneChallenge(onUnlocked: () -> Unit) {
        val binding = ViewUnlockInfoBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.unlock_method_none)
        binding.tvChallengeBody.text = activity.getString(R.string.unlock_none_lock_desc)
        binding.tvChallengeSecondary.visibility = View.GONE
        binding.btnGoBack.visibility = View.VISIBLE
        binding.btnGoBack.text = activity.getString(R.string.challenge_pause_block)
        binding.btnGoBack.setOnClickListener { onUnlocked() }
    }

    fun handleQrScanResult(scannedText: String?) {
        val block = currentQrBlock ?: return
        if (scannedText == block.qrSecret) {
            hideError()
            onQrSuccess?.invoke()
        } else {
            showError(activity.getString(R.string.block_wrong_qr))
        }
    }

    fun clear() {
        timer?.cancel()
        timer = null
        currentQrBlock = null
        onQrSuccess = null
        typeOverSessionText = null
        container.removeAllViews()
    }

    private fun showDelay(block: AppBlock, onUnlocked: () -> Unit) {
        val binding = ViewUnlockDelayBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.unlock_method_delay)
        val durationMillis = block.delayMinutes.coerceAtLeast(0) * 60_000L
        binding.btnProceed.setOnClickListener {
            hideError()
            onUnlocked()
        }
        if (durationMillis <= 0L) {
            binding.tvChallengeBody.text = activity.getString(R.string.challenge_delay_ready)
            binding.tvCountdown.text = UnlockMethodUtils.formatCountdown(0L)
            binding.btnProceed.visibility = View.VISIBLE
            return
        }
        updateDelayViews(binding, durationMillis)
        binding.btnProceed.visibility = View.GONE
        timer = object : CountDownTimer(durationMillis, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateDelayViews(binding, millisUntilFinished)
            }

            override fun onFinish() {
                binding.tvChallengeBody.text = activity.getString(R.string.challenge_delay_ready)
                binding.tvCountdown.text = UnlockMethodUtils.formatCountdown(0L)
                binding.btnProceed.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun updateDelayViews(binding: ViewUnlockDelayBinding, millis: Long) {
        val formatted = UnlockMethodUtils.formatCountdown(millis)
        binding.tvChallengeBody.text = activity.getString(R.string.challenge_delay_wait, formatted)
        binding.tvCountdown.text = formatted
    }

    private fun showPassword(block: AppBlock, onUnlocked: () -> Unit) {
        val binding = ViewUnlockPasswordBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.challenge_password_title)
        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setupErrorClearing(binding.etPassword)
        binding.btnUnlock.setOnClickListener {
            hideError()
            if (binding.etPassword.text?.toString() == block.blockPassword) {
                onUnlocked()
            } else {
                showError(activity.getString(R.string.challenge_password_wrong))
            }
        }
    }

    private fun showTypeOverText(block: AppBlock, onUnlocked: () -> Unit) {
        val binding = ViewUnlockTypeOverBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.challenge_type_over_title)
        val challengeText = UnlockMethodUtils.getTypeOverChallengeText(block, typeOverSessionText)
        typeOverSessionText = challengeText
        binding.tvChallengeText.text = challengeText
        binding.etTypeOver.setSingleLine(false)
        binding.etTypeOver.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        setupErrorClearing(binding.etTypeOver)
        binding.btnUnlock.setOnClickListener {
            hideError()
            if (binding.etTypeOver.text?.toString() == challengeText) {
                onUnlocked()
            } else {
                showError(activity.getString(R.string.challenge_type_over_wrong))
            }
        }
    }

    private fun showQr(block: AppBlock, onRequestQrScan: () -> Unit, onUnlocked: () -> Unit) {
        val binding = ViewUnlockQrBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.challenge_qr_title)
        currentQrBlock = block
        onQrSuccess = onUnlocked
        binding.btnScanQr.setOnClickListener {
            hideError()
            onRequestQrScan()
        }
    }

    private fun showEditWindowInfo(
        block: AppBlock,
        nextAvailableMillis: Long?,
        showGoBackButton: Boolean,
        onGoBack: (() -> Unit)?
    ) {
        val binding = ViewUnlockInfoBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.unlock_method_edit_window)
        binding.tvChallengeBody.text = activity.getString(
            R.string.challenge_edit_window_locked,
            block.editWindowStart,
            block.editWindowEnd,
            UnlockMethodUtils.formatDays(block.editWindowDays)
        )
        if (nextAvailableMillis != null) {
            binding.tvChallengeSecondary.visibility = View.VISIBLE
            binding.tvChallengeSecondary.text = activity.getString(
                R.string.challenge_edit_window_next,
                UnlockMethodUtils.formatDateTime(nextAvailableMillis)
            )
        } else {
            binding.tvChallengeSecondary.visibility = View.GONE
        }
        binding.btnGoBack.visibility = if (showGoBackButton) View.VISIBLE else View.GONE
        binding.btnGoBack.setOnClickListener { onGoBack?.invoke() }
    }

    private fun showTimerInfo(block: AppBlock, showGoBackButton: Boolean, onGoBack: (() -> Unit)?) {
        val binding = ViewUnlockInfoBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.unlock_method_timer)
        binding.tvChallengeBody.text = activity.getString(
            R.string.challenge_timer_locked,
            UnlockMethodUtils.formatDateTime(block.lockUntil)
        )
        binding.btnGoBack.visibility = if (showGoBackButton) View.VISIBLE else View.GONE
        binding.btnGoBack.setOnClickListener { onGoBack?.invoke() }
        val remainingMillis = (block.lockUntil - System.currentTimeMillis()).coerceAtLeast(0L)
        updateTimerMessage(binding, remainingMillis)
        if (remainingMillis <= 0L) return
        timer = object : CountDownTimer(remainingMillis, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerMessage(binding, millisUntilFinished)
            }

            override fun onFinish() {
                binding.tvChallengeSecondary.text = activity.getString(
                    R.string.challenge_timer_remaining,
                    UnlockMethodUtils.formatCountdown(0L)
                )
            }
        }.start()
    }

    private fun showWhileActiveInfo(block: AppBlock, showGoBackButton: Boolean, onGoBack: (() -> Unit)?) {
        val binding = ViewUnlockInfoBinding.inflate(LayoutInflater.from(activity), container, false)
        container.addView(binding.root)
        binding.tvChallengeTitle.text = activity.getString(R.string.unlock_method_while_active)
        binding.tvChallengeBody.text = activity.getString(R.string.unlock_while_active_desc)
        binding.tvChallengeSecondary.visibility = View.GONE
        binding.btnGoBack.visibility = if (showGoBackButton) View.VISIBLE else View.GONE
        binding.btnGoBack.setOnClickListener { onGoBack?.invoke() }
    }

    private fun updateTimerMessage(binding: ViewUnlockInfoBinding, millisUntilFinished: Long) {
        binding.tvChallengeSecondary.visibility = View.VISIBLE
        binding.tvChallengeSecondary.text = activity.getString(
            R.string.challenge_timer_remaining,
            UnlockMethodUtils.formatCountdown(millisUntilFinished)
        )
    }

    private fun setupErrorClearing(editText: TextInputEditText) {
        editText.doAfterTextChanged { hideError() }
    }

    private fun showError(message: String) {
        errorView.visibility = View.VISIBLE
        errorView.text = message
    }

    private fun hideError() {
        errorView.visibility = View.GONE
        errorView.text = ""
    }
}
