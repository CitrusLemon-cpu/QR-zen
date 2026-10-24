package com.qrzen.app.service

import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.ui.unlock.UnlockMethodUtils

/** Pure policy for deciding when a foreground app requires Accessibility tracking. */
object AccessibilityRulePolicy {

    enum class Requirement {
        WAIT_TIMER,
        USAGE_LIMIT,
        SCHEDULED_ALLOWANCE
    }

    fun requirementFor(
        block: AppBlock,
        foregroundPackage: String,
        dayIndex: Int,
        scheduleActive: Boolean
    ): Requirement? {
        if (block.isAllowlistMode || block.blockAudio) return null
        if (!containsPackage(block, foregroundPackage)) return null

        return when (block.blockingStyle) {
            UnlockMethodUtils.STYLE_WAIT_TIMER -> {
                if (isActiveToday(block, dayIndex)) Requirement.WAIT_TIMER else null
            }
            UnlockMethodUtils.STYLE_USAGE_LIMIT -> {
                if (isActiveToday(block, dayIndex)) Requirement.USAGE_LIMIT else null
            }
            UnlockMethodUtils.STYLE_SCHEDULE -> {
                if (!scheduleActive) return null
                when (block.scheduleBreakType.ifBlank { UnlockMethodUtils.BREAK_NONE }) {
                    UnlockMethodUtils.BREAK_WAIT_TIMER -> Requirement.WAIT_TIMER
                    UnlockMethodUtils.BREAK_USAGE_LIMIT -> Requirement.USAGE_LIMIT
                    UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE -> Requirement.SCHEDULED_ALLOWANCE
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun containsPackage(block: AppBlock, packageName: String): Boolean =
        block.appPackages.split(',').any { it.trim() == packageName }

    private fun isActiveToday(block: AppBlock, dayIndex: Int): Boolean =
        block.activeDays.getOrNull(dayIndex) == '1'
}
