package com.micahsoftdotexe.dreamingofclocks.activities.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.micahsoftdotexe.dreamingofclocks.activities.Section
import com.micahsoftdotexe.dreamingofclocks.activities.SubHeading
import com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker.ColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSection(
    bgMode: String, onBgModeChange: (String) -> Unit,
    bgColor: String, onBgColorChange: (String) -> Unit,
    bgImageUri: String?, onBgImageUriChange: (String?) -> Unit,
    textColor: String, onTextColorChange: (String) -> Unit,
    weatherUseGps: Boolean, onWeatherUseGpsToggle: (Boolean) -> Unit,
    weatherLocation: String, onWeatherLocationChange: (String) -> Unit,
    weatherUpdateFreq: Long, onWeatherUpdateFreqChange: (Long) -> Unit,
    onFetchWeatherNow: () -> Unit,
    onSelectImage: () -> Unit,
    onResetToColorMode: () -> Unit,
    colorPresets: List<String>,
) {
    Section("Appearance") {
        SubHeading("Background")
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(selected = (bgMode == "color"), onClick = {
                onResetToColorMode()
            })
            Text("Color", modifier = Modifier.padding(top = 12.dp).clickable {
                onResetToColorMode()
            })

            RadioButton(selected = (bgMode == "image"), onClick = {
                onBgModeChange("image")
            })
            Text("Image", modifier = Modifier.padding(top = 12.dp).clickable {
                onBgModeChange("image")
            })

            RadioButton(selected = (bgMode == "weather"), onClick = {
                onBgModeChange("weather")
            })
            Text("Weather", modifier = Modifier.padding(top = 12.dp).clickable {
                onBgModeChange("weather")
            })
        }

        if (bgMode == "color") {
            ColorPicker(
                colors = colorPresets,
                selected = bgColor,
                onSelect = onBgColorChange
            )
        } else if (bgMode == "weather") {
            // GPS toggle
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use device location")
                Switch(checked = weatherUseGps, onCheckedChange = onWeatherUseGpsToggle)
            }

            // City/zip text field (disabled when GPS enabled)
            OutlinedTextField(
                value = weatherLocation,
                onValueChange = onWeatherLocationChange,
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
                                onWeatherUpdateFreqChange(value)
                                freqExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fetch Now button
            OutlinedButton(
                onClick = onFetchWeatherNow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Now")
            }
        } else {
            // Show button to select image if none selected
            if (bgImageUri.isNullOrEmpty()) {
                OutlinedButton(
                    onClick = onSelectImage,
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
                            .clickable { onSelectImage() }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(bgImageUri?.toUri()),
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
            onSelect = onTextColorChange
        )
    }
}
