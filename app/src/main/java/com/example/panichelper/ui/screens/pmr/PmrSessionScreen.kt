package com.example.panichelper.ui.screens.pmr

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.panichelper.R
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.components.KeepScreenOn
import com.example.panichelper.ui.components.RoutineCompletionOverlay
import com.example.panichelper.ui.screens.breathing.BreathingSoundCategory
import com.example.panichelper.ui.screens.breathing.playEndSequence
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight
import com.example.panichelper.util.PreferencesManager
import com.example.panichelper.util.formatRemainingTime
import kotlinx.coroutines.launch

/**
 * Zeitlich begrenzte, geführte Sitzung der progressiven Muskelentspannung.
 * Nutzt dieselbe Kreis-Animation wie die Atemübung (klein = anspannen,
 * groß = loslassen), läuft aber automatisch nach [minutes] Minuten aus und
 * trägt den Abschluss in dieselbe Routine-Statistik ein.
 *
 * Nutzt PMR-eigene Sprach-Hinweise (siehe [PmrSoundCategory]): eine Einleitung
 * vor dem Start sowie je eine Anspannen-/Loslassen-Zeile pro Muskelgruppe. Nur
 * der Ende-Ton stammt weiterhin aus dem allgemeinen Atem-Sound-Set. Ein
 * kurzer, ruhiger Vibrations-Tick markiert zusätzlich jeden Phasenwechsel.
 *
 * @param minutes Länge der Sitzung in Minuten (aus dem Navigations-Argument).
 * @param onFinish Callback zum Zurücknavigieren zur Übersicht (Abbruch oder Abschluss).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PmrSessionScreen(
    minutes: Int,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val haptics = LocalHapticFeedback.current
    val viewModel: PmrSessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                PmrSessionViewModel(application, minutes * 60_000L)
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
    val soundPlayer = remember {
        SoundPlayer(
            context,
            listOf(BreathingSoundCategory.END_1, BreathingSoundCategory.END_2) + PmrSoundCategory.entries
        ) { audioEnabled }
    }

    KeepScreenOn()

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(Unit) {
        soundPlayer.play(PmrSoundCategory.START)
    }

    LaunchedEffect(audioEnabled) {
        if (audioEnabled) {
            soundPlayer.startBackgroundMusic(R.raw.background_music)
        } else {
            soundPlayer.stopBackgroundMusic()
        }
    }

    // Ein kleiner, ruhiger Vibrations-Tick markiert jeden Phasenwechsel,
    // zusätzlich zur passenden Anspannen-/Loslassen-Sprachzeile.
    LaunchedEffect(phase, isCountingDown, isCompleted) {
        if (!isCompleted && !isCountingDown) {
            if (hapticsEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            soundPlayer.playForPmrPhase(phase)
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            soundPlayer.playEndSequence()
        }
    }

    val handleFinish: () -> Unit = {
        coroutineScope.launch {
            soundPlayer.fadeOutBackgroundMusic()
            onFinish()
        }
    }

    val targetSize = if (isCountingDown) {
        BreathingCircleDefaults.MinSize
    } else {
        when (phase.phaseState) {
            PmrPhaseState.TENSE -> BreathingCircleDefaults.MinSize
            PmrPhaseState.RELEASE -> BreathingCircleDefaults.MaxSize
        }
    }

    val targetColor = if (isCountingDown) {
        BreathingLight
    } else {
        when (phase.phaseState) {
            PmrPhaseState.TENSE -> BreathingDark
            PmrPhaseState.RELEASE -> BreathingLight
        }
    }

    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "pmrCircleSize"
    )

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "pmrCircleColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_pmr)) },
                navigationIcon = {
                    IconButton(
                        onClick = handleFinish,
                        modifier = Modifier.size(ACTION_BUTTON_SIZE)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.size(ACTION_ICON_SIZE)
                        )
                    }
                },
                actions = {
                    val mutedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    IconButton(
                        onClick = {
                            coroutineScope.launch { preferencesManager.setAudioEnabled(!audioEnabled) }
                        },
                        modifier = Modifier.size(ACTION_BUTTON_SIZE)
                    ) {
                        Icon(
                            imageVector = if (audioEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = stringResource(R.string.settings_audio_label),
                            tint = if (audioEnabled) MaterialTheme.colorScheme.onBackground else mutedTint,
                            modifier = Modifier.size(ACTION_ICON_SIZE)
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch { preferencesManager.setHapticsEnabled(!hapticsEnabled) }
                        },
                        modifier = Modifier.size(ACTION_BUTTON_SIZE)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Vibration,
                            contentDescription = stringResource(R.string.settings_haptics_label),
                            tint = if (hapticsEnabled) MaterialTheme.colorScheme.onBackground else mutedTint,
                            modifier = Modifier.size(ACTION_ICON_SIZE)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pmr_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

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
                        val groupName = stringResource(phase.muscleGroup.nameRes)
                        when (phase.phaseState) {
                            PmrPhaseState.TENSE -> stringResource(R.string.pmr_tense_instruction, groupName)
                            PmrPhaseState.RELEASE -> stringResource(R.string.pmr_release_instruction, groupName)
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isCountingDown) " " else formatRemainingTime(viewModel.remainingMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }

            RoutineCompletionOverlay(
                visible = isCompleted,
                progress = viewModel.updatedProgress,
                onFinish = handleFinish
            )
        }
    }
}

// Größer als die Material-Standardwerte (48dp Button/24dp Icon), damit die
// Bedienelemente während einer Übung - wenn man ggf. gestresst ist - auf den
// ersten Blick gut erkennbar und leicht zu treffen sind.
private val ACTION_BUTTON_SIZE = 52.dp
private val ACTION_ICON_SIZE = 28.dp
