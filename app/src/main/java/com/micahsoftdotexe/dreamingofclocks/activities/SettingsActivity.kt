package com.micahsoftdotexe.dreamingofclocks.activities

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.micahsoftdotexe.dreamingofclocks.activities.settings.AppearanceSection
import com.micahsoftdotexe.dreamingofclocks.activities.settings.ClockTypeSection
import com.micahsoftdotexe.dreamingofclocks.activities.settings.FeaturesSection
import com.micahsoftdotexe.dreamingofclocks.activities.settings.FormattingSection
import com.micahsoftdotexe.dreamingofclocks.weather.FetchResult
import com.micahsoftdotexe.dreamingofclocks.weather.GeocodingResult
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherApiClient
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherCache
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherUpdateScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_LAT
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_LON
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.KEY_WEATHER_USE_GPS
import com.micahsoftdotexe.dreamingofclocks.services.screensaver.PreferencesManager.PREFS_NAME
import com.micahsoftdotexe.dreamingofclocks.services.template.TemplateManager
import com.micahsoftdotexe.dreamingofclocks.utils.rememberImagePickerLaunchers

// helper composables for grouping
@Composable
internal fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
@Composable
internal fun SubHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 6.dp))
}


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
    var locationError by remember { mutableStateOf<String?>(null) }
    var locationSearchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var locationSearchJob by remember { mutableStateOf<Job?>(null) }

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
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClockTypeSection(
                clockMode = clockMode,
                onClockModeChange = { clockMode = it; saveString(KEY_CLOCK_MODE, it) },
                analogTemplate = analogTemplate,
                onAnalogTemplateChange = { analogTemplate = it; saveString(KEY_ANALOG_TEMPLATE, it) },
                analogHandColor = analogHandColor,
                onAnalogHandColorChange = { analogHandColor = it; saveString(KEY_ANALOG_HAND_COLOR, it) },
                builtInTemplates = builtInTemplates,
                colorPresets = colorPresets,
            )

            FormattingSection(
                clockMode = clockMode,
                is24Hour = is24Hour, onIs24HourChange = { is24Hour = it; saveBoolean(KEY_24_HOUR, it) },
                showSeconds = showSeconds, onShowSecondsChange = { showSeconds = it; saveBoolean(KEY_SHOW_SECONDS, it) },
                showDate = showDate, onShowDateChange = { showDate = it; saveBoolean(KEY_SHOW_DATE, it) },
                clockFont = clockFont, onClockFontChange = { clockFont = it; saveString(KEY_CLOCK_FONT, it) },
                featureFont = featureFont, onFeatureFontChange = { featureFont = it; saveString(KEY_FEATURE_FONT, it) },
            )

            AppearanceSection(
                bgMode = bgMode, onBgModeChange = { bgMode = it; saveString(KEY_BG_MODE, it) },
                bgColor = bgColor, onBgColorChange = { bgColor = it; saveString(KEY_BG_COLOR, it) },
                bgImageUri = bgImageUri, onBgImageUriChange = { bgImageUri = it },
                textColor = textColor, onTextColorChange = { textColor = it; saveString(KEY_TEXT_COLOR, it) },
                weatherUseGps = weatherUseGps,
                onWeatherUseGpsToggle = { enabled ->
                    if (enabled) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else {
                        weatherUseGps = false
                        prefs.edit { putBoolean(KEY_WEATHER_USE_GPS, false) }
                    }
                },
                weatherLocation = weatherLocation,
                onLocationQueryChange = { query ->
                    weatherLocation = query
                    locationError = null
                    prefs.edit { putString(KEY_WEATHER_LOCATION, query) }
                    // Clear stored coords since user is typing a new query
                    prefs.edit {
                        remove(KEY_WEATHER_LAT)
                        remove(KEY_WEATHER_LON)
                    }
                    // Debounced search
                    locationSearchJob?.cancel()
                    if (query.length >= 2) {
                        locationSearchJob = coroutineScope.launch {
                            delay(300L)
                            isSearchingLocation = true
                            locationSearchResults = WeatherApiClient.geocodeSearch(query)
                            isSearchingLocation = false
                        }
                    } else {
                        locationSearchResults = emptyList()
                    }
                },
                locationError = locationError,
                locationSearchResults = locationSearchResults,
                onLocationSelected = { result ->
                    weatherLocation = result.displayName
                    locationError = null
                    locationSearchResults = emptyList()
                    locationSearchJob?.cancel()
                    prefs.edit {
                        putString(KEY_WEATHER_LOCATION, result.displayName)
                        putFloat(KEY_WEATHER_LAT, result.latitude.toFloat())
                        putFloat(KEY_WEATHER_LON, result.longitude.toFloat())
                    }
                    WeatherCache.saveLocation(context, result.displayName, result.latitude, result.longitude)
                },
                isSearchingLocation = isSearchingLocation,
                weatherUpdateFreq = weatherUpdateFreq,
                onWeatherUpdateFreqChange = { weatherUpdateFreq = it; prefs.edit { putLong(KEY_WEATHER_UPDATE_FREQ, it) } },
                onFetchWeatherNow = {
                    WeatherUpdateScheduler.fetchNow(context, coroutineScope) { result ->
                        val message = when (result) {
                            is FetchResult.Success ->
                                "Weather: ${result.data.condition.name}, ${result.data.temperature}°"
                            is FetchResult.NoLocation -> "No location configured"
                            is FetchResult.LocationNotFound -> "Location not found: ${result.query}"
                            is FetchResult.FetchFailed -> "Weather fetch failed"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                onSelectImage = { imagePickerLaunchers.requestImageSelection() },
                onResetToColorMode = { setColorBgModeAndReset() },
                colorPresets = colorPresets,
            )

            FeaturesSection(
                showAlarm = showAlarm, onShowAlarmChange = { showAlarm = it; saveBoolean(KEY_SHOW_ALARM, it) },
                showMedia = showMedia, onShowMediaChange = { showMedia = it; saveBoolean(KEY_SHOW_MEDIA, it) },
            )
        }
    }
}
