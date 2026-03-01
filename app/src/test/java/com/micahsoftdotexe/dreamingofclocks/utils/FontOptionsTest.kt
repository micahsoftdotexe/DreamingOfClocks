package com.micahsoftdotexe.dreamingofclocks.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FontOptionsTest {

    @Test
    fun `fontOptions has 10 entries`() {
        assertEquals(10, fontOptions.size)
    }

    @Test
    fun `all keys are unique`() {
        val keys = fontOptions.map { it.first }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `all labels are unique`() {
        val labels = fontOptions.map { it.second }
        assertEquals(labels.size, labels.toSet().size)
    }

    @Test
    fun `contains sans-serif`() {
        assertTrue(fontOptions.any { it.first == "sans-serif" })
    }

    @Test
    fun `contains dseg7`() {
        assertTrue(fontOptions.any { it.first == "dseg7" })
    }

    @Test
    fun `contains dseg14`() {
        assertTrue(fontOptions.any { it.first == "dseg14" })
    }

    @Test
    fun `contains serif`() {
        assertTrue(fontOptions.any { it.first == "serif" })
    }

    @Test
    fun `contains monospace`() {
        assertTrue(fontOptions.any { it.first == "monospace" })
    }

    @Test
    fun `contains casual`() {
        assertTrue(fontOptions.any { it.first == "casual" })
    }
}
