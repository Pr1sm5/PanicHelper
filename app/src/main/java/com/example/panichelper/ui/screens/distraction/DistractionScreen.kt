package com.example.panichelper.ui.screens.distraction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
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
import androidx.compose.ui.graphics.Color
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
import com.example.panichelper.R
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.data.DistractionContent
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.launch

/**
 * Screen für die Ablenkungsfunktion.
 * Zeigt abwechselnd:
 * - ein lokal gespeichertes Bild mit einer zufälligen Beobachtungsfrage,
 * - eine Farbsuch-Übung (Grounding), oder
 * - einen großen Text-Prompt ohne Bild (z. B. Geruch/Geräusche).
 *
 * Wechsel zwischen den Inhalten sind sanft animiert (Fade + Scale) und
 * werden durch ein farbiges Icon-Badge sowie Fortschritts-Punkte begleitet,
 * damit der Screen trotz Einfachheit lebendig wirkt.
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param viewModel Verwaltet den aktuell angezeigten Inhalt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DistractionViewModel = viewModel()
) {
    val content = viewModel.currentContent
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val audioEnabled by preferencesManager.audioEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesManager.hapticsEnabled.collectAsState(initial = true)
    val soundPlayer = remember { SoundPlayer(context, DistractionSoundCategory.entries) { audioEnabled } }

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    // Reagiert auf jeden neuen Inhalt (Bild oder Text-Prompt): spielt den
    // passenden Sprach-Hinweis und gibt eine kleine, ruhige Vibration -
    // spürbar statt aufdringlich, damit man auch ohne hinzusehen merkt,
    // dass sich etwas geändert hat.
    LaunchedEffect(content) {
        soundPlayer.playForDistractionContent(content)
        if (hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_distraction)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Icon-Badge: zeigt je nach Inhaltstyp ein anderes Icon in einer
            // andersfarbigen, abgerundeten Kreisfläche - gibt sofort einen
            // visuellen Hinweis darauf, um welche Art von Übung es geht.
            ContentTypeBadge(content = content)

            Spacer(modifier = Modifier.height(20.dp))

            // Fester Rahmen für den wechselnden Inhalt: Bild+Frage und reiner
            // Text-Prompt sind unterschiedlich hoch, würden also Badge/Dots/
            // Button nach oben oder unten verschieben, wenn dieser Bereich
            // einfach nur so groß wäre wie sein jeweiliger Inhalt. Eine feste
            // Höhe hält den Rest des Screens an Ort und Stelle.
            //
            // AnimatedContent sorgt für einen sanften Fade+Scale-Übergang,
            // statt dass der Inhalt hart wechselt. Der Index wird als Teil
            // des Keys mitgegeben, damit auch bei "zufällig auf denselben
            // Inhalt" zuverlässig neu animiert wird.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DISTRACTION_CONTENT_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = viewModel.currentIndex to content,
                    transitionSpec = {
                        (fadeIn(tween(350)) + scaleIn(initialScale = 0.92f, animationSpec = tween(350)))
                            .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)))
                    },
                    label = "distractionContentTransition"
                ) { (_, animatedContent) ->
                    DistractionContentBody(animatedContent = animatedContent)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Fortschritts-Punkte: kleine Dots, die zeigen, an welcher
            // Position man sich innerhalb der Rotation befindet.
            ProgressDots(
                total = viewModel.totalCount,
                currentIndex = viewModel.currentIndex
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.distraction_next_button),
                    icon = Icons.Filled.SkipNext,
                    onClick = { viewModel.showNextContent() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Feste Höhe des wechselnden Inhaltsbereichs (Bild + Frage oder Text-Prompt),
// groß genug für das größere Bild plus eine zweizeilige Frage darunter.
private val DISTRACTION_CONTENT_HEIGHT = 420.dp

/**
 * Zeigt ein kreisförmiges Icon-Badge, dessen Farbe und Symbol vom
 * aktuellen Inhaltstyp abhängen. Rein dekorativ/informativ.
 */
@Composable
private fun ContentTypeBadge(content: DistractionContent) {
    val (icon, containerColor, contentColor) = when (content) {
        is DistractionContent.ImageContent -> Triple(
            Icons.Filled.Pets,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        is DistractionContent.TextContent -> Triple(
            Icons.Filled.SelfImprovement,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Zeigt den eigentlichen Inhalt (Bild+Frage, Farbübung oder Text-Prompt),
 * abhängig vom Typ. Wird innerhalb von AnimatedContent aufgerufen.
 */
@Composable
private fun DistractionContentBody(animatedContent: DistractionContent) {
    when (animatedContent) {
        is DistractionContent.ImageContent -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = animatedContent.imageRes),
                        contentDescription = stringResource(R.string.distraction_image_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                }

                val questions = stringArrayResource(R.array.distraction_image_questions)
                Text(
                    text = questions[animatedContent.questionIndex],
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                )
            }
        }

        is DistractionContent.TextContent -> {
            val prompts = stringArrayResource(R.array.distraction_fixed_prompts)
            Text(
                text = prompts[animatedContent.promptIndex],
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

/**
 * Kleine Punktreihe, die die aktuelle Position innerhalb der Rotation
 * anzeigt (ähnlich einem Karussell-Indikator). Der aktive Punkt ist
 * größer und in der Primärfarbe eingefärbt, inaktive Punkte sind dezent
 * grau, um nicht abzulenken.
 */
@Composable
private fun ProgressDots(total: Int, currentIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive = index == currentIndex
            val dotColor: Color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val dotSize = if (isActive) 10.dp else 7.dp

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}