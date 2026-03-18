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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
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

    fun setTemplate(t: ClockTemplate) {
        template = t
        cachedTicks = null
        invalidate()
    }

    fun setHandColor(color: Int) {
        handColor = color
        invalidate()
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
        face.fillColor?.let { hex ->
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = parseColor(hex, handColor)
            canvas.drawCircle(cx, cy, radius, paint)
        }

        // Border
        if (face.borderWidthDp > 0) {
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dpToPx(face.borderWidthDp)
            paint.color = parseColor(face.borderColor, handColor)
            canvas.drawCircle(cx, cy, radius, paint)
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
        drawHand(canvas, cx, cy, radius, hands.hour, hourAngle)

        // Minute hand
        val minuteAngle = (minute + second / 60f) * 6f - 90f
        drawHand(canvas, cx, cy, radius, hands.minute, minuteAngle)

        // Second hand
        if (hands.second.show) {
            val secondAngle = second * 6f - 90f
            drawSecondHand(canvas, cx, cy, radius, hands.second, secondAngle)
        }

        // Center dot
        if (face.showCenterDot) {
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = handColor
            canvas.drawCircle(cx, cy, base * face.centerDotRadiusFraction, paint)
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
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = handColor
        paint.strokeCap = Paint.Cap.ROUND
        for (tick in ticks) {
            paint.strokeWidth = tick.strokeWidth
            canvas.drawLine(tick.startX, tick.startY, tick.endX, tick.endY, paint)
        }
    }

    private fun drawHourNumbers(canvas: Canvas, cx: Float, cy: Float, radius: Float, base: Float) {
        val face = template.face
        val radiusScale = radiusFractionOverride?.let { it / face.radiusFraction } ?: 1f
        val fontSize = base * face.numberFontSizeFraction * radiusScale
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = handColor
        paint.textSize = fontSize
        paint.textAlign = Paint.Align.CENTER

        val fm = paint.fontMetrics
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
            canvas.drawText(text, nx, ny, paint)
        }
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, radius: Float, hand: HandConfig, angleDeg: Float) {
        val angle = Math.toRadians(angleDeg.toDouble())
        val length = radius * hand.lengthFraction
        val tail = radius * hand.tailFraction

        val endX = cx + length * cos(angle).toFloat()
        val endY = cy + length * sin(angle).toFloat()
        val startX = cx - tail * cos(angle).toFloat()
        val startY = cy - tail * sin(angle).toFloat()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(hand.widthDp)
        paint.color = parseColor(hand.color, handColor)
        paint.strokeCap = parseCap(hand.cap)
        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    private fun drawSecondHand(canvas: Canvas, cx: Float, cy: Float, radius: Float, hand: com.micahsoftdotexe.dreamingofclocks.models.SecondHandConfig, angleDeg: Float) {
        val angle = Math.toRadians(angleDeg.toDouble())
        val length = radius * hand.lengthFraction
        val tail = radius * hand.tailFraction

        val endX = cx + length * cos(angle).toFloat()
        val endY = cy + length * sin(angle).toFloat()
        val startX = cx - tail * cos(angle).toFloat()
        val startY = cy - tail * sin(angle).toFloat()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(hand.widthDp)
        paint.color = parseColor(hand.color, handColor)
        paint.strokeCap = parseCap(hand.cap)
        canvas.drawLine(startX, startY, endX, endY, paint)
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
