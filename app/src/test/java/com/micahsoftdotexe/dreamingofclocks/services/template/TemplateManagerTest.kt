package com.micahsoftdotexe.dreamingofclocks.services.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateManagerTest {

    private val validJson = """
    {
        "name": "TestTemplate",
        "version": 2,
        "face": {
            "radius_fraction": 0.35,
            "fill_color": "#112233",
            "border_color": "#AABBCC",
            "border_width_dp": 4.0,
            "show_hour_numbers": false,
            "number_style": "roman",
            "number_font_size_fraction": 0.06,
            "show_minute_ticks": false,
            "minute_tick_length_fraction": 0.02,
            "minute_tick_width_dp": 0.5,
            "show_hour_ticks": true,
            "hour_tick_length_fraction": 0.05,
            "hour_tick_width_dp": 2.0,
            "show_center_dot": true,
            "center_dot_radius_fraction": 0.015
        },
        "hands": {
            "hour": {
                "length_fraction": 0.45,
                "width_dp": 5.0,
                "tail_fraction": 0.12,
                "color": "#FF0000",
                "cap": "square"
            },
            "minute": {
                "length_fraction": 0.65,
                "width_dp": 3.0,
                "tail_fraction": 0.08,
                "cap": "round"
            },
            "second": {
                "show": true,
                "length_fraction": 0.8,
                "width_dp": 1.0,
                "tail_fraction": 0.15,
                "color": "#00FF00",
                "cap": "butt"
            }
        },
        "widgets": {
            "date": { "position": "below", "offset_x_fraction": 0.0, "offset_y_fraction": 0.1, "font_size_sp": 18 },
            "alarm": { "position": "above", "offset_x_fraction": 0.0, "offset_y_fraction": -0.1, "font_size_sp": 14 },
            "media": { "position": "right", "offset_x_fraction": 0.05, "offset_y_fraction": 0.0, "font_size_sp": 12 }
        }
    }
    """.trimIndent()

    @Test
    fun `parse valid complete JSON`() {
        val result = TemplateManager.loadTemplateFromJson(validJson)
        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertEquals("TestTemplate", template.name)
        assertEquals(2, template.version)
        assertEquals(0.35f, template.face.radiusFraction, 0.01f)
        assertEquals("#112233", template.face.fillColor)
        assertEquals("roman", template.face.numberStyle)
        assertEquals(false, template.face.showHourNumbers)
        assertEquals("square", template.hands.hour.cap)
        assertEquals("#FF0000", template.hands.hour.color)
        assertNull(template.hands.minute.color)
        assertEquals(true, template.hands.second.show)
        assertEquals("#00FF00", template.hands.second.color)
        assertEquals("below", template.widgets.date.position)
        assertEquals(18, template.widgets.date.fontSizeSp)
    }

    @Test
    fun `parse JSON with missing optional fields uses defaults`() {
        val json = """
        {
            "name": "Sparse",
            "face": {},
            "hands": {
                "hour": {},
                "minute": {},
                "second": {}
            },
            "widgets": {
                "date": {},
                "alarm": {},
                "media": {}
            }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertEquals("Sparse", template.name)
        assertEquals(1, template.version)
        assertEquals(0.4f, template.face.radiusFraction, 0.01f)
        assertNull(template.face.fillColor)
        assertEquals(true, template.face.showHourNumbers)
        assertEquals("arabic", template.face.numberStyle)
        assertEquals(0.5f, template.hands.hour.lengthFraction, 0.01f)
        assertEquals("round", template.hands.hour.cap)
        assertEquals("below", template.widgets.date.position)
        assertEquals(16, template.widgets.date.fontSizeSp)
    }

    @Test
    fun `parse invalid JSON returns failure`() {
        val result = TemplateManager.loadTemplateFromJson("not json at all")
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse JSON missing required name field returns failure`() {
        val json = """
        {
            "face": {},
            "hands": { "hour": {}, "minute": {}, "second": {} },
            "widgets": { "date": {}, "alarm": {}, "media": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse JSON missing required face field returns failure`() {
        val json = """
        {
            "name": "NoFace",
            "hands": { "hour": {}, "minute": {}, "second": {} },
            "widgets": { "date": {}, "alarm": {}, "media": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse JSON missing required hands field returns failure`() {
        val json = """
        {
            "name": "NoHands",
            "face": {},
            "widgets": { "date": {}, "alarm": {}, "media": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse JSON missing required widgets field returns failure`() {
        val json = """
        {
            "name": "NoWidgets",
            "face": {},
            "hands": { "hour": {}, "minute": {}, "second": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `clamp constrains out-of-range fraction values`() {
        val json = """
        {
            "name": "OutOfRange",
            "face": {
                "radius_fraction": 1.5,
                "number_font_size_fraction": -0.5,
                "minute_tick_length_fraction": 2.0,
                "hour_tick_length_fraction": -1.0,
                "center_dot_radius_fraction": 3.0
            },
            "hands": {
                "hour": { "length_fraction": 5.0, "tail_fraction": -0.2 },
                "minute": { "length_fraction": -1.0, "tail_fraction": 1.5 },
                "second": { "length_fraction": 2.0, "tail_fraction": -0.5 }
            },
            "widgets": {
                "date": {},
                "alarm": {},
                "media": {}
            }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertEquals(1.0f, template.face.radiusFraction, 0.001f)
        assertEquals(0.0f, template.face.numberFontSizeFraction, 0.001f)
        assertEquals(1.0f, template.face.minuteTickLengthFraction, 0.001f)
        assertEquals(0.0f, template.face.hourTickLengthFraction, 0.001f)
        assertEquals(1.0f, template.face.centerDotRadiusFraction, 0.001f)
        assertEquals(1.0f, template.hands.hour.lengthFraction, 0.001f)
        assertEquals(0.0f, template.hands.hour.tailFraction, 0.001f)
        assertEquals(0.0f, template.hands.minute.lengthFraction, 0.001f)
        assertEquals(1.0f, template.hands.minute.tailFraction, 0.001f)
        assertEquals(1.0f, template.hands.second.lengthFraction, 0.001f)
        assertEquals(0.0f, template.hands.second.tailFraction, 0.001f)
    }

    @Test
    fun `null color handling for missing color field`() {
        val json = """
        {
            "name": "NoColor",
            "face": {},
            "hands": {
                "hour": {},
                "minute": {},
                "second": {}
            },
            "widgets": { "date": {}, "alarm": {}, "media": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertNull(template.face.fillColor)
        assertNull(template.face.borderColor)
        assertNull(template.hands.hour.color)
        assertNull(template.hands.second.color)
    }

    @Test
    fun `empty string color treated as null`() {
        val json = """
        {
            "name": "EmptyColor",
            "face": { "fill_color": "", "border_color": "" },
            "hands": {
                "hour": { "color": "" },
                "minute": {},
                "second": { "color": "" }
            },
            "widgets": { "date": {}, "alarm": {}, "media": {} }
        }
        """.trimIndent()
        val result = TemplateManager.loadTemplateFromJson(json)
        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertNull(template.face.fillColor)
        assertNull(template.face.borderColor)
        assertNull(template.hands.hour.color)
        assertNull(template.hands.second.color)
    }
}
