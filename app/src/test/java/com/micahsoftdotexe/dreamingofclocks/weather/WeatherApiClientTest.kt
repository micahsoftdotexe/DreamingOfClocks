package com.micahsoftdotexe.dreamingofclocks.weather

import org.junit.Assert.*
import org.junit.Test

class WeatherApiClientTest {

    private val client = WeatherApiClient()

    // --- parseGeocode ---

    @Test
    fun `parseGeocode valid response`() {
        val body = """{"results":[{"latitude":52.52,"longitude":13.405}]}"""
        val result = client.parseGeocode(body)
        assertNotNull(result)
        assertEquals(52.52, result!!.first, 0.001)
        assertEquals(13.405, result.second, 0.001)
    }

    @Test
    fun `parseGeocode missing results array`() {
        assertNull(client.parseGeocode("""{"generationtime_ms":0.5}"""))
    }

    @Test
    fun `parseGeocode empty results array`() {
        assertNull(client.parseGeocode("""{"results":[]}"""))
    }

    // --- parseGeocodeSearch ---

    @Test
    fun `parseGeocodeSearch valid response`() {
        val body = """{"results":[
            {"name":"Berlin","admin1":"Berlin","country":"Germany","latitude":52.52,"longitude":13.405},
            {"name":"Bern","latitude":46.95,"longitude":7.45}
        ]}"""
        val results = client.parseGeocodeSearch(body)
        assertEquals(2, results.size)
        assertEquals("Berlin", results[0].name)
        assertEquals("Berlin", results[0].admin1)
        assertEquals("Germany", results[0].country)
        assertEquals("Bern", results[1].name)
        assertNull(results[1].admin1)
        assertNull(results[1].country)
    }

    @Test
    fun `parseGeocodeSearch missing results`() {
        assertEquals(emptyList<GeocodingResult>(), client.parseGeocodeSearch("""{}"""))
    }

    @Test
    fun `parseGeocodeSearch empty results`() {
        assertEquals(emptyList<GeocodingResult>(), client.parseGeocodeSearch("""{"results":[]}"""))
    }

    // --- parseWeather ---

    @Test
    fun `parseWeather valid response daytime`() {
        val body = """{"current_weather":{"weathercode":0,"temperature":22.5,"is_day":1}}"""
        val result = client.parseWeather(body)
        assertNotNull(result)
        assertEquals(WeatherCondition.CLEAR, result!!.condition)
        assertEquals(22.5, result.temperature, 0.001)
        assertTrue(result.isDay)
    }

    @Test
    fun `parseWeather valid response nighttime`() {
        val body = """{"current_weather":{"weathercode":3,"temperature":15.0,"is_day":0}}"""
        val result = client.parseWeather(body)
        assertNotNull(result)
        assertEquals(WeatherCondition.CLOUDY, result!!.condition)
        assertFalse(result.isDay)
    }

    @Test
    fun `parseWeather WMO code mapping`() {
        val body = """{"current_weather":{"weathercode":95,"temperature":18.0,"is_day":1}}"""
        val result = client.parseWeather(body)
        assertEquals(WeatherCondition.THUNDERSTORM, result!!.condition)
    }

    @Test
    fun `parseWeather snow code`() {
        val body = """{"current_weather":{"weathercode":71,"temperature":-2.0,"is_day":0}}"""
        val result = client.parseWeather(body)
        assertEquals(WeatherCondition.SNOW, result!!.condition)
        assertEquals(-2.0, result.temperature, 0.001)
    }

    @Test
    fun `parseWeather missing current_weather returns null`() {
        assertNull(client.parseWeather("""{"hourly":{}}"""))
    }

    @Test
    fun `parseWeather missing fields returns null`() {
        assertNull(client.parseWeather("""{"current_weather":{"temperature":10}}"""))
    }

    @Test
    fun `parseWeather invalid JSON returns null`() {
        assertNull(client.parseWeather("not json"))
    }
}
