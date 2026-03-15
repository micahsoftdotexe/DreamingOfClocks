package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockConfiguratorTest {

    @Test
    fun `24 hour with seconds`() {
        assertEquals("HH:mm:ss", ClockConfigurator.buildClockFormat(is24Hour = true, showSeconds = true))
    }

    @Test
    fun `24 hour without seconds`() {
        assertEquals("HH:mm", ClockConfigurator.buildClockFormat(is24Hour = true, showSeconds = false))
    }

    @Test
    fun `12 hour with seconds`() {
        assertEquals("hh:mm:ss a", ClockConfigurator.buildClockFormat(is24Hour = false, showSeconds = true))
    }

    @Test
    fun `12 hour without seconds`() {
        assertEquals("hh:mm a", ClockConfigurator.buildClockFormat(is24Hour = false, showSeconds = false))
    }
}
