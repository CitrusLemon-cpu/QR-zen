package com.qrzen.app.ui.block

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.qrzen.app.data.model.TimeBlock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WeeklyScheduleGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnBlockInteractionListener {
        fun onBlockSelected(timeBlock: TimeBlock)
        fun onBlockDeselected()
    }

    private data class RenderedBlock(val timeBlock: TimeBlock, val rect: RectF)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")
    private val timeLabels = listOf("00:00", "06:00", "12:00", "18:00", "00:00")

    private val density = resources.displayMetrics.density
    private val defaultHeightPx = (300f * density).toInt()
    private val headerHeight = 36f * density
    private val leftLabelWidth = 56f * density
    private val columnGap = 4f * density
    private val blockInset = 3f * density

    private val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        color = MaterialColors.getColor(
            this@WeeklyScheduleGridView,
            com.google.android.material.R.attr.colorOnSurface,
            Color.WHITE
        )
    }

    private val timeLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
        color = MaterialColors.getColor(
            this@WeeklyScheduleGridView,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            Color.LTGRAY
        )
    }

    private val columnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(
            this@WeeklyScheduleGridView,
            com.google.android.material.R.attr.colorSurfaceVariant,
            Color.parseColor("#424242")
        )
    }

    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density
        color = ColorUtils.setAlphaComponent(
            MaterialColors.getColor(
                this@WeeklyScheduleGridView,
                com.google.android.material.R.attr.colorOutline,
                Color.LTGRAY
            ),
            72
        )
    }

    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ColorUtils.setAlphaComponent(
            MaterialColors.getColor(
                this@WeeklyScheduleGridView,
                com.google.android.material.R.attr.colorPrimary,
                Color.parseColor("#A5D6A7")
            ),
            102
        )
    }

    private val selectedBlockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = MaterialColors.getColor(
            this@WeeklyScheduleGridView,
            com.google.android.material.R.attr.colorPrimary,
            Color.parseColor("#66BB6A")
        )
    }

    private val selectedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = MaterialColors.getColor(
            this@WeeklyScheduleGridView,
            com.google.android.material.R.attr.colorPrimary,
            Color.parseColor("#66BB6A")
        )
    }

    private var timeBlocks: List<TimeBlock> = emptyList()
    private var selectedBlockId: Int? = null
    private var listener: OnBlockInteractionListener? = null
    private var renderedBlocks: List<RenderedBlock> = emptyList()

    fun setTimeBlocks(blocks: List<TimeBlock>) {
        timeBlocks = blocks.sortedBy { it.id }
        renderedBlocks = emptyList()
        invalidate()
    }

    fun getTimeBlocks(): List<TimeBlock> = timeBlocks

    fun setSelectedBlockId(id: Int?) {
        if (selectedBlockId == id) return
        selectedBlockId = id
        invalidate()
    }

    fun getSelectedBlockId(): Int? = selectedBlockId

    fun setOnBlockInteractionListener(listener: OnBlockInteractionListener?) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = paddingLeft + paddingRight + leftLabelWidth.toInt() + (7 * 32f * density).toInt()
        val measuredWidth = resolveSize(minWidth, widthMeasureSpec)
        val desiredHeight = maxOf(defaultHeightPx, suggestedMinimumHeight)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = width.toFloat() - paddingRight.toFloat()
        val contentBottom = height.toFloat() - paddingBottom.toFloat()
        val gridLeft = contentLeft + leftLabelWidth
        val gridTop = contentTop + headerHeight
        val gridWidth = (contentRight - gridLeft).coerceAtLeast(0f)
        val gridHeight = (contentBottom - gridTop).coerceAtLeast(0f)
        if (gridWidth <= 0f || gridHeight <= 0f) return

        val columnWidth = gridWidth / 7f
        val rowHeight = gridHeight / 24f

        drawDayHeaders(canvas, gridLeft, contentTop, columnWidth)
        drawTimeLabels(canvas, contentLeft, gridTop, gridHeight)
        drawColumns(canvas, gridLeft, gridTop, columnWidth, gridHeight)
        drawGridLines(canvas, gridLeft, gridTop, gridWidth, rowHeight)
        drawBlocks(canvas, gridLeft, gridTop, columnWidth, gridHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                handleTap(event.x, event.y)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> return false
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun drawDayHeaders(canvas: Canvas, gridLeft: Float, top: Float, columnWidth: Float) {
        val baseline = top + headerHeight / 2f - (headerTextPaint.descent() + headerTextPaint.ascent()) / 2f
        dayHeaders.forEachIndexed { index, label ->
            val x = gridLeft + (index * columnWidth) + (columnWidth / 2f)
            canvas.drawText(label, x, baseline, headerTextPaint)
        }
    }

    private fun drawTimeLabels(canvas: Canvas, contentLeft: Float, gridTop: Float, gridHeight: Float) {
        timeLabels.forEachIndexed { index, label ->
            val fraction = index / 4f
            val y = gridTop + (fraction * gridHeight)
            val baseline = when (index) {
                0 -> y - timeLabelPaint.ascent()
                timeLabels.lastIndex -> y
                else -> y - (timeLabelPaint.descent() + timeLabelPaint.ascent()) / 2f
            }
            canvas.drawText(label, contentLeft, baseline, timeLabelPaint)
        }
    }

    private fun drawColumns(
        canvas: Canvas,
        gridLeft: Float,
        gridTop: Float,
        columnWidth: Float,
        gridHeight: Float
    ) {
        for (dayIndex in 0 until 7) {
            val left = gridLeft + (dayIndex * columnWidth) + columnGap / 2f
            val right = gridLeft + ((dayIndex + 1) * columnWidth) - columnGap / 2f
            canvas.drawRoundRect(
                RectF(left, gridTop, right, gridTop + gridHeight),
                10f * density,
                10f * density,
                columnPaint
            )
        }
    }

    private fun drawGridLines(
        canvas: Canvas,
        gridLeft: Float,
        gridTop: Float,
        gridWidth: Float,
        rowHeight: Float
    ) {
        for (hour in 0..24) {
            val y = gridTop + (hour * rowHeight)
            canvas.drawLine(gridLeft, y, gridLeft + gridWidth, y, gridLinePaint)
        }
    }

    private fun drawBlocks(
        canvas: Canvas,
        gridLeft: Float,
        gridTop: Float,
        columnWidth: Float,
        gridHeight: Float
    ) {
        val rendered = mutableListOf<RenderedBlock>()
        timeBlocks.forEach { block ->
            buildBlockRects(block, gridLeft, gridTop, columnWidth, gridHeight).forEach { rect ->
                val isSelected = block.id == selectedBlockId
                val fillPaint = if (isSelected) selectedBlockPaint else blockPaint
                canvas.drawRoundRect(rect, 8f * density, 8f * density, fillPaint)
                if (isSelected) {
                    canvas.drawRoundRect(rect, 8f * density, 8f * density, selectedBorderPaint)
                }
                rendered += RenderedBlock(block, rect)
            }
        }
        renderedBlocks = rendered
    }

    private fun buildBlockRects(
        block: TimeBlock,
        gridLeft: Float,
        gridTop: Float,
        columnWidth: Float,
        gridHeight: Float
    ): List<RectF> {
        val startMinutes = parseMinutes(block.startTime)
        val endMinutes = parseMinutes(block.endTime)
        val segments = if (endMinutes <= startMinutes) {
            listOf(startMinutes to 1440, 0 to endMinutes)
        } else {
            listOf(startMinutes to endMinutes)
        }
        val activeDays = block.activeDays.padEnd(7, '0')
        val rects = mutableListOf<RectF>()
        for (dayIndex in 0 until 7) {
            if (activeDays.getOrNull(dayIndex) != '1') continue
            val left = gridLeft + (dayIndex * columnWidth) + (columnGap / 2f) + blockInset
            val right = gridLeft + ((dayIndex + 1) * columnWidth) - (columnGap / 2f) - blockInset
            segments.forEach { (start, end) ->
                val top = gridTop + (start / 1440f) * gridHeight + blockInset
                val bottom = gridTop + (end / 1440f) * gridHeight - blockInset
                if (bottom > top) {
                    rects += RectF(left, top, right, bottom)
                }
            }
        }
        return rects
    }

    private fun handleTap(x: Float, y: Float) {
        val tappedBlock = renderedBlocks.asReversed().firstOrNull { it.rect.contains(x, y) }?.timeBlock
        if (tappedBlock != null) {
            selectedBlockId = tappedBlock.id
            listener?.onBlockSelected(tappedBlock)
        } else {
            selectedBlockId = null
            listener?.onBlockDeselected()
        }
        invalidate()
    }

    private fun parseMinutes(value: String): Int {
        return runCatching {
            val localTime = LocalTime.parse(value, timeFormatter)
            localTime.hour * 60 + localTime.minute
        }.getOrElse {
            val parts = value.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            (hour.coerceIn(0, 23) * 60) + minute.coerceIn(0, 59)
        }
    }
}
