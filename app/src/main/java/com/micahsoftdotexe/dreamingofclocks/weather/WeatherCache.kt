package com.micahsoftdotexe.dreamingofclocks.weather

import android.content.Context

interface WeatherCacheStore {
    fun save(context: Context, data: WeatherData)
    fun load(context: Context): WeatherData?
    fun saveLocation(context: Context, query: String, lat: Double, lon: Double)
    fun getCachedLocation(context: Context): Triple<String, Double, Double>?
    fun isStale(context: Context, maxAgeMs: Long): Boolean
}

class WeatherCache : WeatherCacheStore {
    companion object {
        private const val PREFS_NAME = "weather_cache"
        private const val KEY_CONDITION = "condition"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_IS_DAY = "is_day"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_LAT = "cached_lat"
        private const val KEY_LON = "cached_lon"
        private const val KEY_LOCATION_QUERY = "cached_location_query"
    }

    override fun save(context: Context, data: WeatherData) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_CONDITION, data.condition.name)
            putFloat(KEY_TEMPERATURE, data.temperature.toFloat())
            putBoolean(KEY_IS_DAY, data.isDay)
            putLong(KEY_TIMESTAMP, data.fetchTimestamp)
            apply()
        }
    }

    override fun load(context: Context): WeatherData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val conditionName = prefs.getString(KEY_CONDITION, null) ?: return null
        return try {
            WeatherData(
                condition = WeatherCondition.valueOf(conditionName),
                temperature = prefs.getFloat(KEY_TEMPERATURE, 0f).toDouble(),
                isDay = prefs.getBoolean(KEY_IS_DAY, true),
                fetchTimestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun saveLocation(context: Context, query: String, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_LOCATION_QUERY, query)
            putFloat(KEY_LAT, lat.toFloat())
            putFloat(KEY_LON, lon.toFloat())
            apply()
        }
    }

    override fun getCachedLocation(context: Context): Triple<String, Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val query = prefs.getString(KEY_LOCATION_QUERY, null) ?: return null
        val lat = prefs.getFloat(KEY_LAT, Float.NaN)
        val lon = prefs.getFloat(KEY_LON, Float.NaN)
        if (lat.isNaN() || lon.isNaN()) return null
        return Triple(query, lat.toDouble(), lon.toDouble())
    }

    override fun isStale(context: Context, maxAgeMs: Long): Boolean {
        val data = load(context) ?: return true
        return System.currentTimeMillis() - data.fetchTimestamp > maxAgeMs
    }
}
