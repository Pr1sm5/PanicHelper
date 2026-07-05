package com.example.panichelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Kleiner runder Avatar mit der Profilfarbe und dem ersten Buchstaben des Namens.
 */
@Composable
fun ProfileAvatar(
    username: String,
    colorArgb: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(Color(colorArgb), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.trim().take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}
