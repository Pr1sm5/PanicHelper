package com.example.panichelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rein visuelle Komponente: zeichnet einen Kreis mit gegebener Größe und Farbe.
 * Die eigentliche Animation (Größe/Farbe über Zeit) wird im BreathingScreen
 * mittels Compose Animation APIs (animateDpAsState / animateColorAsState)
 * gesteuert und hier nur als aktueller Wert übergeben.
 *
 * @param size Aktueller Durchmesser des Kreises (animierter Wert).
 * @param color Aktuelle Farbe des Kreises (animierter Wert).
 * @param modifier Optionaler Modifier.
 */
@Composable
fun AnimatedBreathingCircle(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Standardgrößen für den Atem-Kreis, zentral definiert,
 * damit BreathingScreen und ViewModel dieselben Werte referenzieren können.
 */
object BreathingCircleDefaults {
    val MinSize: Dp = 160.dp
    val MaxSize: Dp = 260.dp
}