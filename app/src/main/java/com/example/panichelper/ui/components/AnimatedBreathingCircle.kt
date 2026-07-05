package com.example.panichelper.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Atem-Kreis im Stil eines japanischen Enso (Tusche-Kreis): ein ruhiger,
 * leicht geöffneter äußerer Ring dreht sich sehr langsam und rahmt zwei mit
 * dem Atem pulsierende Kontur-Ringe sowie den eigentlichen Atem-Kreis in der
 * Mitte. Größe/Farbe des Hauptkreises kommen animiert von außen
 * (BreathingScreen); alles andere ist bewusst zurückhaltende Dekoration –
 * genug Bewegung, um den Blick zu halten, ohne von der Atmung abzulenken.
 *
 * @param circleSize Aktueller Durchmesser des Hauptkreises (animierter Wert).
 * @param color Aktuelle Farbe des Hauptkreises (animierter Wert).
 * @param modifier Optionaler Modifier.
 */
@Composable
fun AnimatedBreathingCircle(
    circleSize: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ensoRotation")
    val ensoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ensoRotationAngle"
    )

    Box(
        modifier = modifier.size(BreathingCircleDefaults.FrameSize),
        contentAlignment = Alignment.Center
    ) {
        // Äußerer Enso-Ring: leicht geöffnetes Tusche-Kreismotiv, dreht sich
        // sehr langsam (90s/Umdrehung) – kaum wahrnehmbar, aber nie ganz still.
        Canvas(modifier = Modifier.size(BreathingCircleDefaults.FrameSize)) {
            rotate(ensoRotation) {
                val strokeWidth = size.minDimension * 0.018f
                val diameter = size.minDimension * 0.94f
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)
                drawArc(
                    color = color.copy(alpha = 0.28f),
                    startAngle = -100f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color.copy(alpha = 0.14f),
                    startAngle = -95f,
                    sweepAngle = 288f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round)
                )
            }
        }

        // Kontur-Ringe: pulsieren mit dem Hauptkreis mit und klingen nach außen aus.
        listOf(
            (circleSize + 36.dp) to 0.20f,
            (circleSize + 72.dp) to 0.10f
        ).forEach { (ringSize, alpha) ->
            Canvas(modifier = Modifier.size(ringSize)) {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Hauptkreis: weicher Farbverlauf statt harter Flächenfarbe.
        Canvas(modifier = Modifier.size(circleSize)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, color.copy(alpha = 0.6f))
                ),
                radius = size.minDimension / 2f
            )
        }
    }
}

/**
 * Standardgrößen für den Atem-Kreis, zentral definiert,
 * damit BreathingScreen und ViewModel dieselben Werte referenzieren können.
 */
object BreathingCircleDefaults {
    val MinSize: Dp = 160.dp
    val MaxSize: Dp = 260.dp

    // Fester Gesamtrahmen für den Enso-Ring, unabhängig von der Atemgröße,
    // damit das Layout nicht "springt" und der äußere Ring immer sichtbar bleibt.
    val FrameSize: Dp = MaxSize + 100.dp
}
