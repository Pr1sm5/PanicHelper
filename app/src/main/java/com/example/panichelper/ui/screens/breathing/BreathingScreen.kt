package com.example.panichelper.ui.screens.breathing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.example.panichelper.R
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.components.KeepScreenOn
import com.example.panichelper.ui.components.RoutineCompletionOverlay
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight
import com.example.panichelper.util.PreferencesManager
import com.example.panichelper.util.formatRemainingTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    onNavigateBack: () -> Unit,
    viewModel: BreathingViewModel = viewModel()
) {
    val phase = viewModel.currentPhase
    val technique = viewModel.selectedTechnique
    val completedCycles = viewModel.completedCycles
    val vibrationIntensity = viewModel.vibrationIntensity
    val isCountingDown = viewModel.isCountingDown
    val remainingMillis = viewModel.remainingMillis
    val isCompleted = viewModel.isCompleted
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val audioEnabled by preferencesManager.audioEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesManager.hapticsEnabled.collectAsState(initial = true)
    val soundPlayer = remember { SoundPlayer(context, BreathingSoundCategory.entries) { audioEnabled } }

    KeepScreenOn()

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(Unit) {
        soundPlayer.play(BreathingSoundCategory.START)
    }

    // Reagiert live auf den Audio-Schalter und das Sitzungsende, statt die Musik
    // nur beim Start zu prüfen - sonst läuft eine bereits gestartete Hintergrundmusik
    // nach dem Ausschalten bzw. nach Ablauf der drei Minuten weiter.
    LaunchedEffect(audioEnabled, isCompleted) {
        if (audioEnabled && !isCompleted) {
            soundPlayer.startBackgroundMusic(R.raw.background_music)
        } else {
            soundPlayer.stopBackgroundMusic()
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            soundPlayer.play(BreathingSoundCategory.END)
        }
    }

    DisposableEffect(phase, vibrationIntensity, isCountingDown, hapticsEnabled, isCompleted) {
        if (!isCountingDown && !isCompleted && hapticsEnabled) {
            triggerBreathingHaptic(context, phase.visualState, phase.durationMillis, vibrationIntensity)
        }
        onDispose { cancelBreathingHaptic(context) }
    }

    // Eigener Effekt (ohne vibrationIntensity als Key), damit das Verschieben
    // des Vibrations-Reglers keinen erneuten Sprach-Hinweis auslöst.
    LaunchedEffect(phase, isCountingDown, isCompleted) {
        if (!isCountingDown && !isCompleted) {
            soundPlayer.playForBreathingPhase(phase)
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
        label = "breathingCircleSize"
    )

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "breathingCircleColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_breathing)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
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
                text = if (!isCountingDown) formatRemainingTime(remainingMillis) else " ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = if (!isCountingDown && completedCycles > 0) {
                    stringResource(R.string.breathing_round_counter, completedCycles)
                } else {
                    " "
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreathingTechniques.ALL.forEach { t ->
                    FilterChip(
                        selected = technique == t,
                        onClick = { viewModel.setTechnique(t) },
                        label = { Text(stringResource(t.nameRes)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.breathing_vibration_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = vibrationIntensity,
                    onValueChange = { viewModel.updateVibrationIntensity(it) },
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        RoutineCompletionOverlay(
            visible = isCompleted,
            progress = null,
            onFinish = onNavigateBack
        )
        }
    }
}

// Größer als die Material-Standardwerte (48dp Button/24dp Icon), damit die
// Bedienelemente während einer Übung - wenn man ggf. gestresst ist - auf den
// ersten Blick gut erkennbar und leicht zu treffen sind.
private val ACTION_BUTTON_SIZE = 52.dp
private val ACTION_ICON_SIZE = 28.dp
