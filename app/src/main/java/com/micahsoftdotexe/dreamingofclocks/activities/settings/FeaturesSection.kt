package com.micahsoftdotexe.dreamingofclocks.activities.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.micahsoftdotexe.dreamingofclocks.activities.Section
import com.micahsoftdotexe.dreamingofclocks.activities.SubHeading
import com.micahsoftdotexe.dreamingofclocks.utils.MediaNotificationListener

@Composable
fun FeaturesSection(
    showAlarm: Boolean, onShowAlarmChange: (Boolean) -> Unit,
    showMedia: Boolean, onShowMediaChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    Section("Features") {
        SubHeading("Alarm")
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show next alarm")
            Switch(checked = showAlarm, onCheckedChange = onShowAlarmChange)
        }

        SubHeading("Now Playing")
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show media info")
            Switch(checked = showMedia, onCheckedChange = { enabled ->
                if (enabled && !isNotificationListenerEnabled(context)) {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    )
                } else {
                    onShowMediaChange(enabled)
                }
            })
        }
        if (showMedia && !isNotificationListenerEnabled(context)) {
            Text(
                "Notification access is required for media info. Tap the toggle to open settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val componentName = ComponentName(context, MediaNotificationListener::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(componentName.flattenToString()) == true
}
