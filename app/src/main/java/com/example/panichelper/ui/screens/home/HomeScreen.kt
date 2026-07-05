package com.example.panichelper.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panichelper.R
import com.example.panichelper.ui.components.PrimaryButton
import com.example.panichelper.ui.theme.EnsoCardBorder

/**
 * Startbildschirm der App.
 * Zeigt eine kurze Begrüßung und vier große Buttons für die Hauptfunktionen:
 * Atemübung, Ablenkung, Hilfe, Tägliche Übung.
 *
 * @param onNavigateToBreathing Callback zur Navigation zum BreathingScreen.
 * @param onNavigateToDistraction Callback zur Navigation zum DistractionScreen.
 * @param onNavigateToHelp Callback zur Navigation zum HelpScreen.
 * @param onNavigateToDailyExercise Callback zur Navigation zum täglichen Übungsmodus.
 * @param onNavigateToSettings Callback zur Navigation zu den Einstellungen (u. a. Sprache).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBreathing: () -> Unit,
    onNavigateToDistraction: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDailyExercise: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
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
        }
    ) { innerPadding ->
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HomeHeroCard()

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = stringResource(R.string.home_breathing_button),
                icon = Icons.Filled.Air,
                onClick = onNavigateToBreathing,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.home_distraction_button),
                icon = Icons.Filled.Pets,
                onClick = onNavigateToDistraction,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.home_help_button),
                icon = Icons.Filled.Phone,
                onClick = onNavigateToHelp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.home_daily_exercise_button),
                icon = Icons.Filled.EmojiEvents,
                onClick = onNavigateToDailyExercise
            )
        }
    }
}

@Composable
private fun HomeHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, EnsoCardBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnsoMark(modifier = Modifier.size(184.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 36.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp)
            )

            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun EnsoMark(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.outline
    val text = MaterialTheme.colorScheme.onBackground

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            val softStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            val padding = 9.dp.toPx()
            val diameter = size.minDimension - padding * 2
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

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
            drawArc(
                color = muted,
                startAngle = 128f,
                sweepAngle = 245f,
                useCenter = false,
                topLeft = Offset(topLeft.x + 19.dp.toPx(), topLeft.y + 19.dp.toPx()),
                size = Size(diameter - 38.dp.toPx(), diameter - 38.dp.toPx()),
                style = softStroke,
                alpha = 0.65f
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_enso_kanji),
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 4.sp),
                color = primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal
                ),
                color = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
