package com.example.panichelper.ui.screens.dailyexercise

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.panichelper.R
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.screens.breathing.BreathingVisualState
import com.example.panichelper.ui.screens.breathing.cancelBreathingHaptic
import com.example.panichelper.ui.screens.breathing.triggerBreathingHaptic
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight

/**
 * Zeitlich begrenzte, geführte Atemsitzung des täglichen Übungsmodus.
 * Nutzt dieselbe Kreis-Animation wie der freie Atemübung-Screen, läuft aber
 * automatisch nach [minutes] Minuten aus und trägt den Abschluss in die
 * Routine-Statistik ein.
 *
 * @param minutes Länge der Sitzung in Minuten (aus dem Navigations-Argument).
 * @param onFinish Callback zum Zurücknavigieren zur Übersicht (Abbruch oder Abschluss).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExerciseSessionScreen(
    minutes: Int,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: DailyExerciseSessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DailyExerciseSessionViewModel(application, minutes * 60_000L)
            }
        }
    )

    val phase = viewModel.currentPhase
    val isCompleted = viewModel.isCompleted
    val isCountingDown = viewModel.isCountingDown

    DisposableEffect(phase, isCompleted, isCountingDown) {
        if (!isCompleted && !isCountingDown) {
            triggerBreathingHaptic(context, phase.visualState, phase.durationMillis, 0.7f)
        }
        onDispose { cancelBreathingHaptic(context) }
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
