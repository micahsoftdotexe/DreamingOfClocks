package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.service.dreams.DreamService
import com.micahsoftdotexe.dreamingofclocks.DreamingOfClocksApp
import com.micahsoftdotexe.dreamingofclocks.di.AppContainer
import com.micahsoftdotexe.dreamingofclocks.services.media.MediaDisplayManager

class ScreensaverService : DreamService() {

    private val container: AppContainer by lazy {
        (application as DreamingOfClocksApp).container
    }

    private lateinit var config: PreferencesManager.ScreensaverConfig
    private lateinit var timeManager: ScreensaverTimeManager
    private val mediaDisplayManager = MediaDisplayManager()
    private var weatherController: WeatherBackgroundController? = null

    override fun onCreate() {
        super.onCreate()
        isInteractive = false
        isFullscreen = true
        isScreenBright = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        config = container.preferencesManager.loadConfig(this)

        // 1. Inflate layout and configure clock
        val views = container.screensaverLayoutManager.setupLayout(this, config)

        // 2. Start date/alarm updates
        timeManager = ScreensaverTimeManager(container.alarmHelper)
        timeManager.start(this, views.dateText, views.alarmText, config)

        // 3. Apply background
        container.backgroundRenderer.applyBackground(views.rootLayout, config, resources, contentResolver)

        // 4. Weather background (if needed)
        if (config.bgMode == "weather") {
            weatherController = WeatherBackgroundController(
                container.weatherCache, container.weatherUpdateScheduler
            ).also { it.setup(this, views.rootLayout, config) }
        }

        // 5. Media display (if needed)
        if (config.showMedia) {
            mediaDisplayManager.setup(this, views.mediaText)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timeManager.stop(this)
        mediaDisplayManager.teardown()
        weatherController?.teardown()
    }
}
