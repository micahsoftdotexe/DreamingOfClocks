package com.micahsoftdotexe.dreamingofclocks.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class GeocodingResultTest {

    private fun result(name: String, admin1: String? = null, country: String? = null) =
        GeocodingResult(name, admin1, country, 0.0, 0.0)

    @Test
    fun `all fields present`() {
        assertEquals("Berlin, Berlin, Germany", result("Berlin", "Berlin", "Germany").displayName)
    }

    @Test
    fun `null admin1`() {
        assertEquals("Singapore, Singapore", result("Singapore", null, "Singapore").displayName)
    }

    @Test
    fun `null country`() {
        assertEquals("Portland, Oregon", result("Portland", "Oregon", null).displayName)
    }

    @Test
    fun `null admin1 and country`() {
        assertEquals("Tokyo", result("Tokyo", null, null).displayName)
    }

    @Test
    fun `empty name string included`() {
        assertEquals(", California, US", result("", "California", "US").displayName)
    }
}
