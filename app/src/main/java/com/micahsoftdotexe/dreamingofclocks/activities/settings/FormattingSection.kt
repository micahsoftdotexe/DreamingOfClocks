package com.micahsoftdotexe.dreamingofclocks.activities.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.micahsoftdotexe.dreamingofclocks.activities.Section
import com.micahsoftdotexe.dreamingofclocks.activities.SubHeading
import com.micahsoftdotexe.dreamingofclocks.uicomponents.fontpicker.FontPicker

@Composable
fun FormattingSection(
    clockMode: String,
    is24Hour: Boolean, onIs24HourChange: (Boolean) -> Unit,
    showSeconds: Boolean, onShowSecondsChange: (Boolean) -> Unit,
    showDate: Boolean, onShowDateChange: (Boolean) -> Unit,
    clockFont: String, onClockFontChange: (String) -> Unit,
    featureFont: String, onFeatureFontChange: (String) -> Unit,
) {
    Section("Formatting") {
        if (clockMode == "digital") {
            SubHeading("Time format")
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use 24-hour clock")
                Switch(checked = is24Hour, onCheckedChange = onIs24HourChange)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Seconds")
                Switch(checked = showSeconds, onCheckedChange = onShowSecondsChange)
            }

            SubHeading("Clock font")
            FontPicker(selected = clockFont, onSelect = onClockFontChange)

            SubHeading("Feature text font")
            FontPicker(selected = featureFont, onSelect = onFeatureFontChange)
        }

        SubHeading("Date")
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show date")
            Switch(checked = showDate, onCheckedChange = onShowDateChange)
        }
    }
}
