package com.example.panichelper.ui.screens.distraction

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.R
import com.example.panichelper.data.DistractionContent
import com.example.panichelper.ui.components.PrimaryButton
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Screen für die Ablenkungsfunktion.
 * Zeigt abwechselnd:
 * - ein lokal gespeichertes Bild mit einer zufälligen Beobachtungsfrage, oder
 * - einen großen Text-Prompt ohne Bild (Grounding-Übung, z. B. Geruch/Geräusche).
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
            // Je nach Inhaltstyp wird entweder Bild+Frage oder nur ein
            // großer Text angezeigt.
            when (content) {
                is DistractionContent.ImageContent -> {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = content.imageRes),
                            contentDescription = stringResource(R.string.distraction_image_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(240.dp)
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
                            .padding(top = 28.dp, bottom = 40.dp)
                            .fillMaxWidth()
                    )
                }

                is DistractionContent.ColorPromptContent -> {
                    val colors = stringArrayResource(R.array.distraction_grounding_colors)
                    Text(
                        text = stringResource(
                            R.string.distraction_color_prompt_template,
                            colors[content.colorIndex]
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp)
                    )
                }

                is DistractionContent.TextContent -> {
                    // Reine Text-Übung: kein Bild, dafür ein großer,
                    // gut lesbarer Text als zentraler Fokuspunkt.
                    val prompts = stringArrayResource(R.array.distraction_fixed_prompts)
                    Text(
                        text = prompts[content.promptIndex],
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp)
                    )
                }
            }

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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = stringResource(R.string.distraction_random_button),
                    icon = Icons.Filled.Shuffle,
                    onClick = { viewModel.showRandomContent() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}