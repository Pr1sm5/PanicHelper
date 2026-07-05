package com.example.panichelper.ui.screens.dailyexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.R
import com.example.panichelper.data.DailyExerciseDuration
import com.example.panichelper.ui.screens.breathing.BreathingTechnique
import com.example.panichelper.ui.screens.breathing.BreathingTechniques
import com.example.panichelper.ui.components.HeaderCard
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.components.ProfileAvatar
import com.example.panichelper.ui.components.ProfileColorPicker
import com.example.panichelper.ui.components.RoutineCard
import com.example.panichelper.ui.theme.ProfileColorPalette

/**
 * Übersicht für den täglichen Übungsmodus.
 * Zeigt beim ersten Aufruf ein kurzes Profil-Setup (Name + Farbe), danach
 * eine Begrüßung, die aktuelle Streak und eine Liste von Routine-Karten
 * (aktuell nur Atemübung, künftig weitere Stress-Trainings-Routinen) -
 * jede Karte kapselt ihre eigene Konfiguration und einen Start-Button,
 * damit neue Routinen sich einfach als weitere Karte ergänzen lassen.
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param onStartSession Callback zum Start einer Atemübungs-Sitzung (Länge in Minuten, Technik-ID).
 * @param viewModel Verwaltet Profil, Streak und die für die Atemübung gewählten Optionen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExerciseScreen(
    onNavigateBack: () -> Unit,
    onStartSession: (Int, String) -> Unit,
    viewModel: DailyExerciseViewModel = viewModel()
) {
    val profile = viewModel.profile
    var isEditingProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_daily_exercise)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            HeaderCard(
                title = stringResource(R.string.daily_exercise_header_title),
                subtitle = stringResource(R.string.daily_exercise_header_subtitle)
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
                            text = stringResource(R.string.daily_exercise_greeting, profile.username),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(
                            onClick = { isEditingProfile = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(stringResource(R.string.daily_exercise_edit_profile))
                        }
                    }
                }

                val streak = viewModel.routineProgress
                HeaderCard(
                    title = if (streak.currentStreak > 0) {
                        stringResource(R.string.daily_exercise_streak_active, streak.currentStreak)
                    } else {
                        stringResource(R.string.daily_exercise_streak_empty)
                    },
                    subtitle = stringResource(R.string.daily_exercise_streak_record, streak.longestStreak),
                    modifier = Modifier.padding(top = 20.dp)
                )

                Text(
                    text = stringResource(R.string.daily_exercise_routines_heading),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 12.dp)
                )

                // Aktuell die einzige Routine. Weitere Stress-Trainings-Routinen
                // (z. B. progressive Muskelentspannung, Grounding) kommen später
                // als weitere RoutineCard hier in der Liste dazu.
                BreathingRoutineCard(
                    selectedDuration = viewModel.selectedDuration,
                    selectedTechnique = viewModel.selectedTechnique,
                    onSelectDuration = viewModel::selectDuration,
                    onSelectTechnique = viewModel::selectTechnique,
                    onStart = {
                        onStartSession(viewModel.selectedDuration.minutes, viewModel.selectedTechnique.id)
                    }
                )
            }
        }
    }
}

/**
 * Routine-Karte für die Atemübung: Längen- und Technik-Auswahl plus Start-Button,
 * gekapselt in der einheitlichen [RoutineCard].
 */
@Composable
private fun BreathingRoutineCard(
    selectedDuration: DailyExerciseDuration,
    selectedTechnique: BreathingTechnique,
    onSelectDuration: (DailyExerciseDuration) -> Unit,
    onSelectTechnique: (BreathingTechnique) -> Unit,
    onStart: () -> Unit
) {
    RoutineCard(
        icon = Icons.Filled.Air,
        title = stringResource(R.string.nav_breathing),
        description = stringResource(R.string.daily_exercise_breathing_description)
    ) {
        Text(
            text = stringResource(R.string.daily_exercise_choose_length),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DailyExerciseDuration.entries.forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { onSelectDuration(duration) },
                    label = { Text(stringResource(R.string.duration_minutes_label, duration.minutes)) }
                )
            }
        }

        Text(
            text = stringResource(R.string.daily_exercise_choose_technique),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BreathingTechniques.ALL.forEach { technique ->
                FilterChip(
                    selected = selectedTechnique == technique,
                    onClick = { onSelectTechnique(technique) },
                    label = { Text(stringResource(technique.nameRes)) }
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.daily_exercise_start_button),
            onClick = onStart,
            modifier = Modifier.padding(top = 20.dp)
        )
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
            label = { Text(stringResource(R.string.daily_exercise_name_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Text(
            text = stringResource(R.string.daily_exercise_choose_color),
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
            text = stringResource(R.string.daily_exercise_setup_confirm),
            onClick = { if (username.isNotBlank()) onSave(username.trim(), colorArgb) }
        )
    }
}
