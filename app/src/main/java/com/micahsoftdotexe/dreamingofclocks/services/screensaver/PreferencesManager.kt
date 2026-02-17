package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context

object PreferencesManager {
    const val PREFS_NAME = "clock_prefs"
    const val KEY_24_HOUR = "pref_24_hour"
    const val KEY_SHOW_SECONDS = "pref_show_seconds"
    const val KEY_SHOW_DATE = "pref_show_date"
    const val KEY_SHOW_ALARM = "pref_show_alarm"
    const val KEY_SHOW_MEDIA = "pref_show_media"
    const val KEY_BG_MODE = "pref_bg_mode"
    const val KEY_BG_COLOR = "pref_bg_color"
    const val KEY_BG_IMAGE_URI = "pref_bg_image_uri"
    const val KEY_TEXT_COLOR = "pref_text_color"

    data class ScreensaverConfig(
        val is24Hour: Boolean,
        val showSeconds: Boolean,
        val showDate: Boolean,
        val showAlarm: Boolean,
        val showMedia: Boolean,
        val bgMode: String,
        val bgColor: String,
        val bgImageUri: String?,
        val textColor: String
    )

    fun loadConfig(context: Context): ScreensaverConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ScreensaverConfig(
            is24Hour = prefs.getBoolean(KEY_24_HOUR, false),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, false),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            showAlarm = prefs.getBoolean(KEY_SHOW_ALARM, true),
            showMedia = prefs.getBoolean(KEY_SHOW_MEDIA, false),
            bgMode = prefs.getString(KEY_BG_MODE, "color") ?: "color",
            bgColor = prefs.getString(KEY_BG_COLOR, "#000000") ?: "#000000",
            bgImageUri = prefs.getString(KEY_BG_IMAGE_URI, null),
            textColor = prefs.getString(KEY_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"
        )
    }
}
