package com.micahsoftdotexe.dreamingofclocks.utils

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

private const val ASSET_DSEG7 = "fonts/DSEG7Classic-Regular.ttf"
private const val ASSET_DSEG14 = "fonts/DSEG14Classic-Regular.ttf"

val fontOptions: List<Pair<String, String>> = listOf(
    "sans-serif" to "Default",
    "serif" to "Serif",
    "monospace" to "Monospace",
    "sans-serif-light" to "Light",
    "sans-serif-thin" to "Thin",
    "sans-serif-medium" to "Medium",
    "sans-serif-condensed" to "Condensed",
    "casual" to "Casual",
    "dseg7" to "7-Segment",
    "dseg14" to "14-Segment"
)

fun resolveTypeface(context: Context, fontFamily: String): Typeface = when (fontFamily) {
    "dseg7" -> Typeface.createFromAsset(context.assets, ASSET_DSEG7)
    "dseg14" -> Typeface.createFromAsset(context.assets, ASSET_DSEG14)
    else -> Typeface.create(fontFamily, Typeface.NORMAL)
}

fun fontFamilyForName(name: String, context: Context): FontFamily = when (name) {
    "serif" -> FontFamily.Serif
    "monospace" -> FontFamily.Monospace
    "cursive" -> FontFamily.Cursive
    "dseg7" -> FontFamily(Typeface.createFromAsset(context.assets, ASSET_DSEG7))
    "dseg14" -> FontFamily(Typeface.createFromAsset(context.assets, ASSET_DSEG14))
    else -> FontFamily(Typeface.create(name, Typeface.NORMAL))
}
