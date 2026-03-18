package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.utils.AlarmHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverTimeManager(
    private val alarmHelper: AlarmHelper
) {
    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    private lateinit var dateText: TextView
    private lateinit var alarmText: TextView
    private lateinit var config: PreferencesManager.ScreensaverConfig

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateDate()
        }
    }

    private val alarmUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateAlarm()
        }
    }

    fun start(context: Context, dateText: TextView, alarmText: TextView, config: PreferencesManager.ScreensaverConfig) {
        this.dateText = dateText
        this.alarmText = alarmText
        this.config = config

        updateDate()
        if (config.showAlarm) updateAlarm()

        context.registerReceiver(timeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        context.registerReceiver(alarmUpdateReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        })
    }

    fun stop(context: Context) {
        context.unregisterReceiver(timeReceiver)
        context.unregisterReceiver(alarmUpdateReceiver)
    }

    private fun updateDate() {
        dateText.text = dateFormatter.format(Date())
    }

    private fun updateAlarm() {
        if (!config.showAlarm) {
            alarmText.visibility = View.GONE
            return
        }

        val alarmInfo = alarmHelper.formatNextAlarmCountdown(alarmText.context)
        if (alarmInfo != null) {
            alarmText.text = alarmInfo
            alarmText.visibility = View.VISIBLE
        } else {
            alarmText.visibility = View.GONE
        }
    }
}
