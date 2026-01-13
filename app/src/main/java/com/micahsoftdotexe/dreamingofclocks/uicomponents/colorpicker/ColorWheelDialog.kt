package com.micahsoftdotexe.dreamingofclocks.uicomponents.colorpicker

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ColorWheelDialog(
initialHex: String,
onDismiss: () -> Unit,
onSelect: (String) -> Unit
) {
val hsvInit = FloatArray(3)
try {
    AndroidColor.colorToHSV(initialHex.toColorInt(), hsvInit)
} catch (e: Exception) {
    AndroidColor.colorToHSV(AndroidColor.WHITE, hsvInit)
}

var hue by remember { mutableFloatStateOf(hsvInit[0]) }
var sat by remember { mutableFloatStateOf(hsvInit[1]) }
var value by remember { mutableFloatStateOf(hsvInit[2]) }

val previewColorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat.coerceIn(0f, 1f), value.coerceIn(0f, 1f)))
val previewHex = String.format("#%06X", 0xFFFFFF and previewColorInt)

AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select custom color") },
    text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wheel takes remaining space and stays square
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp)
                ) {
                    ColorWheel(
                        modifier = Modifier.fillMaxSize(),
                        hue = hue,
                        sat = sat,
                        onChange = { h, s ->
                            hue = h
                            sat = s
                        }
                    )
                }

                // Preview stays compact beside the wheel (no slider here)
                Column(
                    modifier = Modifier
                        .widthIn(min = 88.dp, max = 140.dp)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(previewColorInt), shape = CircleShape)
                            .border(1.dp, Color.LightGray, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(previewHex)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slider placed below both the wheel and preview (full width)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Value")
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    },
    confirmButton = {
        TextButton(onClick = { onSelect(previewHex) }) { Text("OK") }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    }
)
}

@Composable
private fun ColorWheel(
modifier: Modifier = Modifier,
hue: Float,
sat: Float,
onChange: (hue: Float, sat: Float) -> Unit
) {
val hueColors = remember {
    val steps = 24
    (0..steps).map { i ->
        val h = i * (360f / steps)
        Color(AndroidColor.HSVToColor(floatArrayOf(h, 1f, 1f)))
    }
}

Canvas(
    modifier = modifier
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val center = this.size.center
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                val r = min(center.x, center.y)
                val dist = hypot(dx, dy)
                if (dist <= r) {
                    val angle = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))).toFloat() + 360f) % 360f
                    val s = (dist / r).coerceIn(0f, 1f)
                    onChange(angle, s)
                }
            }
        }
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val offset = change.position
                val center = this.size.center
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                val r = min(center.x, center.y)
                val dist = hypot(dx, dy)
                if (dist <= r) {
                    val angle = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))).toFloat() + 360f) % 360f
                    val s = (dist / r).coerceIn(0f, 1f)
                    onChange(angle, s)
                }
            }
        }
) {
    val radius = min(size.width, size.height) / 2f
    val center = this.center

    drawCircle(
        brush = Brush.sweepGradient(hueColors),
        radius = radius,
        center = center
    )

    drawIntoCanvas { _ ->
        val radial = Brush.radialGradient(
            colors = listOf(Color.White, Color.Transparent),
            center = center,
            radius = radius
        )
        drawCircle(brush = radial, radius = radius, center = center)
    }

    val angleRad = Math.toRadians(hue.toDouble())
    val indicatorRadius = sat * radius
    val ix = center.x + (indicatorRadius * cos(angleRad)).toFloat()
    val iy = center.y + (indicatorRadius * sin(angleRad)).toFloat()

    drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 10f, center = Offset(ix, iy))
    drawCircle(color = Color.White, radius = 7f, center = Offset(ix, iy))
}
}