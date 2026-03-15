package com.micahsoftdotexe.dreamingofclocks.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `code 0 returns CLEAR`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(0))
    }

    @Test
    fun `code 1 returns CLEAR`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(1))
    }

    @Test
    fun `code 2 returns PARTLY_CLOUDY`() {
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(2))
    }

    @Test
    fun `code 3 returns CLOUDY`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(3))
    }

    @Test
    fun `code 45 returns FOG`() {
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
    }

    @Test
    fun `code 48 returns FOG`() {
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(48))
    }

    @Test
    fun `drizzle range 51-57 returns RAIN`() {
        for (code in 51..57) {
            assertEquals("WMO code $code", WeatherCondition.RAIN, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `rain range 61-67 returns RAIN`() {
        for (code in 61..67) {
            assertEquals("WMO code $code", WeatherCondition.RAIN, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `snow range 71-77 returns SNOW`() {
        for (code in 71..77) {
            assertEquals("WMO code $code", WeatherCondition.SNOW, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `rain showers 80-82 returns RAIN`() {
        for (code in 80..82) {
            assertEquals("WMO code $code", WeatherCondition.RAIN, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `snow showers 85 and 86 return SNOW`() {
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(85))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(86))
    }

    @Test
    fun `code 95 returns THUNDERSTORM`() {
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
    }

    @Test
    fun `codes 96 and 99 return THUNDERSTORM`() {
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(96))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(99))
    }

    @Test
    fun `unknown codes fall back to CLOUDY`() {
        for (code in listOf(-1, 4, 10, 30, 50, 58, 60, 68, 70, 78, 83, 84, 87, 90, 97, 100, 999)) {
            assertEquals("WMO code $code", WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `negative WMO codes fall back to CLOUDY`() {
        for (code in listOf(-100, -50, -1, Int.MIN_VALUE)) {
            assertEquals("WMO code $code", WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `very large WMO codes fall back to CLOUDY`() {
        for (code in listOf(200, 500, 1000, Int.MAX_VALUE)) {
            assertEquals("WMO code $code", WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `boundary codes at range edges`() {
        // drizzle range boundaries
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(51))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(57))
        // rain range boundaries
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(61))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(67))
        // snow range boundaries
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(71))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(77))
        // rain showers boundaries
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(80))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(82))
    }

    @Test
    fun `codes just outside ranges fall back to CLOUDY`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(50))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(58))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(60))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(68))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(70))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(78))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(83))
    }
}
