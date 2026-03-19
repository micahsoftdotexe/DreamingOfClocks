package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.service.dreams.DreamService
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.services.template.TemplateManager
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.uicomponents.analogclock.AnalogClockView

class ScreensaverLayoutManager(
    private val clockConfigurator: ClockConfigurator,
    private val analogClockConfigurator: AnalogClockConfigurator,
    private val templateManager: TemplateManager
) {
    data class ScreensaverViews(
        val rootLayout: View,
        val dateText: TextView,
        val alarmText: TextView,
        val mediaText: TextView,
        val textClock: TextClock?
    )

    fun setupLayout(service: DreamService, config: PreferencesManager.ScreensaverConfig): ScreensaverViews {
        val dateText: TextView
        val alarmText: TextView
        val mediaText: TextView
        var textClock: TextClock? = null
        var clockView: AnalogClockView? = null
        var template: ClockTemplate? = null

        if (config.clockMode == "analog") {
            service.setContentView(R.layout.screensaver_analog_layout)

            clockView = service.findViewById(R.id.analogClockScreensaver)
            template = templateManager.getActiveTemplate(service)
            analogClockConfigurator.configureAnalogClock(clockView, template, config)

            dateText = service.findViewById(R.id.dateTextScreensaver)
            alarmText = service.findViewById(R.id.alarmTextScreensaver)
            mediaText = service.findViewById(R.id.mediaTextScreensaver)

            clockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)
        } else {
            service.setContentView(R.layout.screensaver_layout)

            textClock = service.findViewById(R.id.textClockScreensaver)
            dateText = service.findViewById(R.id.dateTextScreensaver)
            alarmText = service.findViewById(R.id.alarmTextScreensaver)
            mediaText = service.findViewById(R.id.mediaTextScreensaver)

            clockConfigurator.applyClockFormat(textClock!!, config)
            clockConfigurator.applyTextColors(listOf(textClock, dateText, alarmText), config)
            clockConfigurator.applyFonts(service, textClock, listOf(dateText, alarmText, mediaText), config)
            clockConfigurator.applyVisibility(dateText, alarmText, mediaText, config)
        }

        val rootLayout = service.findViewById<View>(R.id.screensaver_root)

        if (clockView != null && template != null) {
            analogClockConfigurator.positionWidgets(
                rootLayout as FrameLayout, clockView, dateText, alarmText, mediaText, template, config
            )
        }

        return ScreensaverViews(rootLayout, dateText, alarmText, mediaText, textClock)
    }
}
