package com.micahsoftdotexe.dreamingofclocks.weather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WeatherApiClient {
    private const val TAG = "WeatherAPI"

    internal fun parseGeocode(body: String): Pair<Double, Double>? {
        val json = JSONObject(body)
        val results = json.optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val first = results.getJSONObject(0)
        return Pair(first.getDouble("latitude"), first.getDouble("longitude"))
    }

    internal fun parseGeocodeSearch(body: String): List<GeocodingResult> {
        val json = JSONObject(body)
        val results = json.optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).map { i ->
            val obj = results.getJSONObject(i)
            GeocodingResult(
                name = obj.getString("name"),
                admin1 = if (obj.has("admin1")) obj.getString("admin1") else null,
                country = if (obj.has("country")) obj.getString("country") else null,
                latitude = obj.getDouble("latitude"),
                longitude = obj.getDouble("longitude")
            )
        }
    }

    internal fun parseWeather(body: String): WeatherData? {
        return try {
            val json = JSONObject(body)
            val current = json.getJSONObject("current_weather")
            val wmoCode = current.getInt("weathercode")
            val temperature = current.getDouble("temperature")
            val isDay = current.getInt("is_day") == 1
            val condition = WeatherCondition.fromWmoCode(wmoCode)
            WeatherData(condition, temperature, isDay)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun geocode(query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            try {
                val body = conn.inputStream.bufferedReader().readText()
                Log.d(TAG, "Geocode response: $body")
                val result = parseGeocode(body)
                if (result != null) Log.d(TAG, "Geocoded '$query' -> (${result.first}, ${result.second})")
                result
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocode failed", e)
            null
        }
    }

    suspend fun geocodeSearch(query: String, count: Int = 5): List<GeocodingResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=$count")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            try {
                val body = conn.inputStream.bufferedReader().readText()
                parseGeocodeSearch(body)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocode search failed", e)
            emptyList()
        }
    }

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            try {
                val body = conn.inputStream.bufferedReader().readText()
                Log.d(TAG, "Weather response: $body")
                val result = parseWeather(body)
                if (result != null) Log.d(TAG, "Weather: ${result.condition}, temp=${result.temperature}, isDay=${result.isDay}")
                result
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch failed", e)
            null
        }
    }
}
