package com.example.panichelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.panichelper.ui.theme.EnsoCardBorder

/**
 * Großer, gut sichtbarer Primär-Button für die gesamte App.
 * Wird u.a. für die Hauptfunktionen auf dem HomeScreen verwendet.
 * Bewusst groß gehalten (Touch-Ziel + Lesbarkeit), da Feinmotorik und
 * Konzentration während einer Panikattacke eingeschränkt sein können.
 *
 * @param text Beschriftung des Buttons.
 * @param onClick Aktion bei Klick.
 * @param icon Optionales Icon, das links vom Text angezeigt wird.
 * @param modifier Optionaler Modifier für äußere Anpassungen (z. B. Breite).
 * @param containerColor Hintergrundfarbe, überschreibbar für Buttons mit eigener Farbkodierung (z. B. Hilfe).
 * @param contentColor Textfarbe, passend zu [containerColor].
 * @param iconTint Icon-Farbe, passend zu [containerColor].
 * @param borderColor Randfarbe, passend zu [containerColor].
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = EnsoCardBorder
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(30.dp)
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
