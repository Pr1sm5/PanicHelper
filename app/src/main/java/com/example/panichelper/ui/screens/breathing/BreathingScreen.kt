package com.example.panichelper.ui.screens.breathing

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.ui.components.AnimatedBreathingCircle
import com.example.panichelper.ui.components.BreathingCircleDefaults
import com.example.panichelper.ui.theme.BreathingDark
import com.example.panichelper.ui.theme.BreathingLight

/**
 * Screen für die geführte Atemübung.
 * Zeigt einen animierten Kreis, der im 4-4-6-Sekunden-Rhythmus
 * wächst (Einatmen), stillsteht (Halten) und schrumpft (Ausatmen).
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param viewModel Steuert den zeitlichen Ablauf der Übung.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    onNavigateBack: () -> Unit,
    viewModel: BreathingViewModel = viewModel()
) {
    val phase = viewModel.currentPhase

    // Zielgröße des Kreises abhängig von der aktuellen Phase:
    // Einatmen -> wächst, Halten -> bleibt groß, Ausatmen -> schrumpft.
    val targetSize = when (phase) {
        BreathingPhase.INHALE -> BreathingCircleDefaults.MaxSize
        BreathingPhase.HOLD -> BreathingCircleDefaults.MaxSize
        BreathingPhase.EXHALE -> BreathingCircleDefaults.MinSize
    }

    // Zielfarbe des Kreises: heller beim Einatmen, dunkler beim Ausatmen.
    val targetColor = when (phase) {
        BreathingPhase.INHALE -> BreathingLight
        BreathingPhase.HOLD -> BreathingLight
        BreathingPhase.EXHALE -> BreathingDark
    }

    // Animationsdauer entspricht jeweils der Phasendauer, damit die
    // Bewegung exakt synchron zum Text-Wechsel verläuft.
    val animationDurationMillis = phase.durationMillis.toInt()

    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = LinearEasing
        ),
        label = "breathingCircleSize"
    )

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = LinearEasing
        ),
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Folge dem Kreis mit deinem Atem",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Box mit fixer Höhe, damit sich umliegende Elemente nicht
            // verschieben, während der Kreis seine Größe ändert.
            Box(
                modifier = Modifier.fillMaxSize().weight(1f, fill = false),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBreathingCircle(
                    size = animatedSize,
                    color = animatedColor
                )
            }

            Text(
                text = phase.label,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 48.dp)
            )
        }
    }
}