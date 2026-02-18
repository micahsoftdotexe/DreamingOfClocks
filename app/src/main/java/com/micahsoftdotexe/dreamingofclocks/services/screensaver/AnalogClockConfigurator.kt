package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.models.WidgetPosition
import com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock.AnalogClockView
import kotlin.math.min

object AnalogClockConfigurator {

    fun configureAnalogClock(
        clockView: AnalogClockView,
        template: ClockTemplate,
        config: PreferencesManager.ScreensaverConfig
    ) {
        clockView.setTemplate(template)
        try {
            clockView.setHandColor(config.analogHandColor.toColorInt())
        } catch (_: Exception) {
            // keep default
        }
    }

    fun positionWidgets(
        container: FrameLayout,
        dateText: TextView,
        alarmText: TextView,
        mediaText: TextView,
        template: ClockTemplate,
        config: PreferencesManager.ScreensaverConfig
    ) {
        // Apply text colors
        try {
            val tc = config.textColor.toColorInt()
            dateText.setTextColor(tc)
            alarmText.setTextColor(tc)
            mediaText.setTextColor(tc)
        } catch (_: Exception) { }

        // Apply font sizes from template
        dateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.date.fontSizeSp.toFloat())
        alarmText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.alarm.fontSizeSp.toFloat())
        mediaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.media.fontSizeSp.toFloat())

        // Position widgets after layout is measured
        container.post {
            val w = container.width
            val h = container.height
            val base = min(w, h).toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val radius = base * template.face.radiusFraction

            applyWidgetPosition(dateText, template.widgets.date, cx, cy, radius, base, w, h)
            applyWidgetPosition(alarmText, template.widgets.alarm, cx, cy, radius, base, w, h)
            applyWidgetPosition(mediaText, template.widgets.media, cx, cy, radius, base, w, h)
        }
    }

    private fun applyWidgetPosition(
        textView: TextView,
        wp: WidgetPosition,
        cx: Float, cy: Float, radius: Float, base: Float,
        containerW: Int, containerH: Int
    ) {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        // Compute target position based on the position keyword
        val targetX: Float
        val targetY: Float

        when (wp.position) {
            "above" -> {
                targetX = cx + base * wp.offsetXFraction
                targetY = cy - radius + base * wp.offsetYFraction
            }
            "below" -> {
                targetX = cx + base * wp.offsetXFraction
                targetY = cy + radius + base * wp.offsetYFraction
            }
            "left" -> {
                targetX = cx - radius + base * wp.offsetXFraction
                targetY = cy + base * wp.offsetYFraction
            }
            "right" -> {
                targetX = cx + radius + base * wp.offsetXFraction
                targetY = cy + base * wp.offsetYFraction
            }
            "on_face" -> {
                targetX = cx + base * wp.offsetXFraction
                targetY = cy + base * wp.offsetYFraction
            }
            else -> {
                targetX = cx + base * wp.offsetXFraction
                targetY = cy + radius + base * wp.offsetYFraction
            }
        }

        // FrameLayout uses margins from top-left, with gravity to handle centering on the point
        params.gravity = Gravity.TOP or Gravity.START

        // Measure the text view to get its dimensions for centering
        textView.measure(
            View.MeasureSpec.makeMeasureSpec(containerW, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(containerH, View.MeasureSpec.AT_MOST)
        )
        val tvW = textView.measuredWidth
        val tvH = textView.measuredHeight

        params.leftMargin = (targetX - tvW / 2f).toInt().coerceIn(0, containerW - tvW)
        params.topMargin = (targetY - tvH / 2f).toInt().coerceIn(0, containerH - tvH)

        textView.layoutParams = params
    }
}
