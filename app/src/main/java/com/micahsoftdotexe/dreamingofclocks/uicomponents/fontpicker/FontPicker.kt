package com.micahsoftdotexe.dreamingofclocks.uicomponents.fontpicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.micahsoftdotexe.dreamingofclocks.utils.fontFamilyForName
import com.micahsoftdotexe.dreamingofclocks.utils.fontOptions

@Composable
fun FontPicker(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current

    Column {
        fontOptions.forEach { (family, label) ->
            val fontFamily = fontFamilyForName(family, context)
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
