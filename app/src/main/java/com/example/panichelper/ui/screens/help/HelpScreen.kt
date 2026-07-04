package com.example.panichelper.ui.screens.help

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.ui.components.HeaderCard
import com.example.panichelper.ui.components.PrimaryButton

/**
 * Screen für die Hilfefunktion.
 * Ermöglicht das Speichern einer Telefonnummer einer Vertrauensperson
 * sowie das direkte Öffnen des Telefon-Dialers über ACTION_DIAL
 * (keine automatische Anrufauslösung, keine CALL_PHONE-Berechtigung nötig).
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param viewModel Verwaltet das Speichern/Laden der Telefonnummer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: HelpViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilfe") },
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
            verticalArrangement = Arrangement.Top
        ) {
            HeaderCard(
                title = "Wenn du Unterstützung brauchst,",
                subtitle = "kannst du hier eine Vertrauensperson kontaktieren."
            )

            OutlinedTextField(
                value = viewModel.phoneNumberInput,
                onValueChange = viewModel::onPhoneNumberChange,
                label = { Text("Telefonnummer") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 12.dp)
            )

            PrimaryButton(
                text = if (viewModel.isSaved) "Gespeichert ✓" else "Telefonnummer speichern",
                icon = Icons.Filled.Save,
                onClick = { viewModel.savePhoneNumber() },
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Text(
                text = "Der Anruf wird nicht automatisch gestartet – " +
                        "du entscheidest im Telefon-Dialer selbst, ob du anrufst.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            PrimaryButton(
                text = "Jetzt anrufen",
                icon = Icons.Filled.Phone,
                onClick = {
                    val phoneNumber = viewModel.phoneNumberInput
                    if (phoneNumber.isNotBlank()) {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(dialIntent)
                    }
                }
            )
        }
    }
}