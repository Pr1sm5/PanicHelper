package com.example.panichelper.ui.screens.help

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.data.ContactRelationship
import com.example.panichelper.data.TrustedContact
import com.example.panichelper.util.PreferencesManager
import java.util.UUID
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Verwaltet das Speichern und Laden der Vertrauenspersonen.
 * Nutzt AndroidViewModel, um Zugriff auf den Application-Context für
 * den PreferencesManager (DataStore) zu erhalten.
 */
class HelpViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    // Aktuell gespeicherte Vertrauenspersonen, aus DataStore geladen.
    var contacts by mutableStateOf<List<TrustedContact>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            preferencesManager.migrateLegacyPhoneNumberIfNeeded()
        }
        preferencesManager.trustedContacts
            .onEach { loadedContacts -> contacts = loadedContacts }
            .launchIn(viewModelScope)
    }

    /** Fügt eine neu ausgewählte Vertrauensperson zur Liste hinzu und speichert sie. */
    fun addContact(name: String, phoneNumber: String, relationship: ContactRelationship) {
        val newContact = TrustedContact(
            id = UUID.randomUUID().toString(),
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship
        )
        persist(contacts + newContact)
    }

    /** Ändert die Beziehungsart einer bereits gespeicherten Vertrauensperson. */
    fun updateContactRelationship(id: String, relationship: ContactRelationship) {
        persist(contacts.map { contact ->
            if (contact.id == id) contact.copy(relationship = relationship) else contact
        })
    }

    /** Entfernt eine gespeicherte Vertrauensperson. */
    fun removeContact(id: String) {
        persist(contacts.filterNot { it.id == id })
    }

    private fun persist(updatedContacts: List<TrustedContact>) {
        contacts = updatedContacts
        viewModelScope.launch {
            preferencesManager.saveTrustedContacts(updatedContacts)
        }
    }
}
