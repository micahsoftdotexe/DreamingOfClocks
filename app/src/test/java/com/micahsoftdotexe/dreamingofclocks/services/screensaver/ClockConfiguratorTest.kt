package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockConfiguratorTest {

    private val configurator = ClockConfigurator()

    @Test
    fun `24 hour with seconds`() {
        assertEquals("HH:mm:ss", configurator.buildClockFormat(is24Hour = true, showSeconds = true))
    }

    @Test
    fun `24 hour without seconds`() {
        assertEquals("HH:mm", configurator.buildClockFormat(is24Hour = true, showSeconds = false))
    }

    @Test
    fun `12 hour with seconds`() {
        assertEquals("hh:mm:ss a", configurator.buildClockFormat(is24Hour = false, showSeconds = true))
    }

    @Test
    fun `12 hour without seconds`() {
        assertEquals("hh:mm a", configurator.buildClockFormat(is24Hour = false, showSeconds = false))
    }
}
