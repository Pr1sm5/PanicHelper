package com.example.panichelper.ui.screens.dailyexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.data.DailyExerciseDuration
import com.example.panichelper.ui.components.HeaderCard
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.components.ProfileAvatar
import com.example.panichelper.ui.components.ProfileColorPicker
import com.example.panichelper.ui.theme.ProfileColorPalette

/**
 * Übersicht für den täglichen Übungsmodus.
 * Zeigt beim ersten Aufruf ein kurzes Profil-Setup (Name + Farbe), danach
 * eine Begrüßung, die aktuelle Streak sowie eine Auswahl der Übungslänge.
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param onStartSession Callback zum Start einer Sitzung mit der gewählten Länge (in Minuten).
 * @param viewModel Verwaltet Profil, Streak und gewählte Übungslänge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExerciseScreen(
    onNavigateBack: () -> Unit,
    onStartSession: (Int) -> Unit,
    viewModel: DailyExerciseViewModel = viewModel()
) {
    val profile = viewModel.profile
    var isEditingProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tägliche Übung") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Zurück"
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            HeaderCard(
                title = "Baue dir eine tägliche Routine auf",
                subtitle = "Kurze, geführte Atemübungen – jeden Tag ein bisschen."
            )

            if (profile == null || isEditingProfile) {
                ProfileSetupForm(
                    initialUsername = profile?.username ?: "",
                    initialColorArgb = profile?.colorArgb,
                    onSave = { username, colorArgb ->
                        viewModel.saveProfile(username, colorArgb)
                        isEditingProfile = false
                    },
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(username = profile.username, colorArgb = profile.colorArgb)
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "Hallo, ${profile.username}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(
                            onClick = { isEditingProfile = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Profil bearbeiten")
                        }
                    }
                }

                val streak = viewModel.routineProgress
                HeaderCard(
                    title = if (streak.currentStreak > 0) {
                        "🔥 ${streak.currentStreak} Tage in Folge"
                    } else {
                        "Starte heute deine Streak"
                    },
                    subtitle = "Rekord: ${streak.longestStreak} Tage",
                    modifier = Modifier.padding(top = 24.dp)
                )

                Text(
                    text = "Wähle eine Länge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DailyExerciseDuration.entries.forEach { duration ->
                        FilterChip(
                            selected = viewModel.selectedDuration == duration,
                            onClick = { viewModel.selectDuration(duration) },
                            label = { Text(duration.label) }
                        )
                    }
                }

                PrimaryButton(
                    text = "Übung starten",
                    onClick = { onStartSession(viewModel.selectedDuration.minutes) },
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        }
    }
}

/**
 * Formular zum Anlegen oder Bearbeiten des lokalen Profils (Name + Akzentfarbe).
 */
@Composable
private fun ProfileSetupForm(
    initialUsername: String,
    initialColorArgb: Int?,
    onSave: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf(initialUsername) }
    var colorArgb by remember { mutableStateOf(initialColorArgb ?: ProfileColorPalette.first().toArgb()) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Dein Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Text(
            text = "Wähle eine Farbe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ProfileColorPicker(
            selectedArgb = colorArgb,
            onSelect = { colorArgb = it },
            modifier = Modifier.padding(bottom = 28.dp)
        )

        PrimaryButton(
            text = "Los geht's",
            onClick = { if (username.isNotBlank()) onSave(username.trim(), colorArgb) }
        )
    }
}
