package com.example.panichelper.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panichelper.R
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.theme.EnsoGuided
import com.example.panichelper.ui.theme.EnsoGuidedContainer
import com.example.panichelper.ui.theme.EnsoHelp
import com.example.panichelper.ui.theme.EnsoHelpContainer
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.launch

/**
 * Startbildschirm der App.
 * Zeigt eine kurze Begrüßung und vier große Buttons für die Hauptfunktionen:
 * Atemübung, Ablenkung, Hilfe, Tägliche Übung.
 *
 * @param onNavigateToBreathing Callback zur Navigation zum BreathingScreen.
 * @param onNavigateToDistraction Callback zur Navigation zum DistractionScreen.
 * @param onNavigateToHelp Callback zur Navigation zum HelpScreen.
 * @param onNavigateToDailyExercise Callback zur Navigation zum täglichen Übungsmodus.
 * @param onNavigateToGuidedSession Callback zur Navigation zum geführten Komplett-Durchlauf.
 * @param onNavigateToSettings Callback zur Navigation zu den Einstellungen (u. a. Sprache).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBreathing: () -> Unit,
    onNavigateToDistraction: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDailyExercise: () -> Unit,
    onNavigateToGuidedSession: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val audioEnabled by preferencesManager.audioEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesManager.hapticsEnabled.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically)
        ) {
            HomeHeroCard()

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.home_breathing_button),
                        icon = Icons.Filled.Air,
                        onClick = onNavigateToBreathing,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = stringResource(R.string.home_distraction_button),
                        icon = Icons.Filled.Pets,
                        onClick = onNavigateToDistraction,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.home_daily_exercise_button),
                        icon = Icons.Filled.EmojiEvents,
                        onClick = onNavigateToDailyExercise,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = stringResource(R.string.home_guided_session_button),
                        icon = Icons.Filled.Spa,
                        onClick = onNavigateToGuidedSession,
                        modifier = Modifier.weight(1f),
                        containerColor = EnsoGuidedContainer,
                        contentColor = EnsoGuided,
                        iconTint = EnsoGuided,
                        borderColor = EnsoGuided.copy(alpha = 0.5f)
                    )
                }

                PrimaryButton(
                    text = stringResource(R.string.home_help_button),
                    icon = Icons.Filled.Phone,
                    onClick = onNavigateToHelp,
                    containerColor = EnsoHelpContainer,
                    contentColor = EnsoHelp,
                    iconTint = EnsoHelp,
                    borderColor = EnsoHelp.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnsoMark(modifier = Modifier.size(300.dp))

        Text(
            text = stringResource(R.string.home_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 18.dp)
        )
    }
}

@Composable
private fun EnsoMark(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.outline
    val text = MaterialTheme.colorScheme.onBackground

    // Dieselbe ruhige, sehr langsame Gegenrotation wie beim Atem-Kreis
    // (AnimatedBreathingCircle) – kaum wahrnehmbar, aber nie ganz still.
    val infiniteTransition = rememberInfiniteTransition(label = "homeEnsoRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "homeEnsoRotationAngle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            val softStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            val padding = 9.dp.toPx()
            val diameter = size.minDimension - padding * 2
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val innerTopLeft = Offset(topLeft.x + 19.dp.toPx(), topLeft.y + 19.dp.toPx())
            val innerDiameter = diameter - 38.dp.toPx()

            rotate(rotation) {
                drawArc(
                    color = primary,
                    startAngle = -22f,
                    sweepAngle = 318f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = stroke,
                    alpha = 0.95f
                )
            }
            rotate(-rotation) {
                drawArc(
                    color = muted,
                    startAngle = 128f,
                    sweepAngle = 245f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = Size(innerDiameter, innerDiameter),
                    style = softStroke,
                    alpha = 0.65f
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_enso_kanji),
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 4.sp, fontSize = 30.sp),
                color = primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 30.sp
                ),
                color = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
