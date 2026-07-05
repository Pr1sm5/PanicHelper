package com.example.panichelper.ui.screens.guidedsession

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.data.DistractionContent
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.components.RoutineCompletionContent
import com.example.panichelper.ui.screens.breathing.BreathingPhaseConfig
import com.example.panichelper.ui.screens.breathing.BreathingSoundCategory
import com.example.panichelper.ui.screens.breathing.BreathingVisualState
import com.example.panichelper.ui.screens.breathing.cancelBreathingHaptic
import com.example.panichelper.ui.screens.breathing.playForBreathingPhase
import com.example.panichelper.ui.screens.breathing.triggerBreathingHaptic
import com.example.panichelper.ui.screens.distraction.DistractionSoundCategory
import com.example.panichelper.ui.screens.distraction.playForDistractionContent
import com.example.panichelper.ui.screens.pmr.PmrPhase
import com.example.panichelper.ui.screens.pmr.PmrPhaseState
import com.example.panichelper.ui.screens.pmr.playForPmrPhase
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight
import com.example.panichelper.util.PreferencesManager
import com.example.panichelper.util.formatRemainingTime
import kotlinx.coroutines.launch

/**
 * Geführter Komplett-Durchlauf: Atemübung, kurze Pause, mehrere
 * Grounding-Übungen und progressive Muskelentspannung direkt nacheinander in
 * einer einzigen, ununterbrochenen Sitzung. Ton, Vibration und die
 * Vibrations-Stärke bleiben während der gesamten Sitzung erreichbar, damit
 * man nur einmal starten muss und danach nicht mehr zwischen Screens
 * wechseln muss.
 *
 * @param onFinish Callback zum Zurücknavigieren zum HomeScreen (Abbruch oder Abschluss).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedSessionScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val haptics = LocalHapticFeedback.current
    val viewModel: GuidedSessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer { GuidedSessionViewModel(application) }
        }
    )

    val stage = viewModel.stage
    val isCountingDown = viewModel.isCountingDown
    val isCompleted = viewModel.isCompleted
    val vibrationIntensity = viewModel.vibrationIntensity
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val audioEnabled by preferencesManager.audioEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesManager.hapticsEnabled.collectAsState(initial = true)

    val soundCategories: List<SoundCategory> = BreathingSoundCategory.entries + DistractionSoundCategory.entries
    val soundPlayer = remember { SoundPlayer(context, soundCategories) { audioEnabled } }

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(Unit) {
        soundPlayer.play(BreathingSoundCategory.START)
    }

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

    // Atemübung: Haptik (mit Vibrations-Stärke) + Sprach-Hinweis pro Phase.
    val breathingPhase = viewModel.breathingPhase
    DisposableEffect(stage, breathingPhase, isCountingDown, hapticsEnabled, vibrationIntensity) {
        if (stage == GuidedSessionStage.BREATHING && !isCountingDown && hapticsEnabled) {
            triggerBreathingHaptic(context, breathingPhase.visualState, breathingPhase.durationMillis, vibrationIntensity)
        }
        onDispose { cancelBreathingHaptic(context) }
    }
    LaunchedEffect(stage, breathingPhase, isCountingDown) {
        if (stage == GuidedSessionStage.BREATHING && !isCountingDown) {
            soundPlayer.playForBreathingPhase(breathingPhase)
        }
    }

    // Grounding: Sprach-Hinweis + kleiner Vibrations-Tick pro neuem Inhalt.
    val distractionContent = viewModel.distractionContent
    LaunchedEffect(stage, distractionContent) {
        if (stage == GuidedSessionStage.DISTRACTION) {
            soundPlayer.playForDistractionContent(distractionContent)
            if (hapticsEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    // PMR: kleiner Vibrations-Tick pro Phasenwechsel + gelegentliche "Constructive"-Zeile.
    val pmrPhase = viewModel.pmrPhase
    LaunchedEffect(stage, pmrPhase) {
        if (stage == GuidedSessionStage.PMR) {
            if (hapticsEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            soundPlayer.playForPmrPhase(pmrPhase)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_guided_session)) },
                navigationIcon = {
                    IconButton(
                        onClick = onFinish,
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
        if (isCompleted) {
            RoutineCompletionContent(
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
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (stage) {
                        GuidedSessionStage.BREATHING -> BreathingStageContent(
                            isCountingDown = isCountingDown,
                            countdownValue = viewModel.countdownValue,
                            phase = breathingPhase,
                            remainingMillis = viewModel.breathingRemainingMillis
                        )

                        GuidedSessionStage.TRANSITION -> TransitionStageContent(
                            remainingSeconds = viewModel.transitionRemainingSeconds
                        )

                        GuidedSessionStage.DISTRACTION -> DistractionStageContent(
                            content = distractionContent,
                            stepIndex = viewModel.distractionStepIndex,
                            totalCount = GuidedSessionViewModel.DISTRACTION_ITEM_COUNT
                        )

                        GuidedSessionStage.PMR -> PmrStageContent(
                            phase = pmrPhase,
                            remainingMillis = viewModel.pmrRemainingMillis
                        )

                        GuidedSessionStage.COMPLETED -> Unit
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
            }
        }
    }
}

@Composable
private fun BreathingStageContent(
    isCountingDown: Boolean,
    countdownValue: Int,
    phase: BreathingPhaseConfig,
    remainingMillis: Long
) {
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
        label = "guidedBreathingSize"
    )
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "guidedBreathingColor"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.nav_breathing),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedBreathingCircle(circleSize = animatedSize, color = animatedColor)
            if (isCountingDown) {
                Text(
                    text = countdownValue.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Text(
            text = if (isCountingDown) stringResource(R.string.breathing_countdown_label) else stringResource(phase.labelRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (isCountingDown) " " else formatRemainingTime(remainingMillis),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun TransitionStageContent(remainingSeconds: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.guided_session_transition_message),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = remainingSeconds.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DistractionStageContent(content: DistractionContent, stepIndex: Int, totalCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.guided_session_distraction_step, stepIndex + 1, totalCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (content) {
            is DistractionContent.ImageContent -> {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = content.imageRes),
                        contentDescription = stringResource(R.string.distraction_image_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
                val questions = stringArrayResource(R.array.distraction_image_questions)
                Text(
                    text = questions[content.questionIndex],
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                )
            }

            is DistractionContent.TextContent -> {
                val prompts = stringArrayResource(R.array.distraction_fixed_prompts)
                Text(
                    text = prompts[content.promptIndex],
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PmrStageContent(phase: PmrPhase, remainingMillis: Long) {
    val targetSize = when (phase.phaseState) {
        PmrPhaseState.TENSE -> BreathingCircleDefaults.MinSize
        PmrPhaseState.RELEASE -> BreathingCircleDefaults.MaxSize
    }
    val targetColor = when (phase.phaseState) {
        PmrPhaseState.TENSE -> BreathingDark
        PmrPhaseState.RELEASE -> BreathingLight
    }
    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "guidedPmrSize"
    )
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = phase.durationMillis.toInt(), easing = LinearEasing),
        label = "guidedPmrColor"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.nav_pmr),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedBreathingCircle(circleSize = animatedSize, color = animatedColor)
        }
        val groupName = stringResource(phase.muscleGroup.nameRes)
        Text(
            text = when (phase.phaseState) {
                PmrPhaseState.TENSE -> stringResource(R.string.pmr_tense_instruction, groupName)
                PmrPhaseState.RELEASE -> stringResource(R.string.pmr_release_instruction, groupName)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = formatRemainingTime(remainingMillis),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
    }
}

// Größer als die Material-Standardwerte (48dp Button/24dp Icon), damit die
// Bedienelemente während einer Übung - wenn man ggf. gestresst ist - auf den
// ersten Blick gut erkennbar und leicht zu treffen sind.
private val ACTION_BUTTON_SIZE = 52.dp
private val ACTION_ICON_SIZE = 28.dp
