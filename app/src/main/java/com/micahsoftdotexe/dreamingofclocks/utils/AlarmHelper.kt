package com.micahsoftdotexe.dreamingofclocks.utils

import android.app.AlarmManager
import android.content.Context
//import android.text.format.DateFormat
//import java.util.Calendar
import java.util.concurrent.TimeUnit

object AlarmHelper {

    /**
     * Gets the next scheduled alarm time, or null if none exists
     */
    fun getNextAlarmTime(context: Context): Long? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        return alarmManager?.nextAlarmClock?.triggerTime
    }

    internal fun formatCountdown(timeDiffMs: Long): String? {
        if (timeDiffMs <= 0) return null

        val days = TimeUnit.MILLISECONDS.toDays(timeDiffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDiffMs) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMs) % 60

        return buildString {
            append("Alarm in ")

            if (days > 0) {
                append("$days day${if (days > 1) "s" else ""}")
                if (hours > 0 || minutes > 0) append(", ")
            }

            if (hours > 0) {
                append("$hours hour${if (hours > 1) "s" else ""}")
                if (minutes > 0) append(", ")
            }

            if (minutes > 0 || (days == 0L && hours == 0L)) {
                append("$minutes minute${if (minutes != 1L) "s" else ""}")
            }
        }
    }

    /**
     * Formats the time until the next alarm in a human-readable format
     * Returns null if no alarm is set
     */
    fun formatNextAlarmCountdown(context: Context): String? {
        val nextAlarmTime = getNextAlarmTime(context) ?: return null
        val timeDiff = nextAlarmTime - System.currentTimeMillis()
        return formatCountdown(timeDiff)
    }

    /**
     * Formats the next alarm time as a clock time (e.g., "7:30 AM")
     */
//    fun formatNextAlarmTime(context: Context): String? {
//        val nextAlarmTime = getNextAlarmTime(context) ?: return null
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = nextAlarmTime
//        }
//        return DateFormat.getTimeFormat(context).format(calendar.time)
//    }
}
