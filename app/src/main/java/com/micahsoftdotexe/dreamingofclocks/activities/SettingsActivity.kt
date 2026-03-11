package com.micahsoftdotexe.dreamingofclocks.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.micahsoftdotexe.dreamingofclocks.uicomponents.fontpicker.FontPicker
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherUpdateScheduler
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_24_HOUR
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_ANALOG_HAND_COLOR
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_ANALOG_TEMPLATE
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_BG_COLOR
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_BG_IMAGE_URI
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_BG_MODE
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_CLOCK_FONT
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_CLOCK_MODE
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_FEATURE_FONT
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_SHOW_ALARM
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_SHOW_DATE
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_SHOW_MEDIA
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_SHOW_SECONDS
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_TEXT_COLOR
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_LOCATION
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_UPDATE_FREQ
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_USE_GPS
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.PREFS_NAME
import com.micahsoftdotexe.dreamingofclocks.services.template.TemplateManager
import com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker.ColorPicker
import com.micahsoftdotexe.dreamingofclocks.utils.MediaNotificationListener
import com.micahsoftdotexe.dreamingofclocks.utils.rememberImagePickerLaunchers

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SettingsActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    var is24Hour by remember { mutableStateOf(prefs.getBoolean(KEY_24_HOUR, false)) }
    var showSeconds by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SECONDS, false)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DATE, true)) }
    var showAlarm by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_ALARM, true)) }
    var showMedia by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_MEDIA, false)) }
    var bgMode by remember { mutableStateOf(prefs.getString(KEY_BG_MODE, "color") ?: "color") }
    var bgColor by remember { mutableStateOf(prefs.getString(KEY_BG_COLOR, "#000000") ?: "#000000") }
    var bgImageUri by remember { mutableStateOf(prefs.getString(KEY_BG_IMAGE_URI, null)) }
    var textColor by remember { mutableStateOf(prefs.getString(KEY_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF") }
    var clockMode by remember { mutableStateOf(prefs.getString(KEY_CLOCK_MODE, "digital") ?: "digital") }
    var analogTemplate by remember { mutableStateOf(prefs.getString(KEY_ANALOG_TEMPLATE, "Classic") ?: "Classic") }
    var analogHandColor by remember { mutableStateOf(prefs.getString(KEY_ANALOG_HAND_COLOR, "#FFFFFF") ?: "#FFFFFF") }
    var clockFont by remember { mutableStateOf(prefs.getString(KEY_CLOCK_FONT, "sans-serif") ?: "sans-serif") }
    var featureFont by remember { mutableStateOf(prefs.getString(KEY_FEATURE_FONT, "sans-serif") ?: "sans-serif") }
    var weatherLocation by remember { mutableStateOf(prefs.getString(KEY_WEATHER_LOCATION, "") ?: "") }
    var weatherUpdateFreq by remember { mutableStateOf(prefs.getLong(KEY_WEATHER_UPDATE_FREQ, 1_800_000L)) }
    var weatherUseGps by remember { mutableStateOf(prefs.getBoolean(KEY_WEATHER_USE_GPS, false)) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            weatherUseGps = true
            prefs.edit { putBoolean(KEY_WEATHER_USE_GPS, true) }
        } else {
            weatherUseGps = false
            prefs.edit { putBoolean(KEY_WEATHER_USE_GPS, false) }
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

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

    val builtInTemplates = remember { TemplateManager.getBuiltInTemplates() }

    Surface(modifier = modifier.fillMaxSize()) {
        // make the settings content vertically scrollable
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clock Type section
            Section("Clock Type") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = (clockMode == "digital"), onClick = {
                        clockMode = "digital"; saveString(KEY_CLOCK_MODE, "digital")
                    })
                    Text("Digital", modifier = Modifier.clickable {
                        clockMode = "digital"; saveString(KEY_CLOCK_MODE, "digital")
                    })

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(selected = (clockMode == "analog"), onClick = {
                        clockMode = "analog"; saveString(KEY_CLOCK_MODE, "analog")
                    })
                    Text("Analog", modifier = Modifier.clickable {
                        clockMode = "analog"; saveString(KEY_CLOCK_MODE, "analog")
                    })
                }

                if (clockMode == "analog") {
                    SubHeading("Template")
                    builtInTemplates.forEach { template ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    analogTemplate = template.name
                                    saveString(KEY_ANALOG_TEMPLATE, template.name)
                                }
                        ) {
                            RadioButton(
                                selected = (analogTemplate == template.name),
                                onClick = {
                                    analogTemplate = template.name
                                    saveString(KEY_ANALOG_TEMPLATE, template.name)
                                }
                            )
                            Text(template.name)
                        }
                    }
                    SubHeading("Hand color")
                    ColorPicker(
                        colors = colorPresets,
                        selected = analogHandColor,
                        onSelect = { hex ->
                            analogHandColor = hex
                            saveString(KEY_ANALOG_HAND_COLOR, hex)
                        }
                    )
                }
            }

            // Formatting section (digital-specific settings + shared date toggle)
            Section("Formatting") {
                if (clockMode == "digital") {
                    SubHeading("Time format")
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

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Seconds")
                        Switch(checked = showSeconds, onCheckedChange = {
                            showSeconds = it; saveBoolean(KEY_SHOW_SECONDS, it)
                        })
                    }

                    SubHeading("Clock font")
                    FontPicker(selected = clockFont, onSelect = {
                        clockFont = it; saveString(KEY_CLOCK_FONT, it)
                    })

                    SubHeading("Feature text font")
                    FontPicker(selected = featureFont, onSelect = {
                        featureFont = it; saveString(KEY_FEATURE_FONT, it)
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

                    RadioButton(selected = (bgMode == "weather"), onClick = {
                        bgMode = "weather"
                        saveString(KEY_BG_MODE, "weather")
                    })
                    Text("Weather", modifier = Modifier.padding(top = 12.dp).clickable {
                        bgMode = "weather"
                        saveString(KEY_BG_MODE, "weather")
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
                } else if (bgMode == "weather") {
                    // GPS toggle
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use device location")
                        Switch(checked = weatherUseGps, onCheckedChange = { enabled ->
                            if (enabled) {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            } else {
                                weatherUseGps = false
                                prefs.edit { putBoolean(KEY_WEATHER_USE_GPS, false) }
                            }
                        })
                    }

                    // City/zip text field (disabled when GPS enabled)
                    OutlinedTextField(
                        value = weatherLocation,
                        onValueChange = { value ->
                            weatherLocation = value
                            prefs.edit { putString(KEY_WEATHER_LOCATION, value) }
                        },
                        label = { Text("City name or zip code") },
                        enabled = !weatherUseGps,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    // Update frequency selector
                    SubHeading("Update frequency")
                    val freqOptions = listOf(
                        "15 minutes" to 900_000L,
                        "30 minutes" to 1_800_000L,
                        "1 hour" to 3_600_000L,
                        "3 hours" to 10_800_000L
                    )
                    var freqExpanded by remember { mutableStateOf(false) }
                    val selectedFreqLabel = freqOptions.find { it.second == weatherUpdateFreq }?.first ?: "30 minutes"
                    ExposedDropdownMenuBox(
                        expanded = freqExpanded,
                        onExpandedChange = { freqExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFreqLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = freqExpanded,
                            onDismissRequest = { freqExpanded = false }
                        ) {
                            freqOptions.forEach { (label, value) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        weatherUpdateFreq = value
                                        prefs.edit { putLong(KEY_WEATHER_UPDATE_FREQ, value) }
                                        freqExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fetch Now button
                    OutlinedButton(
                        onClick = {
                            WeatherUpdateScheduler.fetchNow(context, coroutineScope) { data ->
                                if (data != null) {
                                    Toast.makeText(
                                        context,
                                        "Weather: ${data.condition.name}, ${data.temperature}°",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "Weather fetch failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Now")
                    }
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
                    Switch(checked = showMedia, onCheckedChange = { enabled ->
                        if (enabled && !isNotificationListenerEnabled(context)) {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            )
                        } else {
                            showMedia = enabled
                            saveBoolean(KEY_SHOW_MEDIA, enabled)
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
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val componentName = ComponentName(context, MediaNotificationListener::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(componentName.flattenToString()) == true
}
