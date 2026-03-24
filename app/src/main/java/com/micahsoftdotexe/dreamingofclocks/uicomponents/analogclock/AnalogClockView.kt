package com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.toColorInt
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.models.HandConfig
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var template: ClockTemplate = ClockTemplate.CLASSIC
    private var handColor: Int = Color.WHITE
    private var cachedTicks: List<TickLine>? = null

    var radiusFractionOverride: Float? = null
        set(value) { field = value; cachedTicks = null; invalidate() }

    private data class TickLine(val startX: Float, val startY: Float, val endX: Float, val endY: Float, val strokeWidth: Float)

    private val faceFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val faceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val hourNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val secondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            invalidate()
            val delay = if (template.hands.second.show) 1000L else 60_000L
            handler.postDelayed(this, delay)
        }
    }

    private val romanNumerals = arrayOf(
        "XII", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI"
    )

    init {
        configurePaints()
    }

    fun setTemplate(t: ClockTemplate) {
        template = t
        cachedTicks = null
        configurePaints()
        invalidate()
    }

    fun setHandColor(color: Int) {
        handColor = color
        configurePaints()
        invalidate()
    }

    private fun configurePaints() {
        val face = template.face
        val hands = template.hands

        faceFillPaint.color = parseColor(face.fillColor, handColor)
        faceBorderPaint.color = parseColor(face.borderColor, handColor)
        faceBorderPaint.strokeWidth = dpToPx(face.borderWidthDp)
        tickPaint.color = handColor
        hourNumberPaint.color = handColor
        hourHandPaint.apply {
            strokeWidth = dpToPx(hands.hour.widthDp)
            color = parseColor(hands.hour.color, handColor)
            strokeCap = parseCap(hands.hour.cap)
        }
        minuteHandPaint.apply {
            strokeWidth = dpToPx(hands.minute.widthDp)
            color = parseColor(hands.minute.color, handColor)
            strokeCap = parseCap(hands.minute.cap)
        }
        secondHandPaint.apply {
            strokeWidth = dpToPx(hands.second.widthDp)
            color = parseColor(hands.second.color, handColor)
            strokeCap = parseCap(hands.second.cap)
        }
        centerDotPaint.color = handColor
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cachedTicks = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val face = template.face
        val hands = template.hands

        val cx = width / 2f
        val cy = height / 2f
        val base = min(width, height).toFloat()
        val radius = base * (radiusFractionOverride ?: face.radiusFraction)

        // Face fill
        if (face.fillColor != null) {
            canvas.drawCircle(cx, cy, radius, faceFillPaint)
        }

        // Border
        if (face.borderWidthDp > 0) {
            canvas.drawCircle(cx, cy, radius, faceBorderPaint)
        }

        // Tick marks
        drawTicks(canvas, cx, cy, radius, base)

        // Hour numbers
        if (face.showHourNumbers && face.numberStyle != "none") {
            drawHourNumbers(canvas, cx, cy, radius, base)
        }

        // Get current time
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // Hour hand
        val hourAngle = (hour % 12 + minute / 60f) * 30f - 90f
        drawHand(canvas, cx, cy, radius, hands.hour, hourAngle, hourHandPaint)

        // Minute hand
        val minuteAngle = (minute + second / 60f) * 6f - 90f
        drawHand(canvas, cx, cy, radius, hands.minute, minuteAngle, minuteHandPaint)

        // Second hand
        if (hands.second.show) {
            val secondAngle = second * 6f - 90f
            drawSecondHand(canvas, cx, cy, radius, hands.second, secondAngle)
        }

        // Center dot
        if (face.showCenterDot) {
            canvas.drawCircle(cx, cy, base * face.centerDotRadiusFraction, centerDotPaint)
        }
    }

    private fun buildTicks(cx: Float, cy: Float, radius: Float, base: Float): List<TickLine> {
        val face = template.face
        val ticks = mutableListOf<TickLine>()
        for (i in 0 until 60) {
            val isHourMark = i % 5 == 0
            val show = if (isHourMark) face.showHourTicks else face.showMinuteTicks
            if (!show) continue
            val lengthFraction = if (isHourMark) face.hourTickLengthFraction else face.minuteTickLengthFraction
            val widthDp = if (isHourMark) face.hourTickWidthDp else face.minuteTickWidthDp
            val tickLength = base * lengthFraction
            val angle = Math.toRadians((i * 6f - 90f).toDouble())
            ticks.add(TickLine(
                startX = cx + (radius - tickLength) * cos(angle).toFloat(),
                startY = cy + (radius - tickLength) * sin(angle).toFloat(),
                endX = cx + radius * cos(angle).toFloat(),
                endY = cy + radius * sin(angle).toFloat(),
                strokeWidth = dpToPx(widthDp)
            ))
        }
        return ticks
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float, base: Float) {
        val face = template.face
        if (!face.showHourTicks && !face.showMinuteTicks) return
        val ticks = cachedTicks ?: buildTicks(cx, cy, radius, base).also { cachedTicks = it }
        for (tick in ticks) {
            tickPaint.strokeWidth = tick.strokeWidth
            canvas.drawLine(tick.startX, tick.startY, tick.endX, tick.endY, tickPaint)
        }
    }

    private fun drawHourNumbers(canvas: Canvas, cx: Float, cy: Float, radius: Float, base: Float) {
        val face = template.face
        val radiusScale = radiusFractionOverride?.let { it / face.radiusFraction } ?: 1f
        val fontSize = base * face.numberFontSizeFraction * radiusScale
        hourNumberPaint.textSize = fontSize

        val fm = hourNumberPaint.fontMetrics
        val textCenterOffset = -(fm.ascent + fm.descent) / 2f
        val numberRadius = radius - base * 0.1f

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30f - 90f).toDouble())
            val nx = cx + numberRadius * cos(angle).toFloat()
            val ny = cy + numberRadius * sin(angle).toFloat() + textCenterOffset

            val text = when (face.numberStyle) {
                "roman" -> romanNumerals[i]
                else -> if (i == 0) "12" else i.toString()
            }
            canvas.drawText(text, nx, ny, hourNumberPaint)
        }
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, radius: Float, hand: HandConfig, angleDeg: Float, handPaint: Paint) {
        val angle = Math.toRadians(angleDeg.toDouble())
        val length = radius * hand.lengthFraction
        val tail = radius * hand.tailFraction

        val endX = cx + length * cos(angle).toFloat()
        val endY = cy + length * sin(angle).toFloat()
        val startX = cx - tail * cos(angle).toFloat()
        val startY = cy - tail * sin(angle).toFloat()

        canvas.drawLine(startX, startY, endX, endY, handPaint)
    }

    private fun drawSecondHand(canvas: Canvas, cx: Float, cy: Float, radius: Float, hand: com.micahsoftdotexe.dreamingofclocks.models.SecondHandConfig, angleDeg: Float) {
        val angle = Math.toRadians(angleDeg.toDouble())
        val length = radius * hand.lengthFraction
        val tail = radius * hand.tailFraction

        val endX = cx + length * cos(angle).toFloat()
        val endY = cy + length * sin(angle).toFloat()
        val startX = cx - tail * cos(angle).toFloat()
        val startY = cy - tail * sin(angle).toFloat()

        canvas.drawLine(startX, startY, endX, endY, secondHandPaint)
    }

    private fun parseColor(hex: String?, fallback: Int): Int {
        if (hex.isNullOrEmpty()) return fallback
        return try { hex.toColorInt() } catch (_: Exception) { fallback }
    }

    private fun parseCap(cap: String): Paint.Cap = when (cap) {
        "square" -> Paint.Cap.SQUARE
        "butt" -> Paint.Cap.BUTT
        else -> Paint.Cap.ROUND
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
