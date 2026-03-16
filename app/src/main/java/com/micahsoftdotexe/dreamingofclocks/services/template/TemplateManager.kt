package com.micahsoftdotexe.dreamingofclocks.services.template

import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager

import android.content.Context
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate

class TemplateManager(private val preferencesManager: PreferencesManager) {

    fun getBuiltInTemplates(): List<ClockTemplate> = ClockTemplate.ALL_BUILT_IN

    fun getActiveTemplate(context: Context): ClockTemplate {
        val config = preferencesManager.loadConfig(context)
        return ClockTemplate.findByName(config.analogTemplate) ?: ClockTemplate.CLASSIC
    }
}
