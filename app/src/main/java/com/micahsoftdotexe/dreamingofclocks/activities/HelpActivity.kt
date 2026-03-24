package com.micahsoftdotexe.dreamingofclocks.activities

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Getting Started",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Dreaming of Clocks is a screen saver (formerly called Daydream) that displays a clock when your device is idle or charging.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "How to Enable",
                    style = MaterialTheme.typography.titleMedium
                )
                Step(1, "Open your device's Settings app")
                Step(2, "Go to Display > Screen saver (the exact path may vary by device)")
                Step(3, "Select \"Dreaming of Clocks\" from the list")
                Step(4, "Choose when to start: while charging, while docked, or both")
            }
        }

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Screensaver Settings")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Customization",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Use the Settings tab in this app to customize your screensaver:",
                    style = MaterialTheme.typography.bodyMedium
                )
                BulletPoint("Choose between digital and analog clock styles")
                BulletPoint("Pick from 5 built-in analog clock templates")
                BulletPoint("Set background to a solid color, image, or animated weather")
                BulletPoint("Customize fonts, colors, and date display")
                BulletPoint("Show now-playing media info and next alarm")
            }
        }
    }
}

@Composable
private fun Step(number: Int, text: String) {
    Row {
        Text(
            "$number. ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row {
        Text(
            "\u2022  ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
