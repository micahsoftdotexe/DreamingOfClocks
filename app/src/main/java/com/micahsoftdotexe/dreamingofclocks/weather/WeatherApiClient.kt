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
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return@withContext null
                if (results.length() == 0) return@withContext null
                val first = results.getJSONObject(0)
                val lat = first.getDouble("latitude")
                val lon = first.getDouble("longitude")
                Log.d(TAG, "Geocoded '$query' -> ($lat, $lon)")
                Pair(lat, lon)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocode failed", e)
            null
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
                val json = JSONObject(body)
                val current = json.getJSONObject("current_weather")
                val wmoCode = current.getInt("weathercode")
                val temperature = current.getDouble("temperature")
                val isDay = current.getInt("is_day") == 1
                val condition = WeatherCondition.fromWmoCode(wmoCode)
                Log.d(TAG, "Weather: code=$wmoCode -> $condition, temp=$temperature, isDay=$isDay")
                WeatherData(condition, temperature, isDay)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch failed", e)
            null
        }
    }
}
