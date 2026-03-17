package com.micahsoftdotexe.dreamingofclocks.weather

import android.content.Context
import android.util.Log
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

sealed interface FetchResult {
    data class Success(val data: WeatherData) : FetchResult
    data object NoLocation : FetchResult
    data class LocationNotFound(val query: String) : FetchResult
    data object FetchFailed : FetchResult
}

class WeatherUpdateScheduler(
    private val weatherApi: WeatherApi,
    private val weatherCache: WeatherCacheStore,
    private val locationProvider: LocationProvider,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "WeatherAPI"
    }

    fun startPeriodicUpdates(
        context: Context,
        scope: CoroutineScope,
        onUpdate: (WeatherData) -> Unit
    ) {
        scope.launch {
            while (true) {
                val config = preferencesManager.loadConfig(context)
                val updateFreqMs = config.weatherUpdateFreq

                if (weatherCache.isStale(context, updateFreqMs)) {
                    val result = performFetch(context, config)
                    if (result is FetchResult.Success) onUpdate(result.data)
                } else {
                    val cached = weatherCache.load(context)
                    if (cached != null) onUpdate(cached)
                }

                delay(updateFreqMs)
            }
        }
    }

    fun fetchNow(
        context: Context,
        scope: CoroutineScope,
        onResult: (FetchResult) -> Unit
    ) {
        scope.launch {
            val config = preferencesManager.loadConfig(context)
            val result = performFetch(context, config)
            onResult(result)
        }
    }

    private suspend fun performFetch(
        context: Context,
        config: PreferencesManager.ScreensaverConfig,
    ): FetchResult {
        val locationResult = resolveLocation(context, config)
        if (locationResult == null) {
            // Determine if it's no-location or location-not-found
            val query = config.weatherLocation
            if (!config.weatherUseGps && query.isBlank()) {
                Log.e(TAG, "No location configured")
                return FetchResult.NoLocation
            }
            if (!config.weatherUseGps && query.isNotBlank()) {
                Log.e(TAG, "Location not found: $query")
                return FetchResult.LocationNotFound(query)
            }
            Log.e(TAG, "Could not resolve location")
            return FetchResult.FetchFailed
        }
        val (lat, lon) = locationResult
        val data = weatherApi.fetchWeather(lat, lon)
        if (data != null) {
            weatherCache.save(context, data)
            Log.d(TAG, "Weather updated: ${data.condition}, ${data.temperature}°")
            return FetchResult.Success(data)
        }
        return FetchResult.FetchFailed
    }

    private suspend fun resolveLocation(
        context: Context,
        config: PreferencesManager.ScreensaverConfig
    ): Pair<Double, Double>? {
        if (config.weatherUseGps) {
            val gps = locationProvider.getLastKnownLocation(context)
            if (gps != null) return gps
            Log.d(TAG, "No cached GPS location, requesting active location fix")
            val active = suspendCancellableCoroutine { cont ->
                locationProvider.requestLocationUpdate(context) { result ->
                    cont.resume(result)
                }
            }
            if (active != null) return active
            Log.d(TAG, "GPS location unavailable, falling back to text location")
        }

        // Use coordinates from search selection if available
        if (!config.weatherLat.isNaN() && !config.weatherLon.isNaN()) {
            return Pair(config.weatherLat.toDouble(), config.weatherLon.toDouble())
        }

        val locationQuery = config.weatherLocation
        if (locationQuery.isBlank()) {
            Log.d(TAG, "No location configured")
            return null
        }

        // Check if we have cached coords for this query
        val cached = weatherCache.getCachedLocation(context)
        if (cached != null && cached.first == locationQuery) {
            return Pair(cached.second, cached.third)
        }

        // Geocode the query
        val coords = weatherApi.geocode(locationQuery)
        if (coords != null) {
            weatherCache.saveLocation(context, locationQuery, coords.first, coords.second)
        }
        return coords
    }
}
