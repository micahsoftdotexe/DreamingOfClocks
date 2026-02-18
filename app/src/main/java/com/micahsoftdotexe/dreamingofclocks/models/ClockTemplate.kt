package com.micahsoftdotexe.dreamingofclocks.models

data class ClockTemplate(
    val name: String,
    val version: Int,
    val face: FaceConfig,
    val hands: HandsConfig,
    val widgets: WidgetsConfig
) {
    companion object {
        val CLASSIC = ClockTemplate(
            name = "Classic",
            version = 1,
            face = FaceConfig(
                radiusFraction = 0.4f,
                fillColor = null,
                borderColor = null,
                borderWidthDp = 3f,
                showHourNumbers = true,
                numberStyle = "arabic",
                numberFontSizeFraction = 0.08f,
                showMinuteTicks = true,
                minuteTickLengthFraction = 0.03f,
                minuteTickWidthDp = 1f,
                showHourTicks = true,
                hourTickLengthFraction = 0.06f,
                hourTickWidthDp = 2.5f,
                showCenterDot = true,
                centerDotRadiusFraction = 0.02f
            ),
            hands = HandsConfig(
                hour = HandConfig(lengthFraction = 0.5f, widthDp = 6f, tailFraction = 0.1f, color = null, cap = "round"),
                minute = HandConfig(lengthFraction = 0.7f, widthDp = 4f, tailFraction = 0.1f, color = null, cap = "round"),
                second = SecondHandConfig(show = true, lengthFraction = 0.8f, widthDp = 1.5f, tailFraction = 0.15f, color = "#FF0000", cap = "butt")
            ),
            widgets = WidgetsConfig(
                date = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.1f, fontSizeSp = 20),
                alarm = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.15f, fontSizeSp = 16),
                media = WidgetPosition(position = "above", offsetXFraction = 0f, offsetYFraction = -0.1f, fontSizeSp = 14)
            )
        )

        val MINIMAL = ClockTemplate(
            name = "Minimal",
            version = 1,
            face = FaceConfig(
                radiusFraction = 0.4f,
                fillColor = null,
                borderColor = null,
                borderWidthDp = 2f,
                showHourNumbers = false,
                numberStyle = "none",
                numberFontSizeFraction = 0f,
                showMinuteTicks = false,
                minuteTickLengthFraction = 0f,
                minuteTickWidthDp = 0f,
                showHourTicks = false,
                hourTickLengthFraction = 0f,
                hourTickWidthDp = 0f,
                showCenterDot = true,
                centerDotRadiusFraction = 0.01f
            ),
            hands = HandsConfig(
                hour = HandConfig(lengthFraction = 0.45f, widthDp = 3f, tailFraction = 0.08f, color = null, cap = "round"),
                minute = HandConfig(lengthFraction = 0.65f, widthDp = 2f, tailFraction = 0.08f, color = null, cap = "round"),
                second = SecondHandConfig(show = false, lengthFraction = 0f, widthDp = 0f, tailFraction = 0f, color = null, cap = "butt")
            ),
            widgets = WidgetsConfig(
                date = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.1f, fontSizeSp = 20),
                alarm = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.15f, fontSizeSp = 16),
                media = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.2f, fontSizeSp = 14)
            )
        )

        val MODERN = ClockTemplate(
            name = "Modern",
            version = 1,
            face = FaceConfig(
                radiusFraction = 0.4f,
                fillColor = null,
                borderColor = null,
                borderWidthDp = 4f,
                showHourNumbers = false,
                numberStyle = "none",
                numberFontSizeFraction = 0f,
                showMinuteTicks = false,
                minuteTickLengthFraction = 0f,
                minuteTickWidthDp = 0f,
                showHourTicks = true,
                hourTickLengthFraction = 0.08f,
                hourTickWidthDp = 4f,
                showCenterDot = true,
                centerDotRadiusFraction = 0.025f
            ),
            hands = HandsConfig(
                hour = HandConfig(lengthFraction = 0.45f, widthDp = 8f, tailFraction = 0.1f, color = null, cap = "square"),
                minute = HandConfig(lengthFraction = 0.65f, widthDp = 5f, tailFraction = 0.1f, color = null, cap = "square"),
                second = SecondHandConfig(show = true, lengthFraction = 0.75f, widthDp = 2f, tailFraction = 0.15f, color = "#FF5722", cap = "butt")
            ),
            widgets = WidgetsConfig(
                date = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.08f, fontSizeSp = 20),
                alarm = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.15f, fontSizeSp = 16),
                media = WidgetPosition(position = "above", offsetXFraction = 0f, offsetYFraction = -0.08f, fontSizeSp = 14)
            )
        )

        val ELEGANT = ClockTemplate(
            name = "Elegant",
            version = 1,
            face = FaceConfig(
                radiusFraction = 0.4f,
                fillColor = null,
                borderColor = null,
                borderWidthDp = 2f,
                showHourNumbers = true,
                numberStyle = "roman",
                numberFontSizeFraction = 0.07f,
                showMinuteTicks = true,
                minuteTickLengthFraction = 0.02f,
                minuteTickWidthDp = 0.5f,
                showHourTicks = true,
                hourTickLengthFraction = 0.05f,
                hourTickWidthDp = 1.5f,
                showCenterDot = true,
                centerDotRadiusFraction = 0.015f
            ),
            hands = HandsConfig(
                hour = HandConfig(lengthFraction = 0.45f, widthDp = 4f, tailFraction = 0.1f, color = null, cap = "round"),
                minute = HandConfig(lengthFraction = 0.65f, widthDp = 2.5f, tailFraction = 0.1f, color = null, cap = "round"),
                second = SecondHandConfig(show = true, lengthFraction = 0.7f, widthDp = 1f, tailFraction = 0.2f, color = "#FFD700", cap = "round")
            ),
            widgets = WidgetsConfig(
                date = WidgetPosition(position = "on_face", offsetXFraction = 0.15f, offsetYFraction = 0f, fontSizeSp = 14),
                alarm = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.1f, fontSizeSp = 16),
                media = WidgetPosition(position = "above", offsetXFraction = 0f, offsetYFraction = -0.1f, fontSizeSp = 14)
            )
        )

        val COMPACT = ClockTemplate(
            name = "Compact",
            version = 1,
            face = FaceConfig(
                radiusFraction = 0.25f,
                fillColor = null,
                borderColor = null,
                borderWidthDp = 2f,
                showHourNumbers = true,
                numberStyle = "arabic",
                numberFontSizeFraction = 0.05f,
                showMinuteTicks = false,
                minuteTickLengthFraction = 0f,
                minuteTickWidthDp = 0f,
                showHourTicks = true,
                hourTickLengthFraction = 0.04f,
                hourTickWidthDp = 2f,
                showCenterDot = true,
                centerDotRadiusFraction = 0.015f
            ),
            hands = HandsConfig(
                hour = HandConfig(lengthFraction = 0.5f, widthDp = 5f, tailFraction = 0.1f, color = null, cap = "round"),
                minute = HandConfig(lengthFraction = 0.7f, widthDp = 3f, tailFraction = 0.1f, color = null, cap = "round"),
                second = SecondHandConfig(show = true, lengthFraction = 0.75f, widthDp = 1f, tailFraction = 0.15f, color = "#FF0000", cap = "butt")
            ),
            widgets = WidgetsConfig(
                date = WidgetPosition(position = "below", offsetXFraction = 0f, offsetYFraction = 0.08f, fontSizeSp = 20),
                alarm = WidgetPosition(position = "above", offsetXFraction = 0f, offsetYFraction = -0.08f, fontSizeSp = 16),
                media = WidgetPosition(position = "right", offsetXFraction = 0.1f, offsetYFraction = 0f, fontSizeSp = 14)
            )
        )

        val ALL_BUILT_IN = listOf(CLASSIC, MINIMAL, MODERN, ELEGANT, COMPACT)

        fun findByName(name: String): ClockTemplate? =
            ALL_BUILT_IN.find { it.name.equals(name, ignoreCase = true) }
    }
}

data class FaceConfig(
    val radiusFraction: Float,
    val fillColor: String?,
    val borderColor: String?,
    val borderWidthDp: Float,
    val showHourNumbers: Boolean,
    val numberStyle: String, // "arabic", "roman", "none"
    val numberFontSizeFraction: Float,
    val showMinuteTicks: Boolean,
    val minuteTickLengthFraction: Float,
    val minuteTickWidthDp: Float,
    val showHourTicks: Boolean,
    val hourTickLengthFraction: Float,
    val hourTickWidthDp: Float,
    val showCenterDot: Boolean,
    val centerDotRadiusFraction: Float
)

data class HandConfig(
    val lengthFraction: Float,
    val widthDp: Float,
    val tailFraction: Float,
    val color: String?,
    val cap: String // "round", "butt", "square"
)

data class HandsConfig(
    val hour: HandConfig,
    val minute: HandConfig,
    val second: SecondHandConfig
)

data class SecondHandConfig(
    val show: Boolean,
    val lengthFraction: Float,
    val widthDp: Float,
    val tailFraction: Float,
    val color: String?,
    val cap: String
)

data class WidgetPosition(
    val position: String, // "above", "below", "left", "right", "on_face"
    val offsetXFraction: Float,
    val offsetYFraction: Float,
    val fontSizeSp: Int
)

data class WidgetsConfig(
    val date: WidgetPosition,
    val alarm: WidgetPosition,
    val media: WidgetPosition
)
