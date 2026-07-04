package com.example.panichelper.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.panichelper.ui.components.PrimaryButton

/**
 * Startbildschirm der App.
 * Zeigt eine kurze Begrüßung und drei große Buttons für die Hauptfunktionen:
 * Atemübung, Ablenkung, Hilfe.
 *
 * @param onNavigateToBreathing Callback zur Navigation zum BreathingScreen.
 * @param onNavigateToDistraction Callback zur Navigation zum DistractionScreen.
 * @param onNavigateToHelp Callback zur Navigation zum HelpScreen.
 */
@Composable
fun HomeScreen(
    onNavigateToBreathing: () -> Unit,
    onNavigateToDistraction: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PanicHelper",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Du bist nicht allein. Wähle, was dir jetzt helfen könnte.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
            )

            PrimaryButton(
                text = "Atemübung",
                icon = Icons.Filled.Air,
                onClick = onNavigateToBreathing,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = "Ablenkung",
                icon = Icons.Filled.Pets,
                onClick = onNavigateToDistraction,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = "Hilfe",
                icon = Icons.Filled.Phone,
                onClick = onNavigateToHelp
            )
        }
    }
}