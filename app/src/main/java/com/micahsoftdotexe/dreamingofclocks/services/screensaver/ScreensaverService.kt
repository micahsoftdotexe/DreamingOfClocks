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
import com.micahsoftdotexe.dreamingofclocks.utils.MediaMetadataHelper
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.graphics.drawable.toDrawable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverService: DreamService() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var mediaText: TextView
    private lateinit var textClock: TextClock

    private var mediaHelper: MediaMetadataHelper? = null
    private val handler = Handler(Looper.getMainLooper())
    private var playPauseAnimationIndex = 0
    private val playIcon = "▶"
    private val pauseIcon = "⏸"

    private val playPauseAnimationRunnable = object : Runnable {
        override fun run() {
            if (mediaText.visibility == View.VISIBLE) {
                updateMediaDisplay()
                handler.postDelayed(this, 500) // Update animation every 500ms
            }
        }
    }

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
        mediaText = findViewById(R.id.mediaTextScreensaver)

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

        // Initialize media helper
        val prefs = getSharedPreferences("clock_prefs", MODE_PRIVATE)
        val showMedia = prefs.getBoolean("pref_show_media", false)
        if (showMedia) {
            setupMediaListener()
        }
    }

    private fun applyPreferences() {
        val prefs = getSharedPreferences("clock_prefs", MODE_PRIVATE)
        val is24Hour = prefs.getBoolean("pref_24_hour", false)
        val showSeconds = prefs.getBoolean("pref_show_seconds", false)
        val showDate = prefs.getBoolean("pref_show_date", true)
        val showAlarm = prefs.getBoolean("pref_show_alarm", true)
        val showMedia = prefs.getBoolean("pref_show_media", false)
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

        // Media visibility and update
        if (showMedia) {
            setupMediaListener()
        } else {
            mediaText.visibility = View.GONE
            mediaHelper?.stopListening()
            handler.removeCallbacks(playPauseAnimationRunnable)
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

    private fun setupMediaListener() {
        mediaHelper = MediaMetadataHelper(this).apply {
            startListening(object : MediaMetadataHelper.MediaCallback {
                override fun onMediaInfoChanged(info: MediaMetadataHelper.MediaInfo?) {
                    handler.post {
                        updateMediaDisplay(info)
                    }
                }
            })
        }
        handler.post(playPauseAnimationRunnable)
    }

    private fun updateMediaDisplay(info: MediaMetadataHelper.MediaInfo? = null) {
        val mediaInfo = info ?: mediaHelper?.getCurrentMediaInfo()

        if (mediaInfo == null) {
            mediaText.visibility = View.GONE
            return
        }

        val icon = if (mediaInfo.isPlaying) {
            playIcon
        } else {
            pauseIcon
        }

        mediaText.text = "$icon ${mediaInfo.title} - ${mediaInfo.artist}"
        mediaText.visibility = View.VISIBLE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterReceiver(timeReceiver)
        unregisterReceiver(alarmUpdateReceiver)
        mediaHelper?.stopListening()
        handler.removeCallbacks(playPauseAnimationRunnable)
    }
}