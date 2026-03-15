package com.micahsoftdotexe.dreamingofclocks.weather

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    THUNDERSTORM,
    SNOW;

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> CLEAR
            2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            in 51..57 -> RAIN    // drizzle / freezing drizzle
            in 61..67 -> RAIN    // rain / freezing rain
            in 71..77 -> SNOW    // snow / snow grains
            in 80..82 -> RAIN    // rain showers
            85, 86 -> SNOW       // snow showers
            95 -> THUNDERSTORM
            96, 99 -> THUNDERSTORM // thunderstorm with hail
            else -> CLOUDY
        }
    }
}

data class WeatherData(
    val condition: WeatherCondition,
    val temperature: Double,
    val isDay: Boolean,
    val fetchTimestamp: Long = System.currentTimeMillis()
)

data class GeocodingResult(
    val name: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = listOfNotNull(name, admin1, country).joinToString(", ")
}
