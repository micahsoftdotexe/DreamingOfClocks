package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.dreams.DreamService
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.utils.AlarmHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverService : DreamService() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var mediaText: TextView
    private lateinit var textClock: TextClock

    private val mediaDisplayManager = MediaDisplayManager()

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
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

        val config = PreferencesManager.loadConfig(this)

        ClockConfigurator.applyClockFormat(textClock, config)
        ClockConfigurator.applyTextColors(listOf(textClock, dateText, alarmText), config)
        ClockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)

        updateDate()
        if (config.showAlarm) updateAlarm()

        val rootLayout = findViewById<View>(R.id.screensaver_root)
        BackgroundRenderer.applyBackground(rootLayout, config, resources, contentResolver)

        if (config.showMedia) {
            mediaDisplayManager.setup(this, mediaText)
        }

        registerReceiver(timeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        registerReceiver(alarmUpdateReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        })
    }

    private fun updateDate() {
        dateText.text = dateFormatter.format(Date())
    }

    private fun updateAlarm() {
        val config = PreferencesManager.loadConfig(this)
        if (!config.showAlarm) {
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
        unregisterReceiver(alarmUpdateReceiver)
        mediaDisplayManager.teardown()
    }
}
