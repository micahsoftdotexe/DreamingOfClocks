package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.dreams.DreamService
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.services.media.MediaDisplayManager
import com.micahsoftdotexe.dreamingofclocks.services.template.TemplateManager
import com.micahsoftdotexe.dreamingofclocks.utils.BackgroundRenderer
import com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock.AnalogClockView
import com.micahsoftdotexe.dreamingofclocks.utils.AlarmHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverService : DreamService() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var mediaText: TextView
    private var textClock: TextClock? = null

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

    @SuppressLint("CutPasteId")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val config = PreferencesManager.loadConfig(this)

        if (config.clockMode == "analog") {
            setContentView(R.layout.screensaver_analog_layout)

            val clockView = findViewById<AnalogClockView>(R.id.analogClockScreensaver)
            val template = TemplateManager.getActiveTemplate(this)
            AnalogClockConfigurator.configureAnalogClock(clockView, template, config)

            dateText = findViewById(R.id.dateTextScreensaver)
            alarmText = findViewById(R.id.alarmTextScreensaver)
            mediaText = findViewById(R.id.mediaTextScreensaver)

            ClockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)

            val container = findViewById<FrameLayout>(R.id.screensaver_root)
            AnalogClockConfigurator.positionWidgets(
                container, dateText, alarmText, mediaText, template, config
            )
        } else {
            setContentView(R.layout.screensaver_layout)

            textClock = findViewById(R.id.textClockScreensaver)
            dateText = findViewById(R.id.dateTextScreensaver)
            alarmText = findViewById(R.id.alarmTextScreensaver)
            mediaText = findViewById(R.id.mediaTextScreensaver)

            ClockConfigurator.applyClockFormat(textClock!!, config)
            ClockConfigurator.applyTextColors(listOf(textClock!!, dateText, alarmText), config)
            ClockConfigurator.applyFonts(this, textClock!!, listOf(dateText, alarmText, mediaText), config)
            ClockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)
        }

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
