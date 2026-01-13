package com.micahsoftdotexe.dreamingofclocks.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker.ColorPicker
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher


// --- New: Settings UI and persistence ---
private const val PREFS_NAME = "clock_prefs"
private const val KEY_24_HOUR = "pref_24_hour"
private const val KEY_SHOW_DATE = "pref_show_date"
private const val KEY_BG_MODE = "pref_bg_mode" // "color" or "image"
private const val KEY_BG_COLOR = "pref_bg_color" // hex string like #000000
private const val KEY_BG_IMAGE_URI = "pref_bg_image_uri" // uri string
private const val KEY_TEXT_COLOR = "pref_text_color" // hex string

@Composable
@Preview
fun SettingsActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var is24Hour by remember { mutableStateOf(prefs.getBoolean(KEY_24_HOUR, true)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DATE, true)) }
    var bgMode by remember { mutableStateOf(prefs.getString(KEY_BG_MODE, "color") ?: "color") }
    var bgColor by remember { mutableStateOf(prefs.getString(KEY_BG_COLOR, "#000000") ?: "#000000") }
    var bgImageUri by remember { mutableStateOf(prefs.getString(KEY_BG_IMAGE_URI, null)) }
    var textColor by remember { mutableStateOf(prefs.getString(KEY_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF") }
    var showGallery by remember { mutableStateOf(false) }
    var imageBackgroundSelectionUi by remember { mutableStateOf(false) }

    // default palette (can be extended)
    val colorPresets = listOf("#000000", "#FFFFFF", "#0D47A1", "#4CAF50", "#FF5722", "#607D8B")

    if (showGallery) {
        @Suppress("AssignedValueIsNeverRead")
        GalleryPickerLauncher(
            onPhotosSelected = { photos ->
                if (photos.isNotEmpty()) {
                    val uri = photos[0].uri
                    prefs.edit { putString(KEY_BG_IMAGE_URI, uri) }
                    prefs.edit { putString(KEY_BG_MODE, "image") }
                    bgImageUri = uri
                }
                showGallery = false


            },
            onError = {
                showGallery = false
                //display error message
                if (context is Activity) {
                    // display toast message
                    context.runOnUiThread {
                        android.widget.Toast.makeText(context, "Error picking image: $it", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = {
                showGallery = false
            }

        )
    }

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
            // 24h toggle
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use 24-hour clock")
                Switch(checked = is24Hour, onCheckedChange = {
                    is24Hour = it; saveBoolean(KEY_24_HOUR, it)
                })
            }

            HorizontalDivider()

            // show date
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

            HorizontalDivider()

            // Background mode
            Text("Background")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                RadioButton(selected = (bgMode == "color"), onClick = {
                    bgMode = "color"; saveString(KEY_BG_MODE, "color")
                })
                Text("Color", modifier = Modifier.padding(top = 12.dp).clickable {
                    bgMode = "color"; saveString(KEY_BG_MODE, "color")
                })
                RadioButton(selected = (bgMode == "image"), onClick = {
                     bgMode = "image"
//                    showGallery = true
//                    pickImageLauncher.launch(arrayOf("image/*"))
                })
                Text("Image", modifier = Modifier.padding(top = 12.dp).clickable {
                    bgMode = "image"
//                    pickImageLauncher.launch(arrayOf("image/*"))
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
            } else if (bgMode == "image" || imageBackgroundSelectionUi) {
                Text("Selected image: ${bgImageUri ?: "none"}")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { showGallery = true }) {
                        Text("Choose image")
                    }
                    Button(onClick = {
                        bgImageUri = null
                        saveString(KEY_BG_IMAGE_URI, "")
                    }) {
                        Text("Clear image")
                    }
                }

            }

            HorizontalDivider()

            // Text color presets -> REPLACED with ColorPicker
            Text("Text color")
            ColorPicker(
                colors = colorPresets,
                selected = textColor,
                onSelect = { hex ->
                    textColor = hex
                    saveString(KEY_TEXT_COLOR, hex)
                }
            )
        }
    }
}

