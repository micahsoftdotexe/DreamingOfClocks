package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import androidx.core.graphics.toColorInt

object ClockConfigurator {
    fun applyClockFormat(textClock: TextClock, config: PreferencesManager.ScreensaverConfig) {
        val secondString = if (config.showSeconds) ":ss" else ""
        val hourString = if (config.is24Hour) "HH" else "hh"
        val ampmString = if (config.is24Hour) "" else " a"
        val format = "$hourString:mm$secondString$ampmString"
        textClock.format24Hour = format
        textClock.format12Hour = format
    }

    fun applyTextColors(views: List<TextView>, config: PreferencesManager.ScreensaverConfig) {
        try {
            val tc = config.textColor.toColorInt()
            views.forEach { it.setTextColor(tc) }
        } catch (_: Exception) {
            // ignore parse errors
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

    private fun resolveTypeface(context: Context, fontFamily: String): Typeface =
        if (fontFamily == "dseg7") {
            Typeface.createFromAsset(context.assets, "fonts/DSEG7Classic-Regular.ttf")
        } else {
            Typeface.create(fontFamily, Typeface.NORMAL)
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
