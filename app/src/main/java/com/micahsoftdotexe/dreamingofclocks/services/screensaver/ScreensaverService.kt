package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.service.dreams.DreamService
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.graphics.drawable.toDrawable

class ScreensaverService: DreamService() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private lateinit var dateText: TextView
    private lateinit var textClock: TextClock

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the date whenever time ticks or date/time zone changes
            updateDate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // not interactive, full screen
        isInteractive = false
        isFullscreen = true

        setContentView(R.layout.screensaver_layout)
        textClock = findViewById(R.id.textClockScreensaver)
        dateText = findViewById(R.id.dateTextScreensaver)
        // Ensure formats include seconds (may be overridden by preferences)
        textClock.format24Hour = "HH:mm:ss"
        textClock.format12Hour = "hh:mm:ss a"
        updateDate()

        // Apply saved settings
        applyPreferences()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(timeReceiver, filter)
    }

    private fun applyPreferences() {
        val prefs = getSharedPreferences("clock_prefs", MODE_PRIVATE)
        val is24Hour = prefs.getBoolean("pref_24_hour", true)
        val showDate = prefs.getBoolean("pref_show_date", true)
        val bgMode = prefs.getString("pref_bg_mode", "color") ?: "color"
        val bgColor = prefs.getString("pref_bg_color", "#000000") ?: "#000000"
        val bgImageUri = prefs.getString("pref_bg_image_uri", null)
        val textColor = prefs.getString("pref_text_color", "#FFFFFF") ?: "#FFFFFF"

        // Clock format
        if (is24Hour) {
            textClock.format24Hour = "HH:mm:ss"
            textClock.format12Hour = "HH:mm:ss"
        } else {
            textClock.format24Hour = "hh:mm:ss a"
            textClock.format12Hour = "hh:mm:ss a"
        }

        // Date visibility
        dateText.visibility = if (showDate) View.VISIBLE else View.GONE

        // Text color
        try {
            val tc = textColor.toColorInt()
            textClock.setTextColor(tc)
            dateText.setTextColor(tc)
        } catch (e: Exception) {
            // ignore parse errors
        }

        // Background: image preferred when mode=image and uri present
        if (bgMode == "image" && !bgImageUri.isNullOrEmpty()) {
            try {
                val uri = bgImageUri.toUri()
                val input = contentResolver.openInputStream(uri)
                input?.use {
                    val bmp = BitmapFactory.decodeStream(it)
                    window?.decorView?.background = bmp.toDrawable(resources)
                }
            } catch (e: Exception) {
                // fallback to color if image fails
                try {
                    window?.decorView?.setBackgroundColor(bgColor.toColorInt())
                } catch (ex: Exception) {
                    window?.decorView?.setBackgroundColor(Color.BLACK)
                }
            }
        } else {
            try {
                window?.decorView?.setBackgroundColor(bgColor.toColorInt())
            } catch (e: Exception) {
                window?.decorView?.setBackgroundColor(Color.BLACK)
            }
        }
    }

    private fun updateDate() {
        dateText.text = dateFormatter.format(Date())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterReceiver(timeReceiver)
        // no handler callbacks to remove anymore
    }
}