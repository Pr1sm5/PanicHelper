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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight

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
    val context = LocalContext.current

    DisposableEffect(phase, vibrationIntensity, isCountingDown) {
        if (!isCountingDown) {
            triggerBreathingHaptic(context, phase.visualState, phase.durationMillis, vibrationIntensity)
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
                title = { Text("Atemübung") },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Folge dem Kreis mit deinem Atem",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBreathingCircle(size = animatedSize, color = animatedColor)
                if (isCountingDown) {
                    Text(
                        text = viewModel.countdownValue.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Text(
                text = if (isCountingDown) "Gleich geht's los …" else phase.label,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (!isCountingDown && completedCycles > 0) "Runde $completedCycles" else " ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreathingTechniques.ALL.forEach { t ->
                    FilterChip(
                        selected = technique == t,
                        onClick = { viewModel.setTechnique(t) },
                        label = { Text(t.name) }
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
                    text = "Vibration",
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
    }
}
