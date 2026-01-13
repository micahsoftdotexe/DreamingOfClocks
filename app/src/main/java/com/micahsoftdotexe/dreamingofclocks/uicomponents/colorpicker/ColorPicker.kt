package com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

@Composable
fun ColorPicker(colors: List<String>, selected: String, onSelect: (String) -> Unit) {
    var showCustom by remember { mutableStateOf(false) }
    var customHex by remember { mutableStateOf("#FFFFFF") }
    val defaultColorSelected : Boolean = colors.any { it.equals(selected, ignoreCase = true) }
    if (!defaultColorSelected){
        customHex = selected
    }

    if (showCustom) {
        @Suppress("AssignedValueIsNeverRead")
        ColorWheelDialog(
            initialHex = customHex,
            onDismiss = {
                showCustom = false
            },
            onSelect = { hex ->
                customHex = hex
                onSelect(hex)
                showCustom = false
            }
        )
    }


    // horizontal scroll added so swatches can scroll on small screens
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        val customColor = try { Color(customHex.toColorInt()) } catch (_: Exception) { Color.White }
        val customBorder = if (!defaultColorSelected) MaterialTheme.colorScheme.primary else Color.LightGray

    // pick a contrasting color for the plus symbol based on luminance
        val plusColor = if (customColor.luminance() > 0.5f) Color.Black else Color.White
        @Suppress("AssignedValueIsNeverRead")
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(customColor, shape = CircleShape)
                .border(width = if (!defaultColorSelected) 3.dp else 1.dp, color = customBorder, shape = CircleShape)
                .clickable { showCustom = true }
        ) {
            Text("+", color = plusColor)
        }

        colors.forEach { hex ->
            val compColor = try { Color(hex.toColorInt()) } catch (_: Exception) { Color.Black }
            val borderColor = if (selected.equals(hex, ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.LightGray
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(compColor, shape = CircleShape)
                    .border(width = if (selected.equals(hex, ignoreCase = true)) 3.dp else 1.dp, color = borderColor, shape = CircleShape)
                    .clickable { onSelect(hex) }
            )
        }
    }
}

// helper: convert Color (Compose) / int to hex string
//private fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)