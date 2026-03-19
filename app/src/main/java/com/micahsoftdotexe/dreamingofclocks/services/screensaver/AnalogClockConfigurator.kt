package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.util.Log
import androidx.core.graphics.toColorInt
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.models.WidgetPosition
import com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock.AnalogClockView
import kotlin.math.min
import kotlin.math.sqrt
import androidx.core.view.isGone

class AnalogClockConfigurator {

    companion object {
        private const val TAG = "AnalogClockConfig"
    }

    fun configureAnalogClock(
        clockView: AnalogClockView,
        template: ClockTemplate,
        config: PreferencesManager.ScreensaverConfig
    ) {
        clockView.setTemplate(template)
        try {
            clockView.setHandColor(config.analogHandColor.toColorInt())
        } catch (e: Exception) {
            Log.w(TAG, "Invalid hand color: ${config.analogHandColor}", e)
        }
    }

    fun positionWidgets(
        container: FrameLayout,
        clockView: AnalogClockView,
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
        } catch (e: Exception) {
            Log.w(TAG, "Invalid text color: ${config.textColor}", e)
        }

        // Scale font sizes down on small screens (reference: 480dp)
        val density = container.resources.displayMetrics.density
        val baseDp = min(container.width, container.height).let { if (it > 0) it else container.resources.displayMetrics.widthPixels } / density
        val fontScale = (baseDp / 480f).coerceIn(0.5f, 1.0f)

        dateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.date.fontSizeSp.toFloat() * fontScale)
        alarmText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.alarm.fontSizeSp.toFloat() * fontScale)
        mediaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, template.widgets.media.fontSizeSp.toFloat() * fontScale)

        // Position widgets after layout is measured
        container.post {
            val w = container.width
            val h = container.height
            val base = min(w, h).toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val templateRadius = base * template.face.radiusFraction
            val gap = base * 0.02f
            val interGap = gap * 0.5f
            val isRound = container.resources.configuration.isScreenRound

            // Measure all visible widgets up front
            val widgets = listOf(
                dateText to template.widgets.date,
                alarmText to template.widgets.alarm,
                mediaText to template.widgets.media
            )

            // Cap widget width on round screens to avoid circular bezel clipping
            if (isRound) {
                val maxTextWidth = (base * 0.5f).toInt()
                for ((tv, _) in widgets) {
                    if (tv.isGone) continue
                    tv.maxWidth = maxTextWidth
                    tv.ellipsize = TextUtils.TruncateAt.END
                }
            }

            for ((tv, _) in widgets) {
                if (tv.isGone) continue
                tv.measure(
                    View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.AT_MOST)
                )
            }

            // Group visible non-on_face widgets by position keyword
            val groups = mutableMapOf<String, MutableList<Pair<TextView, WidgetPosition>>>()
            val onFaceWidgets = mutableListOf<Pair<TextView, WidgetPosition>>()
            for ((tv, wp) in widgets) {
                if (tv.isGone) continue
                if (wp.position == "on_face") {
                    onFaceWidgets.add(tv to wp)
                } else {
                    groups.getOrPut(wp.position) { mutableListOf() }.add(tv to wp)
                }
            }

            // Compute max primary-axis extent across all groups (using smaller inter-widget gap)
            var maxGroupExtent = 0f
            var maxCrossAxis = 0f
            for ((pos, group) in groups) {
                val isVertical = pos == "above" || pos == "below"
                val primarySize = if (isVertical) {
                    group.sumOf { (tv, _) -> tv.measuredHeight.toDouble() }.toFloat() +
                        (group.size - 1) * interGap
                } else {
                    group.sumOf { (tv, _) -> tv.measuredWidth.toDouble() }.toFloat() +
                        (group.size - 1) * interGap
                }
                maxGroupExtent = maxOf(maxGroupExtent, primarySize)
                val crossSize = if (isVertical)
                    group.maxOf { (tv, _) -> tv.measuredWidth.toFloat() }
                else
                    group.maxOf { (tv, _) -> tv.measuredHeight.toFloat() }
                maxCrossAxis = maxOf(maxCrossAxis, crossSize)
            }

            // On round screens, compute effective half-distance accounting for circular bezel
            val effectiveHalf = if (isRound && groups.isNotEmpty()) {
                val halfBase = base / 2f
                sqrt(halfBase * halfBase - (maxCrossAxis / 2f) * (maxCrossAxis / 2f))
            } else base / 2f

            // Auto-shrink clock face if widgets can't fit between clock edge and screen edge
            val maxRadius = if (maxGroupExtent > 0f)
                (effectiveHalf - maxGroupExtent - gap).coerceAtLeast(base * 0.2f)
            else Float.MAX_VALUE

            val radius: Float
            if (maxRadius < templateRadius) {
                radius = maxRadius
                clockView.radiusFractionOverride = radius / base
            } else {
                radius = templateRadius
                clockView.radiusFractionOverride = null
            }

            // Position on_face widgets directly
            for ((tv, wp) in onFaceWidgets) {
                positionOnFaceWidget(tv, wp, cx, cy, radius, w, h)
            }

            // Position each group via sequential stacking
            for ((pos, group) in groups) {
                positionWidgetGroup(group, pos, cx, cy, radius, base, gap, interGap, w, h)
            }
        }
    }

    private fun positionWidgetGroup(
        group: List<Pair<TextView, WidgetPosition>>,
        position: String,
        cx: Float, cy: Float, radius: Float, base: Float, gap: Float, interGap: Float,
        containerW: Int, containerH: Int
    ) {
        // Sort by offset fraction to preserve template-intended order
        val sorted = when (position) {
            "below" -> group.sortedBy { (_, wp) -> wp.offsetYFraction }
            "above" -> group.sortedBy { (_, wp) -> -wp.offsetYFraction }
            "right" -> group.sortedBy { (_, wp) -> wp.offsetXFraction }
            "left" -> group.sortedBy { (_, wp) -> -wp.offsetXFraction }
            else -> group
        }

        when (position) {
            "below" -> {
                var cursor = cy + radius + gap
                for ((tv, _) in sorted) {
                    val tvH = tv.measuredHeight
                    val targetY = cursor
                    applyVerticalLayout(tv, targetY, containerW, containerH)
                    cursor += tvH + interGap
                }
            }
            "above" -> {
                var cursor = cy - radius - gap
                for ((tv, _) in sorted) {
                    val tvH = tv.measuredHeight
                    val targetY = cursor - tvH
                    applyVerticalLayout(tv, targetY, containerW, containerH)
                    cursor -= tvH + interGap
                }
            }
            "right" -> {
                var cursor = cx + radius + gap
                for ((tv, wp) in sorted) {
                    val tvW = tv.measuredWidth
                    val tvH = tv.measuredHeight
                    val targetX = cursor + tvW / 2f
                    val targetY = cy + base * wp.offsetYFraction
                    applyLayout(tv, targetX, targetY, tvW, tvH, containerW, containerH)
                    cursor += tvW + interGap
                }
            }
            "left" -> {
                var cursor = cx - radius - gap
                for ((tv, wp) in sorted) {
                    val tvW = tv.measuredWidth
                    val tvH = tv.measuredHeight
                    val targetX = cursor - tvW / 2f
                    val targetY = cy + base * wp.offsetYFraction
                    applyLayout(tv, targetX, targetY, tvW, tvH, containerW, containerH)
                    cursor -= tvW + interGap
                }
            }
        }
    }

    private fun positionOnFaceWidget(
        textView: TextView,
        wp: WidgetPosition,
        cx: Float, cy: Float, radius: Float,
        containerW: Int, containerH: Int
    ) {
        val tvW = textView.measuredWidth
        val tvH = textView.measuredHeight
        val targetX = cx + radius * 2f * wp.offsetXFraction
        val targetY = cy + radius * 2f * wp.offsetYFraction
        applyLayout(textView, targetX, targetY, tvW, tvH, containerW, containerH)
    }

    private fun applyVerticalLayout(
        textView: TextView,
        targetY: Float,
        containerW: Int, containerH: Int
    ) {
        val tvH = textView.measuredHeight
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.topMargin = targetY.toInt().coerceIn(0, containerH - tvH)
        textView.layoutParams = params
    }

    private fun applyLayout(
        textView: TextView,
        targetX: Float, targetY: Float,
        tvW: Int, tvH: Int,
        containerW: Int, containerH: Int
    ) {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.leftMargin = (targetX - tvW / 2f).toInt().coerceIn(0, containerW - tvW)
        params.topMargin = (targetY - tvH / 2f).toInt().coerceIn(0, containerH - tvH)
        textView.layoutParams = params
    }
}
