package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.dreams.DreamService
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.DreamingOfClocksApp
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.di.AppContainer
import com.micahsoftdotexe.dreamingofclocks.services.media.MediaDisplayManager
import com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock.AnalogClockView
import com.micahsoftdotexe.dreamingofclocks.uicomponents.weatherbackground.WeatherBackgroundView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverService : DreamService() {

    private val container: AppContainer by lazy {
        (application as DreamingOfClocksApp).container
    }

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var mediaText: TextView
    private var textClock: TextClock? = null

    private lateinit var config: PreferencesManager.ScreensaverConfig
    private val mediaDisplayManager = MediaDisplayManager()
    private var weatherScope: CoroutineScope? = null
    private var weatherView: WeatherBackgroundView? = null

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

//    @SuppressLint("CutPasteId")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        config = container.preferencesManager.loadConfig(this)

        if (config.clockMode == "analog") {
            setContentView(R.layout.screensaver_analog_layout)

            val clockView = findViewById<AnalogClockView>(R.id.analogClockScreensaver)
            val template = container.templateManager.getActiveTemplate(this)
            container.analogClockConfigurator.configureAnalogClock(clockView, template, config)

            dateText = findViewById(R.id.dateTextScreensaver)
            alarmText = findViewById(R.id.alarmTextScreensaver)
            mediaText = findViewById(R.id.mediaTextScreensaver)

            container.clockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)
        } else {
            setContentView(R.layout.screensaver_layout)

            textClock = findViewById(R.id.textClockScreensaver)
            dateText = findViewById(R.id.dateTextScreensaver)
            alarmText = findViewById(R.id.alarmTextScreensaver)
            mediaText = findViewById(R.id.mediaTextScreensaver)

            container.clockConfigurator.applyClockFormat(textClock!!, config)
            container.clockConfigurator.applyTextColors(listOf(textClock!!, dateText, alarmText), config)
            container.clockConfigurator.applyFonts(this, textClock!!, listOf(dateText, alarmText, mediaText), config)
            container.clockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)
        }

        val rootLayout = findViewById<View>(R.id.screensaver_root)

        if (config.clockMode == "analog") {
            val template = container.templateManager.getActiveTemplate(this)
            container.analogClockConfigurator.positionWidgets(
                rootLayout as FrameLayout, dateText, alarmText, mediaText, template, config
            )
        }

        updateDate()
        if (config.showAlarm) updateAlarm()
        container.backgroundRenderer.applyBackground(rootLayout, config, resources, contentResolver)

        if (config.bgMode == "weather") {
            setupWeatherBackground(rootLayout, config)
        }

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
        if (!config.showAlarm) {
            alarmText.visibility = View.GONE
            return
        }

        val alarmInfo = container.alarmHelper.formatNextAlarmCountdown(this)
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
        weatherScope?.cancel()
        weatherScope = null
        weatherView = null
    }

    private fun setupWeatherBackground(rootView: View, config: PreferencesManager.ScreensaverConfig) {
        val bgView = WeatherBackgroundView(this)
        weatherView = bgView

        // Show cached weather immediately
        val cached = container.weatherCache.load(this)
        if (cached != null) {
            bgView.setWeather(cached.condition, cached.isDay)
        }

        // Insert the weather view behind all other content
        when (rootView) {
            is FrameLayout -> {
                rootView.addView(bgView, 0, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
            is LinearLayout -> {
                // Wrap the LinearLayout content in a FrameLayout overlay
                val parent = rootView.parent as? ViewGroup
                if (parent != null) {
                    val index = parent.indexOfChild(rootView)
                    parent.removeView(rootView)
                    val wrapper = FrameLayout(this).apply {
                        id = R.id.screensaver_root
                    }
                    wrapper.addView(bgView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                    wrapper.addView(rootView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                    parent.addView(wrapper, index)
                }
            }
        }

        // Start periodic weather updates
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        weatherScope = scope
        container.weatherUpdateScheduler.startPeriodicUpdates(this, scope) { data ->
            bgView.setWeather(data.condition, data.isDay)
        }
    }
}
