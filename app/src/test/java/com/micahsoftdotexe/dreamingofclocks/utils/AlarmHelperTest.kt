package com.micahsoftdotexe.dreamingofclocks.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class AlarmHelperTest {

    private val helper = AlarmHelper()

    @Test
    fun `days hours and minutes`() {
        val ms = TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(15)
        assertEquals("Alarm in 2 days, 3 hours, 15 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `hours and minutes only`() {
        val ms = TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(30)
        assertEquals("Alarm in 5 hours, 30 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `minutes only`() {
        val ms = TimeUnit.MINUTES.toMillis(45)
        assertEquals("Alarm in 45 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `exactly zero minutes shows 0 minutes`() {
        val ms = TimeUnit.SECONDS.toMillis(30) // less than a minute but positive
        assertEquals("Alarm in 0 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `1 day singular`() {
        val ms = TimeUnit.DAYS.toMillis(1)
        assertEquals("Alarm in 1 day", helper.formatCountdown(ms))
    }

    @Test
    fun `2 days plural`() {
        val ms = TimeUnit.DAYS.toMillis(2)
        assertEquals("Alarm in 2 days", helper.formatCountdown(ms))
    }

    @Test
    fun `1 hour singular`() {
        val ms = TimeUnit.HOURS.toMillis(1)
        assertEquals("Alarm in 1 hour", helper.formatCountdown(ms))
    }

    @Test
    fun `2 hours plural`() {
        val ms = TimeUnit.HOURS.toMillis(2)
        assertEquals("Alarm in 2 hours", helper.formatCountdown(ms))
    }

    @Test
    fun `1 minute singular`() {
        val ms = TimeUnit.MINUTES.toMillis(1)
        assertEquals("Alarm in 1 minute", helper.formatCountdown(ms))
    }

    @Test
    fun `2 minutes plural`() {
        val ms = TimeUnit.MINUTES.toMillis(2)
        assertEquals("Alarm in 2 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `negative diff returns null`() {
        assertNull(helper.formatCountdown(-1000))
    }

    @Test
    fun `zero diff returns null`() {
        assertNull(helper.formatCountdown(0))
    }

    @Test
    fun `days and minutes no hours`() {
        val ms = TimeUnit.DAYS.toMillis(1) + TimeUnit.MINUTES.toMillis(5)
        assertEquals("Alarm in 1 day, 5 minutes", helper.formatCountdown(ms))
    }

    @Test
    fun `days and hours no minutes`() {
        val ms = TimeUnit.DAYS.toMillis(3) + TimeUnit.HOURS.toMillis(2)
        assertEquals("Alarm in 3 days, 2 hours", helper.formatCountdown(ms))
    }
}
