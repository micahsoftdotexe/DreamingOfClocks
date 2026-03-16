package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.micahsoftdotexe.dreamingofclocks.utils.resolveTypeface

class ClockConfigurator {
    companion object {
        private const val TAG = "ClockConfigurator"
    }

    internal fun buildClockFormat(is24Hour: Boolean, showSeconds: Boolean): String {
        val hourString = if (is24Hour) "HH" else "hh"
        val secondString = if (showSeconds) ":ss" else ""
        val ampmString = if (is24Hour) "" else " a"
        return "$hourString:mm$secondString$ampmString"
    }

    fun applyClockFormat(textClock: TextClock, config: PreferencesManager.ScreensaverConfig) {
        val format = buildClockFormat(config.is24Hour, config.showSeconds)
        textClock.format24Hour = format
        textClock.format12Hour = format
    }

    fun applyTextColors(views: List<TextView>, config: PreferencesManager.ScreensaverConfig) {
        try {
            val tc = config.textColor.toColorInt()
            views.forEach { it.setTextColor(tc) }
        } catch (e: Exception) {
            Log.w(TAG, "Invalid text color, ignoring", e)
        }
    }

    fun applyFonts(
        context: Context,
        clock: TextClock,
        featureViews: List<TextView>,
        config: PreferencesManager.ScreensaverConfig
    ) {
        clock.typeface = resolveTypeface(context, config.clockFont)
        val featureTypeface = resolveTypeface(context, config.featureFont)
        featureViews.forEach { it.typeface = featureTypeface }
    }

    fun applyVisibility(
        dateText: TextView,
        alarmText: TextView,
        mediaText: TextView,
        config: PreferencesManager.ScreensaverConfig
    ) {
        dateText.visibility = if (config.showDate) View.VISIBLE else View.GONE
        if (!config.showAlarm) alarmText.visibility = View.GONE
        if (!config.showMedia) mediaText.visibility = View.GONE
    }
}
