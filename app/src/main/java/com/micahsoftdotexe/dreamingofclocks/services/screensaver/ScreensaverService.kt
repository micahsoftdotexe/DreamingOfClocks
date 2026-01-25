package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.service.dreams.DreamService
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.utils.AlarmHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.graphics.drawable.toDrawable

class ScreensaverService: DreamService() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var textClock: TextClock

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the date whenever time ticks or date/time zone changes
            updateDate()
        }
    }

    private val alarmUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateAlarm()
        }
    }


    override fun onCreate() {
        super.onCreate()
        isInteractive = false
        isFullscreen = true
        isScreenBright = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setContentView(R.layout.screensaver_layout)
        textClock = findViewById(R.id.textClockScreensaver)
        dateText = findViewById(R.id.dateTextScreensaver)
        alarmText = findViewById(R.id.alarmTextScreensaver)

        // Ensure formats include seconds (may be overridden by preferences)
        textClock.format24Hour = "HH:mm:ss"
        textClock.format12Hour = "hh:mm:ss a"
        updateDate()
        updateAlarm()

        // Apply saved settings
        applyPreferences()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        val alarmUpdateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        }
        registerReceiver(alarmUpdateReceiver, alarmUpdateFilter)
        registerReceiver(timeReceiver, filter)
    }

    private fun applyPreferences() {
        val prefs = getSharedPreferences("clock_prefs", MODE_PRIVATE)
        val is24Hour = prefs.getBoolean("pref_24_hour", false)
        val showSeconds = prefs.getBoolean("pref_show_seconds", false)
        val showDate = prefs.getBoolean("pref_show_date", true)
        val showAlarm = prefs.getBoolean("pref_show_alarm", true)
        val bgMode = prefs.getString("pref_bg_mode", "color") ?: "color"
        val bgColor = prefs.getString("pref_bg_color", "#000000") ?: "#000000"
        val bgImageUri = prefs.getString("pref_bg_image_uri", null)
        val textColor = prefs.getString("pref_text_color", "#FFFFFF") ?: "#FFFFFF"

        // Get the root layout
        val rootLayout = findViewById<View>(R.id.screensaver_root)

        // Clock format
        val secondString = if (showSeconds) ":ss" else ""
        val hourString = if (is24Hour) "HH" else "hh"
        val ampmString = if (is24Hour) "" else " a"
        textClock.format24Hour = "$hourString:mm$secondString$ampmString"
        textClock.format12Hour = "$hourString:mm$secondString$ampmString"

        // Date visibility
        dateText.visibility = if (showDate) View.VISIBLE else View.GONE

        // Alarm visibility and update
        if (showAlarm) {
            updateAlarm()
        } else {
            alarmText.visibility = View.GONE
        }

        // Text color
        try {
            val tc = textColor.toColorInt()
            textClock.setTextColor(tc)
            dateText.setTextColor(tc)
            alarmText.setTextColor(tc)
        } catch (_: Exception) {
            // ignore parse errors
        }

        // Background: image preferred when mode=image and uri present
        if (bgMode == "image" && !bgImageUri.isNullOrEmpty()) {
            try {
                val uri = bgImageUri.toUri()
                val input = contentResolver.openInputStream(uri)
                input?.use {
                    val bmp = BitmapFactory.decodeStream(it)
                    rootLayout?.background = bmp.toDrawable(resources)
                }
            } catch (_: Exception) {
                // fallback to color if image fails
                try {
                    rootLayout?.setBackgroundColor(bgColor.toColorInt())
                } catch (_: Exception) {
                    rootLayout?.setBackgroundColor(Color.BLACK)
                }
            }
        } else {
            try {
                rootLayout?.setBackgroundColor(bgColor.toColorInt())
            } catch (_: Exception) {
                rootLayout?.setBackgroundColor(Color.BLACK)
            }
        }
    }

    private fun updateDate() {
        dateText.text = dateFormatter.format(Date())
    }

    private fun updateAlarm() {
        val prefs = getSharedPreferences("clock_prefs", MODE_PRIVATE)
        val showAlarm = prefs.getBoolean("pref_show_alarm", true)

        if (!showAlarm) {
            alarmText.visibility = View.GONE
            return
        }

        val alarmInfo = AlarmHelper.formatNextAlarmCountdown(this)
        if (alarmInfo != null) {
            alarmText.text = alarmInfo
            alarmText.visibility = View.VISIBLE
        } else {
            alarmText.visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterReceiver(timeReceiver)
        // no handler callbacks to remove anymore
    }
}