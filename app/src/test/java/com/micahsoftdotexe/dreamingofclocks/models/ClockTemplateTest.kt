package com.micahsoftdotexe.dreamingofclocks.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockTemplateTest {

    @Test
    fun `findByName returns CLASSIC for exact match`() {
        assertEquals(ClockTemplate.CLASSIC, ClockTemplate.findByName("Classic"))
    }

    @Test
    fun `findByName is case insensitive`() {
        assertEquals(ClockTemplate.CLASSIC, ClockTemplate.findByName("classic"))
        assertEquals(ClockTemplate.CLASSIC, ClockTemplate.findByName("CLASSIC"))
        assertEquals(ClockTemplate.ELEGANT, ClockTemplate.findByName("elegant"))
    }

    @Test
    fun `findByName returns null for nonexistent name`() {
        assertNull(ClockTemplate.findByName("nonexistent"))
        assertNull(ClockTemplate.findByName(""))
    }

    @Test
    fun `ALL_BUILT_IN has 5 templates`() {
        assertEquals(5, ClockTemplate.ALL_BUILT_IN.size)
    }

    @Test
    fun `all built-in templates have unique names`() {
        val names = ClockTemplate.ALL_BUILT_IN.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `all built-in templates have valid fraction values`() {
        for (template in ClockTemplate.ALL_BUILT_IN) {
            val face = template.face
            assertInRange(face.radiusFraction, "radiusFraction in ${template.name}")
            assertInRange(face.numberFontSizeFraction, "numberFontSizeFraction in ${template.name}")
            assertInRange(face.minuteTickLengthFraction, "minuteTickLengthFraction in ${template.name}")
            assertInRange(face.hourTickLengthFraction, "hourTickLengthFraction in ${template.name}")
            assertInRange(face.centerDotRadiusFraction, "centerDotRadiusFraction in ${template.name}")

            assertInRange(template.hands.hour.lengthFraction, "hour lengthFraction in ${template.name}")
            assertInRange(template.hands.hour.tailFraction, "hour tailFraction in ${template.name}")
            assertInRange(template.hands.minute.lengthFraction, "minute lengthFraction in ${template.name}")
            assertInRange(template.hands.minute.tailFraction, "minute tailFraction in ${template.name}")
        }
    }

    @Test
    fun `MINIMAL has no second hand`() {
        assertEquals(false, ClockTemplate.MINIMAL.hands.second.show)
    }

    @Test
    fun `ELEGANT uses roman numerals`() {
        assertEquals("roman", ClockTemplate.ELEGANT.face.numberStyle)
    }

    @Test
    fun `CLASSIC has second hand shown`() {
        assertTrue(ClockTemplate.CLASSIC.hands.second.show)
    }

    @Test
    fun `COMPACT has smaller radius`() {
        assertTrue(ClockTemplate.COMPACT.face.radiusFraction < ClockTemplate.CLASSIC.face.radiusFraction)
    }

    @Test
    fun `MODERN uses square caps`() {
        assertEquals("square", ClockTemplate.MODERN.hands.hour.cap)
        assertEquals("square", ClockTemplate.MODERN.hands.minute.cap)
    }

    @Test
    fun `findByName finds all built-in templates`() {
        for (template in ClockTemplate.ALL_BUILT_IN) {
            assertNotNull("Should find ${template.name}", ClockTemplate.findByName(template.name))
        }
    }

    private fun assertInRange(value: Float, label: String) {
        assertTrue("$label should be >= 0 but was $value", value >= 0f)
        assertTrue("$label should be <= 1 but was $value", value <= 1f)
    }
}
