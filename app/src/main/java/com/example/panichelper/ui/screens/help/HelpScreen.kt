package com.example.panichelper.ui.screens.help

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panichelper.R
import com.example.panichelper.data.ContactRelationship
import com.example.panichelper.ui.components.ContactCard
import com.example.panichelper.ui.components.HeaderCard
import com.example.panichelper.ui.components.PrimaryButton

/**
 * Screen für die Hilfefunktion.
 * Ermöglicht das Speichern mehrerer Vertrauenspersonen (Name, Telefonnummer,
 * Beziehungsart), ausgewählt direkt aus den Kontakten des Geräts, sowie das
 * direkte Öffnen des Telefon-Dialers über ACTION_DIAL für jede von ihnen
 * (keine automatische Anrufauslösung, keine CALL_PHONE- oder READ_CONTACTS-
 * Berechtigung nötig).
 *
 * @param onNavigateBack Callback zur Rückkehr zum HomeScreen.
 * @param viewModel Verwaltet das Speichern/Laden der Vertrauenspersonen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: HelpViewModel = viewModel()
) {
    val context = LocalContext.current

    // Frisch aus dem Kontaktpicker übernommene Person, die noch eine Beziehungsart braucht.
    var pendingPickedContact by remember { mutableStateOf<Pair<String, String>?>(null) }
    // ID der Vertrauensperson, deren Beziehungsart gerade bearbeitet wird.
    var editingContactId by remember { mutableStateOf<String?>(null) }

    val pickContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val name = nameIndex.takeIf { it >= 0 }?.let { cursor.getString(it) }
                        val number = numberIndex.takeIf { it >= 0 }?.let { cursor.getString(it) }
                        if (!name.isNullOrBlank() && !number.isNullOrBlank()) {
                            pendingPickedContact = name to number
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_help)) },
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
            verticalArrangement = Arrangement.Top
        ) {
            HeaderCard(
                title = stringResource(R.string.help_header_title),
                subtitle = stringResource(R.string.help_header_subtitle)
            )

            if (viewModel.contacts.isEmpty()) {
                Text(
                    text = stringResource(R.string.help_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.contacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onCall = {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.phoneNumber}")
                            }
                            context.startActivity(dialIntent)
                        },
                        onDelete = { viewModel.removeContact(contact.id) },
                        onEditRelationship = { editingContactId = contact.id }
                    )
                }
            }

            Text(
                text = stringResource(R.string.help_call_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.help_add_contact_button),
                icon = Icons.Filled.PersonAdd,
                onClick = {
                    val pickIntent = Intent(
                        Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    )
                    pickContactLauncher.launch(pickIntent)
                }
            )
        }
    }

    pendingPickedContact?.let { (name, number) ->
        RelationshipPickerDialog(
            initialRelationship = ContactRelationship.FAMILY,
            onDismiss = { pendingPickedContact = null },
            onConfirm = { relationship ->
                viewModel.addContact(name, number, relationship)
                pendingPickedContact = null
            }
        )
    }

    editingContactId?.let { id ->
        val contact = viewModel.contacts.find { it.id == id }
        if (contact != null) {
            RelationshipPickerDialog(
                initialRelationship = contact.relationship,
                onDismiss = { editingContactId = null },
                onConfirm = { relationship ->
                    viewModel.updateContactRelationship(id, relationship)
                    editingContactId = null
                }
            )
        }
    }
}

/**
 * Kleiner Dialog zur Auswahl der Beziehungsart (Familie/Freund:in/Therapeut:in)
 * für eine neue oder bereits gespeicherte Vertrauensperson.
 */
@Composable
private fun RelationshipPickerDialog(
    initialRelationship: ContactRelationship,
    onDismiss: () -> Unit,
    onConfirm: (ContactRelationship) -> Unit
) {
    var selected by remember(initialRelationship) { mutableStateOf(initialRelationship) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.help_relationship_dialog_title)) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContactRelationship.entries.forEach { relationship ->
                    FilterChip(
                        selected = selected == relationship,
                        onClick = { selected = relationship },
                        label = { Text(stringResource(relationship.labelRes)) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
