package com.micahsoftdotexe.dreamingofclocks.activities

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker.ColorPicker
import com.micahsoftdotexe.dreamingofclocks.utils.rememberImagePickerLaunchers

// --- New: Settings UI and persistence ---
private const val PREFS_NAME = "clock_prefs"
private const val KEY_24_HOUR = "pref_24_hour"
private const val KEY_SHOW_DATE = "pref_show_date"
private const val KEY_SHOW_SECONDS = "pref_show_seconds"
private const val KEY_BG_MODE = "pref_bg_mode" // "color" or "image"
private const val KEY_BG_COLOR = "pref_bg_color" // hex string like #000000
private const val KEY_BG_IMAGE_URI = "pref_bg_image_uri" // uri string
private const val KEY_TEXT_COLOR = "pref_text_color" // hex string
private const val KEY_SHOW_ALARM = "pref_show_alarm" // boolean
private const val KEY_SHOW_MEDIA = "pref_show_media" // boolean

// helper composables for grouping
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
@Composable
private fun SubHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
@Preview
fun SettingsActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var is24Hour by remember { mutableStateOf(prefs.getBoolean(KEY_24_HOUR, false)) }
    var showSeconds by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SECONDS, false)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DATE, true)) }
    var showAlarm by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_ALARM, true)) }
    var showMedia by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_MEDIA, false)) }
    var bgMode by remember { mutableStateOf(prefs.getString(KEY_BG_MODE, "color") ?: "color") }
    var bgColor by remember { mutableStateOf(prefs.getString(KEY_BG_COLOR, "#000000") ?: "#000000") }
    var bgImageUri by remember { mutableStateOf(prefs.getString(KEY_BG_IMAGE_URI, null)) }
    var textColor by remember { mutableStateOf(prefs.getString(KEY_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF") }

//    if (bgMode == "image" && bgImageUri.isNullOrEmpty()) {
//        // Fallback to color mode if image URI is missing
//        bgMode = "color"
//    }

    // Set up image picker with proper permissions
    val imagePickerLaunchers = rememberImagePickerLaunchers(
        onImageSelected = { uri ->
            if (uri != null) {
                val uriString = uri.toString()
                prefs.edit {
                    putString(KEY_BG_IMAGE_URI, uriString)
                    putString(KEY_BG_MODE, "image")
                }
                bgImageUri = uriString
                bgMode = "image"
            }
        },
        onPermissionDenied = {
            // Optionally revert to color mode if user denies permission
            bgMode = "color"
            prefs.edit { putString(KEY_BG_MODE, "color") }
        }
    )

    fun setColorBgModeAndReset() {
        bgMode = "color"
        prefs.edit {
            putString(KEY_BG_MODE, "color")
            putString(KEY_BG_IMAGE_URI, null)
        }
        bgImageUri = null
    }

    // default palette (can be extended)
    val colorPresets = listOf("#000000", "#FFFFFF", "#0D47A1", "#4CAF50", "#FF5722", "#607D8B")

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }
    fun saveString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        // make the settings content vertically scrollable
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Formatting section
            Section("Formatting") {
                SubHeading("Time format")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use 24-hour clock")
                    Switch(checked = showSeconds, onCheckedChange = {
                        showSeconds = it; saveBoolean(KEY_24_HOUR, it)
                    })
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show Seconds")
                    Switch(checked = is24Hour, onCheckedChange = {
                        is24Hour = it; saveBoolean(KEY_SHOW_SECONDS, it)
                    })
                }

                SubHeading("Date")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show date")
                    Switch(checked = showDate, onCheckedChange = {
                        showDate = it; saveBoolean(KEY_SHOW_DATE, it)
                    })
                }
            }

            // Appearance section
            Section("Appearance") {
                SubHeading("Background")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = (bgMode == "color"), onClick = {
                        setColorBgModeAndReset()
                    })
                    Text("Color", modifier = Modifier.padding(top = 12.dp).clickable {
                        setColorBgModeAndReset()
                    })

                    RadioButton(selected = (bgMode == "image"), onClick = {
                        bgMode = "image"
                        saveString(KEY_BG_MODE, "image")
                    })
                    Text("Image", modifier = Modifier.padding(top = 12.dp).clickable {
                        bgMode = "image"
                        saveString(KEY_BG_MODE, "image")
                    })
                }

                if (bgMode == "color") {
                    ColorPicker(
                        colors = colorPresets,
                        selected = bgColor,
                        onSelect = { hex ->
                            bgColor = hex
                            saveString(KEY_BG_COLOR, hex)
                        }
                    )
                } else {
                    // Show button to select image if none selected
                    if (bgImageUri.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = { imagePickerLaunchers.requestImageSelection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Background Image")
                        }
                    } else {
                        // Show small preview thumbnail that can be tapped to reselect
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { imagePickerLaunchers.requestImageSelection() }
                            ) {
                                Image(

                                    painter = rememberAsyncImagePainter((bgImageUri?.toUri())),
                                    contentDescription = "Background preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Optional: Add overlay text hint
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Text(
                                        "Tap to change",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                SubHeading("Text color")
                ColorPicker(
                    colors = colorPresets,
                    selected = textColor,
                    onSelect = { hex ->
                        textColor = hex
                        saveString(KEY_TEXT_COLOR, hex)
                    }
                )
            }

            // Features section (placeholder for feature toggles / extras)
            Section("Features") {
                SubHeading("Alarm")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show next alarm")
                    Switch(checked = showAlarm, onCheckedChange = {
                        showAlarm = it
                        saveBoolean(KEY_SHOW_ALARM, it)
                    })
                }

                SubHeading("Now Playing")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show media info")
                    Switch(checked = showMedia, onCheckedChange = {
                        showMedia = it
                        saveBoolean(KEY_SHOW_MEDIA, it)
                    })
                }
            }
        }
    }
}
