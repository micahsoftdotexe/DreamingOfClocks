package com.micahsoftdotexe.dreamingofclocks.models

import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.ScreensaverConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreensaverConfigTest {

    private fun defaultConfig() = ScreensaverConfig(
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
        weatherLocation = "",
        weatherUpdateFreq = 1_800_000L,
        weatherUseGps = false
    )

    @Test
    fun `create config with all fields`() {
        val config = defaultConfig()
        assertEquals(false, config.is24Hour)
        assertEquals(true, config.showDate)
        assertEquals("color", config.bgMode)
        assertEquals("#000000", config.bgColor)
        assertNull(config.bgImageUri)
        assertEquals("digital", config.clockMode)
        assertEquals("Classic", config.analogTemplate)
        assertEquals("sans-serif", config.clockFont)
        assertEquals(1_800_000L, config.weatherUpdateFreq)
        assertEquals(false, config.weatherUseGps)
    }

    @Test
    fun `copy changes only specified field`() {
        val original = defaultConfig()
        val modified = original.copy(is24Hour = true)
        assertEquals(true, modified.is24Hour)
        assertEquals(original.showDate, modified.showDate)
        assertEquals(original.bgMode, modified.bgMode)
        assertEquals(original.clockMode, modified.clockMode)
    }

    @Test
    fun `bgMode values`() {
        val color = defaultConfig().copy(bgMode = "color")
        assertEquals("color", color.bgMode)

        val image = defaultConfig().copy(bgMode = "image")
        assertEquals("image", image.bgMode)

        val weather = defaultConfig().copy(bgMode = "weather")
        assertEquals("weather", weather.bgMode)
    }

    @Test
    fun `clockMode values`() {
        val digital = defaultConfig().copy(clockMode = "digital")
        assertEquals("digital", digital.clockMode)

        val analog = defaultConfig().copy(clockMode = "analog")
        assertEquals("analog", analog.clockMode)
    }

    @Test
    fun `config with image URI`() {
        val config = defaultConfig().copy(
            bgMode = "image",
            bgImageUri = "content://media/external/images/123"
        )
        assertEquals("image", config.bgMode)
        assertEquals("content://media/external/images/123", config.bgImageUri)
    }

    @Test
    fun `equality works for identical configs`() {
        assertEquals(defaultConfig(), defaultConfig())
    }
}
