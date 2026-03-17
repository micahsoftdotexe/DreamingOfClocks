package com.micahsoftdotexe.dreamingofclocks.di

import com.micahsoftdotexe.dreamingofclocks.services.screensaver.AnalogClockConfigurator
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.ClockConfigurator
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager
import com.micahsoftdotexe.dreamingofclocks.services.template.TemplateManager
import com.micahsoftdotexe.dreamingofclocks.utils.AlarmHelper
import com.micahsoftdotexe.dreamingofclocks.utils.BackgroundRenderer
import com.micahsoftdotexe.dreamingofclocks.weather.LocationHelper
import com.micahsoftdotexe.dreamingofclocks.weather.LocationProvider
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherApi
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherApiClient
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherCache
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherCacheStore
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherUpdateScheduler

class AppContainer {
    val weatherApi: WeatherApi = WeatherApiClient()
    val weatherCache: WeatherCacheStore = WeatherCache()
    val locationProvider: LocationProvider = LocationHelper()
    val preferencesManager = PreferencesManager()
    val templateManager = TemplateManager(preferencesManager)
    val clockConfigurator = ClockConfigurator()
    val analogClockConfigurator = AnalogClockConfigurator()
    val backgroundRenderer = BackgroundRenderer()
    val alarmHelper = AlarmHelper()
    val weatherUpdateScheduler = WeatherUpdateScheduler(
        weatherApi, weatherCache, locationProvider, preferencesManager
    )
}
