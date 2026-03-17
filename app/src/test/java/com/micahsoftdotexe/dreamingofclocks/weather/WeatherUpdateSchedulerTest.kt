package com.micahsoftdotexe.dreamingofclocks.weather

import android.content.Context
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherUpdateSchedulerTest {

    private val context: Context = Mockito.mock(Context::class.java)

    private fun makeConfig(
        weatherUseGps: Boolean = false,
        weatherLocation: String = "",
        weatherLat: Float = Float.NaN,
        weatherLon: Float = Float.NaN,
    ) = PreferencesManager.ScreensaverConfig(
        is24Hour = false,
        showSeconds = false,
        showDate = true,
        showAlarm = true,
        showMedia = false,
        bgMode = "color",
        bgColor = "#000000",
        bgImageUri = null,
        textColor = "#FFFFFF",
        clockMode = "digital",
        analogTemplate = "Classic",
        analogHandColor = "#FFFFFF",
        clockFont = "sans-serif",
        featureFont = "sans-serif",
        weatherLocation = weatherLocation,
        weatherUpdateFreq = 1_800_000L,
        weatherLat = weatherLat,
        weatherLon = weatherLon,
        weatherUseGps = weatherUseGps
    )

    // --- Fake implementations ---

    private class FakeWeatherApi(
        private val weatherResult: WeatherData? = null,
        private val geocodeResult: Pair<Double, Double>? = null,
    ) : WeatherApi {
        val fetchWeatherCalls = mutableListOf<Pair<Double, Double>>()
        val geocodeCalls = mutableListOf<String>()

        override suspend fun geocode(query: String): Pair<Double, Double>? {
            geocodeCalls.add(query)
            return geocodeResult
        }

        override suspend fun geocodeSearch(query: String, count: Int): List<GeocodingResult> {
            return emptyList()
        }

        override suspend fun fetchWeather(lat: Double, lon: Double): WeatherData? {
            fetchWeatherCalls.add(Pair(lat, lon))
            return weatherResult
        }
    }

    private class FakeWeatherCache : WeatherCacheStore {
        var savedData: WeatherData? = null
        var savedLocation: Triple<String, Double, Double>? = null
        var cachedLocation: Triple<String, Double, Double>? = null

        override fun save(context: Context, data: WeatherData) { savedData = data }
        override fun load(context: Context): WeatherData? = savedData
        override fun saveLocation(context: Context, query: String, lat: Double, lon: Double) {
            savedLocation = Triple(query, lat, lon)
            cachedLocation = Triple(query, lat, lon)
        }
        override fun getCachedLocation(context: Context): Triple<String, Double, Double>? = cachedLocation
        override fun isStale(context: Context, maxAgeMs: Long): Boolean = true
    }

    private class FakeLocationProvider(
        private val lastKnown: Pair<Double, Double>? = null,
        private val activeLocation: Pair<Double, Double>? = null
    ) : LocationProvider {
        override fun getLastKnownLocation(context: Context): Pair<Double, Double>? = lastKnown
        override fun requestLocationUpdate(context: Context, callback: (Pair<Double, Double>?) -> Unit) {
            callback(activeLocation)
        }
    }

    private class FakePreferencesManager(
        private val config: ScreensaverConfig
    ) : PreferencesManager() {
        override fun loadConfig(context: Context): ScreensaverConfig = config
    }

    // --- Tests ---

    @Test
    fun `fetchNow returns NoLocation when no location configured`() = runTest {
        val scheduler = WeatherUpdateScheduler(
            FakeWeatherApi(), FakeWeatherCache(), FakeLocationProvider(),
            FakePreferencesManager(makeConfig())
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.NoLocation)
    }

    @Test
    fun `fetchNow returns LocationNotFound when geocode fails`() = runTest {
        val scheduler = WeatherUpdateScheduler(
            FakeWeatherApi(geocodeResult = null), FakeWeatherCache(), FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLocation = "Nonexistent City"))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.LocationNotFound)
        assertEquals("Nonexistent City", (result as FetchResult.LocationNotFound).query)
    }

    @Test
    fun `fetchNow returns Success with valid weather from coordinates`() = runTest {
        val weatherData = WeatherData(WeatherCondition.CLEAR, 20.0, true)
        val scheduler = WeatherUpdateScheduler(
            FakeWeatherApi(weatherResult = weatherData), FakeWeatherCache(), FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLat = 52.0f, weatherLon = 13.0f))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.Success)
        val success = result as FetchResult.Success
        assertEquals(WeatherCondition.CLEAR, success.data.condition)
        assertEquals(20.0, success.data.temperature, 0.001)
    }

    @Test
    fun `fetchNow returns Success with geocoded location`() = runTest {
        val weatherData = WeatherData(WeatherCondition.CLOUDY, 15.0, false)
        val fakeApi = FakeWeatherApi(weatherResult = weatherData, geocodeResult = Pair(52.52, 13.405))
        val fakeCache = FakeWeatherCache()
        val scheduler = WeatherUpdateScheduler(
            fakeApi, fakeCache, FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLocation = "Berlin"))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.Success)
        assertEquals("Berlin", fakeCache.savedLocation?.first)
        assertEquals(52.52, fakeApi.fetchWeatherCalls[0].first, 0.001)
        assertEquals(13.405, fakeApi.fetchWeatherCalls[0].second, 0.001)
    }

    @Test
    fun `fetchNow returns FetchFailed when API returns null`() = runTest {
        val scheduler = WeatherUpdateScheduler(
            FakeWeatherApi(weatherResult = null, geocodeResult = Pair(52.0, 13.0)),
            FakeWeatherCache(), FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLocation = "Berlin"))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.FetchFailed)
    }

    @Test
    fun `fetchNow uses GPS when weatherUseGps is true`() = runTest {
        val weatherData = WeatherData(WeatherCondition.RAIN, 10.0, true)
        val fakeApi = FakeWeatherApi(weatherResult = weatherData)
        val scheduler = WeatherUpdateScheduler(
            fakeApi, FakeWeatherCache(),
            FakeLocationProvider(lastKnown = Pair(48.0, 11.0)),
            FakePreferencesManager(makeConfig(weatherUseGps = true))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.Success)
        assertEquals(48.0, fakeApi.fetchWeatherCalls[0].first, 0.001)
        assertEquals(11.0, fakeApi.fetchWeatherCalls[0].second, 0.001)
    }

    @Test
    fun `fetchNow uses cached location when query matches`() = runTest {
        val weatherData = WeatherData(WeatherCondition.CLEAR, 25.0, true)
        val fakeApi = FakeWeatherApi(weatherResult = weatherData)
        val fakeCache = FakeWeatherCache().apply {
            cachedLocation = Triple("Berlin", 52.52, 13.405)
        }
        val scheduler = WeatherUpdateScheduler(
            fakeApi, fakeCache, FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLocation = "Berlin"))
        )
        var result: FetchResult? = null
        scheduler.fetchNow(context, this, onResult = { result = it })
        advanceUntilIdle()
        assertTrue(result is FetchResult.Success)
        assertTrue(fakeApi.geocodeCalls.isEmpty())
        assertEquals(52.52, fakeApi.fetchWeatherCalls[0].first, 0.001)
    }

    @Test
    fun `fetchNow caches weather data on success`() = runTest {
        val weatherData = WeatherData(WeatherCondition.CLEAR, 20.0, true)
        val fakeCache = FakeWeatherCache()
        val scheduler = WeatherUpdateScheduler(
            FakeWeatherApi(weatherResult = weatherData), fakeCache, FakeLocationProvider(),
            FakePreferencesManager(makeConfig(weatherLat = 52.0f, weatherLon = 13.0f))
        )
        scheduler.fetchNow(context, this, onResult = {})
        advanceUntilIdle()
        assertEquals(WeatherCondition.CLEAR, fakeCache.savedData?.condition)
        assertEquals(20.0, fakeCache.savedData?.temperature ?: 0.0, 0.001)
    }
}
