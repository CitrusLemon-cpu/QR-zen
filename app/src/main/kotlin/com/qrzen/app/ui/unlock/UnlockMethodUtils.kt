package com.qrzen.app.ui.unlock

import android.content.Context
import com.qrzen.app.R
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.TimeBlock
import com.qrzen.app.data.prefs.Prefs
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object UnlockMethodUtils {
    const val METHOD_NONE = "NONE"
    const val METHOD_DELAY = "DELAY"
    const val METHOD_PASSWORD = "PASSWORD"
    const val METHOD_TYPE_OVER_TEXT = "TYPE_OVER_TEXT"
    const val METHOD_QR_CODE = "QR_CODE"
    const val METHOD_EDIT_WINDOW = "EDIT_WINDOW"
    const val METHOD_TIMER = "TIMER"
    const val METHOD_WHILE_ACTIVE = "WHILE_ACTIVE"
    const val STYLE_MANUAL = "MANUAL"
    const val STYLE_SCHEDULE = "SCHEDULE"
    const val STYLE_USAGE_LIMIT = "USAGE_LIMIT"
    const val STYLE_WAIT_TIMER = "WAIT_TIMER"
    const val STYLE_POMODORO = "POMODORO"
    const val BREAK_NONE = "NONE"
    const val BREAK_POMODORO = "POMODORO"
    const val BREAK_WAIT_TIMER = "WAIT_TIMER"
    const val BREAK_USAGE_LIMIT = "USAGE_LIMIT"
    const val BREAK_SCHEDULED_ALLOWANCE = "SCHEDULED_ALLOWANCE"

    private val displayDateTimeFormatter = SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault())
    private val challengeWords = listOf(
        "river", "stone", "forest", "window", "planet", "coffee", "garden", "signal",
        "silver", "pencil", "sunrise", "meadow", "harbor", "candle", "bridge", "ocean",
        "yellow", "quiet", "thunder", "paper", "lantern", "mirror", "breeze", "winter",
        "summer", "travel", "rocket", "puzzle", "future", "gentle", "mountain", "dream"
    )

    data class EditWindowAvailability(
        val isAvailable: Boolean,
        val nextAvailableMillis: Long?
    )

    data class SchedulePomodoroPhase(
        val isActive: Boolean,
        val isInFocus: Boolean,
        val phaseRemainingMs: Long,
        val focusDurationMin: Int,
        val breakDurationMin: Int
    )

    fun getNormalizedMethod(block: AppBlock): String = block.unlockMethod.ifBlank { METHOD_NONE }

    fun isTimerExpired(block: AppBlock, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis >= block.lockUntil
    }

    fun getEditWindowAvailability(
        block: AppBlock,
        now: LocalDateTime = LocalDateTime.now()
    ): EditWindowAvailability {
        val startTime = parseTime(block.editWindowStart, LocalTime.of(9, 0))
        val endTime = parseTime(block.editWindowEnd, LocalTime.of(10, 0))
        val today = now.toLocalDate()
        var available = false
        var nextStart: LocalDateTime? = null

        for (offset in -1..14) {
            val date = today.plusDays(offset.toLong())
            if (!isDayActive(block.editWindowDays, date.dayOfWeek)) continue
            val start = date.atTime(startTime)
            val end = if (endTime <= startTime) date.plusDays(1).atTime(endTime) else date.atTime(endTime)
            if (!now.isBefore(start) && now.isBefore(end)) {
                available = true
            }
            if (start.isAfter(now) && (nextStart == null || start.isBefore(nextStart))) {
                nextStart = start
            }
        }

        return EditWindowAvailability(
            isAvailable = available,
            nextAvailableMillis = nextStart?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
    }

    fun formatWindowSchedule(block: AppBlock): String {
        return "${block.editWindowStart}–${block.editWindowEnd} on ${formatDays(block.editWindowDays)}"
    }

    fun formatDays(days: String): String {
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val active = days.padEnd(7, '0').mapIndexedNotNull { index, value ->
            if (value == '1') names.getOrNull(index) else null
        }
        return if (active.isEmpty()) "No days" else active.joinToString(", ")
    }

    fun formatDateTime(epochMillis: Long): String {
        return displayDateTimeFormatter.format(Date(epochMillis))
    }

    fun formatCountdown(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    fun getTypeOverChallengeText(block: AppBlock, existingText: String? = null): String {
        if (!block.typeOverIsRandom) return block.typeOverText
        return existingText ?: generateRandomTypeOverText()
    }

    fun isBlockCurrentlyActive(block: AppBlock, timeBlocks: List<TimeBlock>): Boolean {
        if (block.blockNowUntil > System.currentTimeMillis()) return true
        if (block.blockingStyle == STYLE_MANUAL) return true
        if (block.blockingStyle == STYLE_POMODORO) {
            val state = computePomodoroState(block)
            return state.isInFocus
        }
        if (block.blockingStyle != STYLE_SCHEDULE) return true

        val scheduleActive = isScheduleCurrentlyActive(block, timeBlocks)
        if (!scheduleActive) return false

        return when (block.scheduleBreakType.ifBlank { BREAK_NONE }) {
            BREAK_NONE -> true
            BREAK_POMODORO -> isSchedulePomodoroBlocking(block, timeBlocks)
            BREAK_WAIT_TIMER -> Prefs.getScheduleWtBlockingUntil(block.id) > System.currentTimeMillis()
            BREAK_USAGE_LIMIT -> true
            BREAK_SCHEDULED_ALLOWANCE -> Prefs.getSchedAllowanceRemaining(block.id) == 0L
            else -> true
        }
    }

    fun getUnlockMethodSummary(context: Context, block: AppBlock, nowMillis: Long = System.currentTimeMillis()): String? {
        return when (getNormalizedMethod(block)) {
            METHOD_NONE -> null
            METHOD_DELAY -> "${context.getString(R.string.unlock_method_delay)} (${block.delayMinutes} min)"
            METHOD_PASSWORD -> context.getString(R.string.unlock_method_password)
            METHOD_TYPE_OVER_TEXT -> context.getString(R.string.unlock_method_type_over)
            METHOD_QR_CODE -> context.getString(R.string.unlock_method_qr_code)
            METHOD_EDIT_WINDOW -> {
                val schedule = formatWindowSchedule(block)
                "${context.getString(R.string.unlock_method_edit_window)} ($schedule)"
            }
            METHOD_WHILE_ACTIVE -> context.getString(R.string.unlock_method_while_active)
            METHOD_TIMER -> {
                if (block.lockUntil > nowMillis) {
                    context.getString(R.string.challenge_timer_locked, formatDateTime(block.lockUntil))
                } else {
                    null
                }
            }
            else -> block.unlockMethod
        }
    }

    private fun generateRandomTypeOverText(): String {
        val targetLength = Random.nextInt(50, 101)
        val builder = StringBuilder()
        while (builder.length < targetLength) {
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(challengeWords.random())
        }
        if (builder.length <= 100) return builder.toString()
        val trimmed = builder.substring(0, 100)
        val lastSpace = trimmed.lastIndexOf(' ')
        return if (lastSpace >= 50) trimmed.substring(0, lastSpace) else trimmed
    }

    private fun isDayActive(days: String, dayOfWeek: DayOfWeek): Boolean {
        val index = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
        return days.padEnd(7, '0').getOrNull(index) == '1'
    }

    private fun parseTime(value: String, fallback: LocalTime): LocalTime {
        return runCatching { LocalTime.parse(value) }.getOrDefault(fallback)
    }

    fun isScheduleCurrentlyActive(
        block: AppBlock,
        timeBlocks: List<TimeBlock>,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return if (timeBlocks.isEmpty()) {
            computeLegacyScheduleWindowStartMs(block, nowMillis) != null
        } else {
            computeCurrentWindowStartMs(timeBlocks, nowMillis) != null
        }
    }

    fun computeCurrentWindowStartMs(
        timeBlocks: List<TimeBlock>,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        return findCurrentWindow(timeBlocks, nowMillis)?.windowStartMillis
    }

    fun computeLegacyScheduleWindowStartMs(
        block: AppBlock,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val pseudoBlock = TimeBlock(
            blockId = block.id,
            startTime = block.startTime,
            endTime = block.endTime,
            activeDays = block.activeDays
        )
        return findCurrentWindow(listOf(pseudoBlock), nowMillis)?.windowStartMillis
    }

    private data class ActiveWindow(val windowStartMillis: Long)

    fun computeSchedulePomodoroPhase(
        block: AppBlock,
        timeBlocks: List<TimeBlock>,
        nowMillis: Long = System.currentTimeMillis()
    ): SchedulePomodoroPhase {
        val windowStartMs = if (timeBlocks.isEmpty()) {
            computeLegacyScheduleWindowStartMs(block, nowMillis)
        } else {
            computeCurrentWindowStartMs(timeBlocks, nowMillis)
        } ?: return SchedulePomodoroPhase(
            isActive = false,
            isInFocus = false,
            phaseRemainingMs = 0L,
            focusDurationMin = block.pomodoroDurationMin,
            breakDurationMin = block.pomodoroBreakMin
        )

        val elapsed = nowMillis - windowStartMs
        if (elapsed < 0L) {
            return SchedulePomodoroPhase(
                isActive = true,
                isInFocus = true,
                phaseRemainingMs = block.pomodoroDurationMin * 60_000L,
                focusDurationMin = block.pomodoroDurationMin,
                breakDurationMin = block.pomodoroBreakMin
            )
        }

        val focusMs = block.pomodoroDurationMin * 60_000L
        val breakMs = block.pomodoroBreakMin * 60_000L
        val cycleMs = focusMs + breakMs
        if (cycleMs <= 0L) {
            return SchedulePomodoroPhase(
                isActive = true,
                isInFocus = true,
                phaseRemainingMs = 0L,
                focusDurationMin = block.pomodoroDurationMin,
                breakDurationMin = block.pomodoroBreakMin
            )
        }

        val positionInCycle = elapsed % cycleMs
        val isInFocus = positionInCycle < focusMs
        val phaseRemainingMs = if (isInFocus) {
            focusMs - positionInCycle
        } else {
            cycleMs - positionInCycle
        }

        return SchedulePomodoroPhase(
            isActive = true,
            isInFocus = isInFocus,
            phaseRemainingMs = phaseRemainingMs,
            focusDurationMin = block.pomodoroDurationMin,
            breakDurationMin = block.pomodoroBreakMin
        )
    }

    private fun isSchedulePomodoroBlocking(
        block: AppBlock,
        timeBlocks: List<TimeBlock>,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val phase = computeSchedulePomodoroPhase(block, timeBlocks, nowMillis)
        return !phase.isActive || phase.isInFocus
    }

    private fun findCurrentWindow(
        timeBlocks: List<TimeBlock>,
        nowMillis: Long
    ): ActiveWindow? {
        if (timeBlocks.isEmpty()) return null

        val zoneId = ZoneId.systemDefault()
        val nowDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zoneId)
        val today = nowDateTime.toLocalDate()
        val yesterday = today.minusDays(1)
        val now = nowDateTime.toLocalTime()
        val dayIndex = today.dayOfWeek.value - 1
        val yesterdayIndex = (dayIndex + 6) % 7

        return timeBlocks.firstNotNullOfOrNull { timeBlock ->
            val start = parseTime(timeBlock.startTime, LocalTime.MIN)
            val end = parseTime(timeBlock.endTime, LocalTime.MAX)
            if (end.isAfter(start)) {
                if (timeBlock.activeDays.getOrNull(dayIndex) == '1' &&
                    !now.isBefore(start) &&
                    !now.isAfter(end)
                ) {
                    ActiveWindow(toEpochMillis(today, start, zoneId))
                } else {
                    null
                }
            } else {
                when {
                    !now.isBefore(start) && timeBlock.activeDays.getOrNull(dayIndex) == '1' ->
                        ActiveWindow(toEpochMillis(today, start, zoneId))
                    !now.isAfter(end) && timeBlock.activeDays.getOrNull(yesterdayIndex) == '1' ->
                        ActiveWindow(toEpochMillis(yesterday, start, zoneId))
                    else -> null
                }
            }
        }
    }

    private fun toEpochMillis(date: LocalDate, time: LocalTime, zoneId: ZoneId): Long {
        return date.atTime(time).atZone(zoneId).toInstant().toEpochMilli()
    }

    data class PomodoroState(
        val isSessionActive: Boolean,
        val isInFocus: Boolean,
        val isInBreak: Boolean,
        val currentRound: Int,
        val totalRounds: Int,
        val periodRemainingMs: Long,
        val sessionRemainingMs: Long,
        val sessionEndMillis: Long
    )

    fun computePomodoroState(block: AppBlock, nowMillis: Long = System.currentTimeMillis()): PomodoroState {
        val rounds = block.pomodoroRoundsTotal
        val sessionStart = block.pomodoroSessionStartMillis
        if (rounds <= 0 || sessionStart <= 0L) {
            return PomodoroState(false, false, false, 0, 0, 0L, 0L, 0L)
        }
        val focusMs = block.pomodoroDurationMin * 60_000L
        val breakMs = block.pomodoroBreakMin * 60_000L
        val totalSessionMs = focusMs * rounds + breakMs * (rounds - 1)
        val sessionEnd = sessionStart + totalSessionMs
        val elapsed = nowMillis - sessionStart

        if (elapsed < 0L || elapsed >= totalSessionMs) {
            return PomodoroState(false, false, false, rounds, rounds, 0L, 0L, sessionEnd)
        }

        val roundCycleMs = focusMs + breakMs
        val currentCycle = (elapsed / roundCycleMs).toInt()
        val timeInCycle = elapsed % roundCycleMs

        if (currentCycle >= rounds - 1) {
            val elapsedInLastRound = elapsed - (rounds - 1).toLong() * roundCycleMs
            return if (elapsedInLastRound < focusMs) {
                PomodoroState(
                    isSessionActive = true,
                    isInFocus = true,
                    isInBreak = false,
                    currentRound = rounds,
                    totalRounds = rounds,
                    periodRemainingMs = focusMs - elapsedInLastRound,
                    sessionRemainingMs = totalSessionMs - elapsed,
                    sessionEndMillis = sessionEnd
                )
            } else {
                PomodoroState(false, false, false, rounds, rounds, 0L, 0L, sessionEnd)
            }
        }

        return if (timeInCycle < focusMs) {
            PomodoroState(
                isSessionActive = true,
                isInFocus = true,
                isInBreak = false,
                currentRound = currentCycle + 1,
                totalRounds = rounds,
                periodRemainingMs = focusMs - timeInCycle,
                sessionRemainingMs = totalSessionMs - elapsed,
                sessionEndMillis = sessionEnd
            )
        } else {
            PomodoroState(
                isSessionActive = true,
                isInFocus = false,
                isInBreak = true,
                currentRound = currentCycle + 1,
                totalRounds = rounds,
                periodRemainingMs = roundCycleMs - timeInCycle,
                sessionRemainingMs = totalSessionMs - elapsed,
                sessionEndMillis = sessionEnd
            )
        }
    }
}
