package com.micahsoftdotexe.dreamingofclocks.uicomponents.fontpicker

import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

val fontOptions: List<Pair<String, String>> = listOf(
    "sans-serif" to "Default",
    "serif" to "Serif",
    "monospace" to "Monospace",
    "sans-serif-light" to "Light",
    "sans-serif-thin" to "Thin",
    "sans-serif-medium" to "Medium",
    "sans-serif-condensed" to "Condensed",
    "casual" to "Casual",
    "dseg7" to "7-Segment"
)

fun fontFamilyForName(name: String): FontFamily = when (name) {
    "serif" -> FontFamily.Serif
    "monospace" -> FontFamily.Monospace
    "cursive" -> FontFamily.Cursive
    else -> FontFamily(Typeface.create(name, Typeface.NORMAL))
}

@Composable
fun FontPicker(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val dseg7FontFamily = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/DSEG7Classic-Regular.ttf"))
    }

    Column {
        fontOptions.forEach { (family, label) ->
            val fontFamily = if (family == "dseg7") dseg7FontFamily else fontFamilyForName(family)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(family) }
            ) {
                RadioButton(selected = (selected == family), onClick = { onSelect(family) })
                Text(text = label, fontFamily = fontFamily)
            }
        }
    }
}
