package com.micahsoftdotexe.dreamingofclocks.activities.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.micahsoftdotexe.dreamingofclocks.activities.Section
import com.micahsoftdotexe.dreamingofclocks.activities.SubHeading
import com.micahsoftdotexe.dreamingofclocks.models.ClockTemplate
import com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker.ColorPicker

@Composable
fun ClockTypeSection(
    clockMode: String,
    onClockModeChange: (String) -> Unit,
    analogTemplate: String,
    onAnalogTemplateChange: (String) -> Unit,
    analogHandColor: String,
    onAnalogHandColorChange: (String) -> Unit,
    builtInTemplates: List<ClockTemplate>,
    colorPresets: List<String>,
) {
    Section("Clock Type") {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(selected = (clockMode == "digital"), onClick = {
                onClockModeChange("digital")
            })
            Text("Digital", modifier = Modifier.clickable {
                onClockModeChange("digital")
            })

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(selected = (clockMode == "analog"), onClick = {
                onClockModeChange("analog")
            })
            Text("Analog", modifier = Modifier.clickable {
                onClockModeChange("analog")
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
                            onAnalogTemplateChange(template.name)
                        }
                ) {
                    RadioButton(
                        selected = (analogTemplate == template.name),
                        onClick = {
                            onAnalogTemplateChange(template.name)
                        }
                    )
                    Text(template.name)
                }
            }
            SubHeading("Hand color")
            ColorPicker(
                colors = colorPresets,
                selected = analogHandColor,
                onSelect = onAnalogHandColorChange
            )
        }
    }
}
