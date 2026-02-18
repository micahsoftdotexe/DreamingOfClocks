package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context
import android.net.Uri
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.models.FaceConfig
import com.micahsoftdotexe.dreamingofclocks.models.HandConfig
import com.micahsoftdotexe.dreamingofclocks.models.HandsConfig
import com.micahsoftdotexe.dreamingofclocks.models.SecondHandConfig
import com.micahsoftdotexe.dreamingofclocks.models.WidgetPosition
import com.micahsoftdotexe.dreamingofclocks.models.WidgetsConfig
import org.json.JSONObject
import androidx.core.net.toUri

object TemplateManager {

    fun getBuiltInTemplates(): List<ClockTemplate> = ClockTemplate.ALL_BUILT_IN

    fun loadTemplateFromJson(json: String): Result<ClockTemplate> = runCatching {
        val root = JSONObject(json)
        ClockTemplate(
            name = root.getString("name"),
            version = root.optInt("version", 1),
            face = parseFace(root.getJSONObject("face")),
            hands = parseHands(root.getJSONObject("hands")),
            widgets = parseWidgets(root.getJSONObject("widgets"))
        )
    }

    fun loadTemplateFromUri(context: Context, uri: Uri): Result<ClockTemplate> = runCatching {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalArgumentException("Could not read file")
        loadTemplateFromJson(json).getOrThrow()
    }

    fun getActiveTemplate(context: Context): ClockTemplate {
        val config = PreferencesManager.loadConfig(context)
        val customUri = config.customTemplateUri
        if (!customUri.isNullOrEmpty()) {
            val result = loadTemplateFromUri(context, customUri.toUri())
            if (result.isSuccess) return result.getOrThrow()
        }
        return ClockTemplate.findByName(config.analogTemplate) ?: ClockTemplate.CLASSIC
    }

    private fun parseFace(obj: JSONObject): FaceConfig = FaceConfig(
        radiusFraction = clamp(obj.optDouble("radius_fraction", 0.4).toFloat()),
        fillColor = obj.optStringOrNull("fill_color"),
        borderColor = obj.optStringOrNull("border_color"),
        borderWidthDp = obj.optDouble("border_width_dp", 3.0).toFloat(),
        showHourNumbers = obj.optBoolean("show_hour_numbers", true),
        numberStyle = obj.optString("number_style", "arabic"),
        numberFontSizeFraction = clamp(obj.optDouble("number_font_size_fraction", 0.08).toFloat()),
        showMinuteTicks = obj.optBoolean("show_minute_ticks", true),
        minuteTickLengthFraction = clamp(obj.optDouble("minute_tick_length_fraction", 0.03).toFloat()),
        minuteTickWidthDp = obj.optDouble("minute_tick_width_dp", 1.0).toFloat(),
        showHourTicks = obj.optBoolean("show_hour_ticks", true),
        hourTickLengthFraction = clamp(obj.optDouble("hour_tick_length_fraction", 0.06).toFloat()),
        hourTickWidthDp = obj.optDouble("hour_tick_width_dp", 2.5).toFloat(),
        showCenterDot = obj.optBoolean("show_center_dot", true),
        centerDotRadiusFraction = clamp(obj.optDouble("center_dot_radius_fraction", 0.02).toFloat())
    )

    private fun parseHands(obj: JSONObject): HandsConfig {
        val hour = parseHand(obj.getJSONObject("hour"))
        val minute = parseHand(obj.getJSONObject("minute"))
        val sec = obj.getJSONObject("second")
        return HandsConfig(
            hour = hour,
            minute = minute,
            second = SecondHandConfig(
                show = sec.optBoolean("show", true),
                lengthFraction = clamp(sec.optDouble("length_fraction", 0.8).toFloat()),
                widthDp = sec.optDouble("width_dp", 1.5).toFloat(),
                tailFraction = clamp(sec.optDouble("tail_fraction", 0.15).toFloat()),
                color = sec.optStringOrNull("color"),
                cap = sec.optString("cap", "butt")
            )
        )
    }

    private fun parseHand(obj: JSONObject): HandConfig = HandConfig(
        lengthFraction = clamp(obj.optDouble("length_fraction", 0.5).toFloat()),
        widthDp = obj.optDouble("width_dp", 4.0).toFloat(),
        tailFraction = clamp(obj.optDouble("tail_fraction", 0.1).toFloat()),
        color = obj.optStringOrNull("color"),
        cap = obj.optString("cap", "round")
    )

    private fun parseWidgets(obj: JSONObject): WidgetsConfig = WidgetsConfig(
        date = parseWidgetPosition(obj.getJSONObject("date")),
        alarm = parseWidgetPosition(obj.getJSONObject("alarm")),
        media = parseWidgetPosition(obj.getJSONObject("media"))
    )

    private fun parseWidgetPosition(obj: JSONObject): WidgetPosition = WidgetPosition(
        position = obj.optString("position", "below"),
        offsetXFraction = obj.optDouble("offset_x_fraction", 0.0).toFloat(),
        offsetYFraction = obj.optDouble("offset_y_fraction", 0.0).toFloat(),
        fontSizeSp = obj.optInt("font_size_sp", 16)
    )

    private fun clamp(value: Float): Float = value.coerceIn(0f, 1f)

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (isNull(key)) return null
        val value = optString(key, "")
        return value.ifEmpty { null }
    }
}
