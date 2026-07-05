package com.example.panichelper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.panichelper.R
import com.example.panichelper.data.RoutineProgress

/**
 * Abschluss-Bildschirm für jede zeitlich begrenzte Routine-Sitzung im
 * täglichen Übungsmodus (Atemübung, PMR, künftige Routinen): zeigt die
 * aktuelle Streak und einen Button zurück zur Übersicht. Routinenneutral,
 * damit jede neue Routine-Sitzung sie unverändert wiederverwenden kann.
 */
@Composable
fun RoutineCompletionContent(
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

/**
 * Blendet [RoutineCompletionContent] als Overlay über dem zuletzt sichtbaren
 * Übungsinhalt ein, statt ihn abrupt zu ersetzen - der Hintergrund bleibt
 * eingefroren sichtbar und wird von einem abdunkelnden Scrim überblendet,
 * während Scrim und Inhalt gemeinsam sanft einblenden und leicht wachsen.
 * Der Übergang soll sich weich anfühlen statt hart abzuschneiden, gerade weil
 * man beim Abschluss einer Übung ohnehin schon etwas zur Ruhe kommt.
 */
@Composable
fun RoutineCompletionOverlay(
    visible: Boolean,
    progress: RoutineProgress?,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center
        ) {
            RoutineCompletionContent(
                progress = progress,
                onFinish = onFinish,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
