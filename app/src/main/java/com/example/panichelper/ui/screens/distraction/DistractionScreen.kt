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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.data.DistractionRepository
import com.example.panichelper.ui.components.PrimaryButton

/**
 * Screen für die Ablenkungsfunktion.
 * Zeigt ein lokal gespeichertes Bild mit einer zufälligen Beobachtungsfrage
 * darunter. Über zwei Buttons kann zum nächsten bzw. einem zufälligen
 * Bild gewechselt werden.
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param viewModel Verwaltet Bild-Index und aktuelle Frage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DistractionViewModel = viewModel()
) {
    val imageRes = DistractionRepository.getImageResAt(viewModel.currentImageIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ablenkung") },
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
            // Karte mit abgerundeten Ecken um das Bild.
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Ablenkungsbild",
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .padding(8.dp)
                )
            }

            Text(
                text = viewModel.currentQuestion,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 40.dp)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(
                    text = "Nächstes Bild",
                    icon = Icons.Filled.SkipNext,
                    onClick = { viewModel.showNextImage() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = "Zufälliges Bild",
                    icon = Icons.Filled.Shuffle,
                    onClick = { viewModel.showRandomImage() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}