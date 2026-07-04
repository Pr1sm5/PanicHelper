package com.example.panichelper.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Großer, gut sichtbarer Primär-Button für die gesamte App.
 * Wird u.a. für die drei Hauptfunktionen auf dem HomeScreen verwendet.
 *
 * @param text Beschriftung des Buttons.
 * @param onClick Aktion bei Klick.
 * @param icon Optionales Icon, das links vom Text angezeigt wird.
 * @param modifier Optionaler Modifier für äußere Anpassungen (z. B. Breite).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Große Touch-Fläche für einfache Bedienung im Stress-Zustand
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 12.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}