package com.example.panichelper.ui.screens.dailyexercise

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.panichelper.R
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.screens.breathing.BreathingSoundCategory
import com.example.panichelper.ui.screens.breathing.BreathingTechniques
import com.example.panichelper.ui.screens.breathing.BreathingVisualState
import com.example.panichelper.ui.screens.breathing.cancelBreathingHaptic
import com.example.panichelper.ui.screens.breathing.playForBreathingPhase
import com.example.panichelper.ui.screens.breathing.triggerBreathingHaptic
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.launch

/**
 * Zeitlich begrenzte, geführte Atemsitzung des täglichen Übungsmodus.
 * Nutzt dieselbe Kreis-Animation wie der freie Atemübung-Screen, läuft aber
 * automatisch nach [minutes] Minuten aus und trägt den Abschluss in die
 * Routine-Statistik ein.
 *
 * @param minutes Länge der Sitzung in Minuten (aus dem Navigations-Argument).
 * @param techniqueId ID der gewählten Atemtechnik (aus dem Navigations-Argument).
 * @param onFinish Callback zum Zurücknavigieren zur Übersicht (Abbruch oder Abschluss).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExerciseSessionScreen(
    minutes: Int,
    techniqueId: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val technique = remember(techniqueId) { BreathingTechniques.byId(techniqueId) }
    val viewModel: DailyExerciseSessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DailyExerciseSessionViewModel(application, minutes * 60_000L, technique)
            }
        }
    )

    val phase = viewModel.currentPhase
    val isCompleted = viewModel.isCompleted
    val isCountingDown = viewModel.isCountingDown
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val audioEnabled by preferencesManager.audioEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesManager.hapticsEnabled.collectAsState(initial = true)
    val soundPlayer = remember { SoundPlayer(context, BreathingSoundCategory.entries) { audioEnabled } }

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(Unit) {
        soundPlayer.play(BreathingSoundCategory.START)
    }

    // Reagiert live auf den Audio-Schalter und das Sitzungsende, statt die Musik
    // nur beim Start zu prüfen - sonst läuft eine bereits gestartete
    // Hintergrundmusik nach dem Ausschalten oder nach Abschluss weiter.
    LaunchedEffect(audioEnabled, isCompleted) {
        if (audioEnabled && !isCompleted) {
            soundPlayer.startBackgroundMusic(R.raw.background_music)
        } else {
            soundPlayer.stopBackgroundMusic()
        }
    }

    DisposableEffect(phase, isCompleted, isCountingDown, hapticsEnabled) {
        if (!isCompleted && !isCountingDown && hapticsEnabled) {
            triggerBreathingHaptic(context, phase.visualState, phase.durationMillis, 0.7f)
        }
        onDispose { cancelBreathingHaptic(context) }
    }

    LaunchedEffect(phase, isCountingDown, isCompleted) {
        if (!isCompleted && !isCountingDown) {
            soundPlayer.playForBreathingPhase(phase)
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            soundPlayer.play(BreathingSoundCategory.END)
        }
    }

    val targetSize = if (isCountingDown) {
        BreathingCircleDefaults.MinSize
    } else {
        when (phase.visualState) {
            BreathingVisualState.EXPAND, BreathingVisualState.HOLD_LARGE -> BreathingCircleDefaults.MaxSize
            BreathingVisualState.CONTRACT, BreathingVisualState.HOLD_SMALL -> BreathingCircleDefaults.MinSize
        }
    }

    val targetColor = if (isCountingDown) {
        BreathingLight
    } else {
        when (phase.visualState) {
            BreathingVisualState.EXPAND, BreathingVisualState.HOLD_LARGE -> BreathingLight
            BreathingVisualState.CONTRACT, BreathingVisualState.HOLD_SMALL -> BreathingDark
        }
    }

    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "dailyExerciseCircleSize"
    )

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "dailyExerciseCircleColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_daily_exercise)) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    val mutedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    IconButton(
                        onClick = {
                            coroutineScope.launch { preferencesManager.setAudioEnabled(!audioEnabled) }
                        }
                    ) {
                        Icon(
                            imageVector = if (audioEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = stringResource(R.string.settings_audio_label),
                            tint = if (audioEnabled) MaterialTheme.colorScheme.onBackground else mutedTint
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch { preferencesManager.setHapticsEnabled(!hapticsEnabled) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Vibration,
                            contentDescription = stringResource(R.string.settings_haptics_label),
                            tint = if (hapticsEnabled) MaterialTheme.colorScheme.onBackground else mutedTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (isCompleted) {
            CompletionContent(
                progress = viewModel.updatedProgress,
                onFinish = onFinish,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.breathing_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.breathing_how_it_works),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    technique.phases.forEachIndexed { index, stepPhase ->
                        val isActiveStep = !isCountingDown && index == viewModel.currentPhaseIndex
                        Text(
                            text = stringResource(stepPhase.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isActiveStep) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActiveStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            }
                        )
                        if (index != technique.phases.lastIndex) {
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedBreathingCircle(circleSize = animatedSize, color = animatedColor)
                    if (isCountingDown) {
                        Text(
                            text = viewModel.countdownValue.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Text(
                    text = if (isCountingDown) {
                        stringResource(R.string.breathing_countdown_label)
                    } else {
                        stringResource(phase.labelRes)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (isCountingDown) " " else formatRemaining(viewModel.remainingMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun CompletionContent(
    progress: RoutineProgress?,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.daily_exercise_completed_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        if (progress != null) {
            Text(
                text = stringResource(R.string.daily_exercise_streak_active, progress.currentStreak),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp)
            )
        }

        PrimaryButton(
            text = stringResource(R.string.action_done),
            onClick = onFinish
        )
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutesPart = totalSeconds / 60
    val secondsPart = totalSeconds % 60
    return "%d:%02d".format(minutesPart, secondsPart)
}
