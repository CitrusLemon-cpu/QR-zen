package com.qrzen.app.service

import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.ui.unlock.UnlockMethodUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessibilityRulePolicyTest {
    private val packageName = "com.example.blocked"

    @Test
    fun allowlistNeverRequiresAccessibility() {
        val block = waitTimerBlock(isAllowlist = true)
        assertNull(AccessibilityRulePolicy.requirementFor(block, packageName, 0, false))
    }

    @Test
    fun unlistedPackageNeverRequiresAccessibility() {
        assertNull(AccessibilityRulePolicy.requirementFor(waitTimerBlock(), "com.example.other", 0, false))
    }

    @Test
    fun standaloneRuleHonorsActiveDay() {
        val block = waitTimerBlock(activeDays = "1000000")
        assertEquals(
            AccessibilityRulePolicy.Requirement.WAIT_TIMER,
            AccessibilityRulePolicy.requirementFor(block, packageName, 0, false)
        )
        assertNull(AccessibilityRulePolicy.requirementFor(block, packageName, 1, false))
    }

    @Test
    fun scheduledRuleOnlyRequiresAccessibilityDuringSchedule() {
        val block = AppBlock(
            appPackages = packageName,
            blockingStyle = UnlockMethodUtils.STYLE_SCHEDULE,
            scheduleBreakType = UnlockMethodUtils.BREAK_USAGE_LIMIT
        )
        assertNull(AccessibilityRulePolicy.requirementFor(block, packageName, 0, false))
        assertEquals(
            AccessibilityRulePolicy.Requirement.USAGE_LIMIT,
            AccessibilityRulePolicy.requirementFor(block, packageName, 0, true)
        )
    }

    @Test
    fun scheduledAllowanceHasItsOwnRequirement() {
        val block = AppBlock(
            appPackages = packageName,
            blockingStyle = UnlockMethodUtils.STYLE_SCHEDULE,
            scheduleBreakType = UnlockMethodUtils.BREAK_SCHEDULED_ALLOWANCE
        )
        assertEquals(
            AccessibilityRulePolicy.Requirement.SCHEDULED_ALLOWANCE,
            AccessibilityRulePolicy.requirementFor(block, packageName, 0, true)
        )
    }

    private fun waitTimerBlock(
        isAllowlist: Boolean = false,
        activeDays: String = "1111111"
    ) = AppBlock(
        appPackages = packageName,
        isAllowlistMode = isAllowlist,
        activeDays = activeDays,
        blockingStyle = UnlockMethodUtils.STYLE_WAIT_TIMER
    )
}
