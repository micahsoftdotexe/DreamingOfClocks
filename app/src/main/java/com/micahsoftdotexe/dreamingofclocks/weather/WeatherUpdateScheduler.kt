package com.micahsoftdotexe.dreamingofclocks.weather

import android.content.Context
import android.util.Log
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WeatherUpdateScheduler {
    private const val TAG = "WeatherAPI"

    fun startPeriodicUpdates(
        context: Context,
        scope: CoroutineScope,
        onUpdate: (WeatherData) -> Unit
    ) {
        scope.launch {
            while (true) {
                val config = PreferencesManager.loadConfig(context)
                val updateFreqMs = config.weatherUpdateFreq

                if (WeatherCache.isStale(context, updateFreqMs)) {
                    performFetch(context, config, onUpdate)
                } else {
                    val cached = WeatherCache.load(context)
                    if (cached != null) onUpdate(cached)
                }

                delay(updateFreqMs)
            }
        }
    }

    fun fetchNow(
        context: Context,
        scope: CoroutineScope,
        onUpdate: (WeatherData?) -> Unit
    ) {
        scope.launch {
            val config = PreferencesManager.loadConfig(context)
            val data = performFetch(context, config, null)
            onUpdate(data)
        }
    }

    private suspend fun performFetch(
        context: Context,
        config: PreferencesManager.ScreensaverConfig,
        onUpdate: ((WeatherData) -> Unit)?
    ): WeatherData? {
        val coords = resolveLocation(context, config)
        if (coords == null) {
            Log.e(TAG, "Could not resolve location")
            return null
        }
        val (lat, lon) = coords
        val data = WeatherApiClient.fetchWeather(lat, lon)
        if (data != null) {
            WeatherCache.save(context, data)
            onUpdate?.invoke(data)
            Log.d(TAG, "Weather updated: ${data.condition}, ${data.temperature}°")
        }
        return data
    }

    private suspend fun resolveLocation(
        context: Context,
        config: PreferencesManager.ScreensaverConfig
    ): Pair<Double, Double>? {
        if (config.weatherUseGps) {
            val gps = LocationHelper.getLastKnownLocation(context)
            if (gps != null) return gps
            Log.d(TAG, "GPS location unavailable, falling back to text location")
        }

        val locationQuery = config.weatherLocation
        if (locationQuery.isBlank()) {
            Log.d(TAG, "No location configured")
            return null
        }

        // Check if we have cached coords for this query
        val cached = WeatherCache.getCachedLocation(context)
        if (cached != null && cached.first == locationQuery) {
            return Pair(cached.second, cached.third)
        }

        // Geocode the query
        val coords = WeatherApiClient.geocode(locationQuery)
        if (coords != null) {
            WeatherCache.saveLocation(context, locationQuery, coords.first, coords.second)
        }
        return coords
    }
}
