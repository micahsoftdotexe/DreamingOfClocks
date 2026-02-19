package com.micahsoftdotexe.dreamingofclocks.utils

import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

object BackgroundRenderer {
    fun applyBackground(
        rootView: View,
        config: PreferencesManager.ScreensaverConfig,
        resources: Resources,
        contentResolver: ContentResolver
    ) {
        if (config.bgMode == "image" && !config.bgImageUri.isNullOrEmpty()) {
            try {
                val uri = config.bgImageUri.toUri()
                val input = contentResolver.openInputStream(uri)
                input?.use {
                    val bmp = BitmapFactory.decodeStream(it)
                    rootView.background = bmp.toDrawable(resources)
                }
            } catch (_: Exception) {
                applyColorBackground(rootView, config.bgColor)
            }
        } else {
            applyColorBackground(rootView, config.bgColor)
        }
    }

    private fun applyColorBackground(view: View, bgColor: String) {
        try {
            view.setBackgroundColor(bgColor.toColorInt())
        } catch (_: Exception) {
            view.setBackgroundColor(Color.BLACK)
        }
    }
}
