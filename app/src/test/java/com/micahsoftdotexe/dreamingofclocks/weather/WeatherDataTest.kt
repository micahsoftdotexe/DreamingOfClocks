package com.micahsoftdotexe.dreamingofclocks.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherDataTest {

    @Test
    fun `default fetchTimestamp is set to current time`() {
        val before = System.currentTimeMillis()
        val data = WeatherData(WeatherCondition.CLEAR, 20.0, true)
        val after = System.currentTimeMillis()
        assertTrue(data.fetchTimestamp in before..after)
    }

    @Test
    fun `data class equality works`() {
        val ts = 1000L
        val a = WeatherData(WeatherCondition.RAIN, 15.5, false, ts)
        val b = WeatherData(WeatherCondition.RAIN, 15.5, false, ts)
        assertEquals(a, b)
    }

    @Test
    fun `data class inequality on different condition`() {
        val ts = 1000L
        val a = WeatherData(WeatherCondition.RAIN, 15.5, false, ts)
        val b = WeatherData(WeatherCondition.SNOW, 15.5, false, ts)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy preserves other fields`() {
        val original = WeatherData(WeatherCondition.CLEAR, 25.0, true, 5000L)
        val copied = original.copy(condition = WeatherCondition.FOG)
        assertEquals(WeatherCondition.FOG, copied.condition)
        assertEquals(25.0, copied.temperature, 0.001)
        assertEquals(true, copied.isDay)
        assertEquals(5000L, copied.fetchTimestamp)
    }

    @Test
    fun `various condition and temperature combinations`() {
        val nightSnow = WeatherData(WeatherCondition.SNOW, -5.0, false, 0L)
        assertEquals(WeatherCondition.SNOW, nightSnow.condition)
        assertEquals(-5.0, nightSnow.temperature, 0.001)
        assertEquals(false, nightSnow.isDay)

        val dayThunder = WeatherData(WeatherCondition.THUNDERSTORM, 30.0, true, 0L)
        assertEquals(WeatherCondition.THUNDERSTORM, dayThunder.condition)
        assertEquals(30.0, dayThunder.temperature, 0.001)
        assertEquals(true, dayThunder.isDay)
    }
}
