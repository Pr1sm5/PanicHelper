package com.example.panichelper.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.panichelper.R
import com.example.panichelper.ui.components.PrimaryButton

/**
 * Startbildschirm der App.
 * Zeigt eine kurze Begrüßung und vier große Buttons für die Hauptfunktionen:
 * Atemübung, Ablenkung, Hilfe, Tägliche Übung.
 *
 * @param onNavigateToBreathing Callback zur Navigation zum BreathingScreen.
 * @param onNavigateToDistraction Callback zur Navigation zum DistractionScreen.
 * @param onNavigateToHelp Callback zur Navigation zum HelpScreen.
 * @param onNavigateToDailyExercise Callback zur Navigation zum täglichen Übungsmodus.
 * @param onNavigateToSettings Callback zur Navigation zu den Einstellungen (u. a. Sprache).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBreathing: () -> Unit,
    onNavigateToDistraction: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDailyExercise: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.nav_breathing),
                icon = Icons.Filled.Air,
                onClick = onNavigateToBreathing,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.nav_distraction),
                icon = Icons.Filled.Pets,
                onClick = onNavigateToDistraction,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.nav_help),
                icon = Icons.Filled.Phone,
                onClick = onNavigateToHelp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.nav_daily_exercise),
                icon = Icons.Filled.EmojiEvents,
                onClick = onNavigateToDailyExercise
            )
        }
    }
}
