package com.example.panichelper.ui.screens.help

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Verwaltet das Speichern und Laden der Telefonnummer der Vertrauensperson.
 * Nutzt AndroidViewModel, um Zugriff auf den Application-Context für
 * den PreferencesManager (DataStore) zu erhalten.
 */
class HelpViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    // Aktueller Wert des Eingabefelds (kann sich vom gespeicherten Wert
    // unterscheiden, solange der Nutzer noch nicht auf "Speichern" geklickt hat).
    var phoneNumberInput by mutableStateOf("")
        private set

    // Zeigt an, ob zuletzt erfolgreich gespeichert wurde (für kurzes Feedback in der UI).
    var isSaved by mutableStateOf(false)
        private set

    init {
        // Beim Start: gespeicherte Nummer laden und als Eingabewert vorbelegen.
        preferencesManager.trustedPhoneNumber
            .onEach { savedNumber ->
                if (savedNumber.isNotEmpty()) {
                    phoneNumberInput = savedNumber
                }
            }
            .launchIn(viewModelScope)
    }

    /** Wird bei jeder Texteingabe im Telefonnummer-Feld aufgerufen. */
    fun onPhoneNumberChange(newValue: String) {
        phoneNumberInput = newValue
        isSaved = false
    }

    /** Speichert die aktuell eingegebene Telefonnummer dauerhaft via DataStore. */
    fun savePhoneNumber() {
        viewModelScope.launch {
            preferencesManager.saveTrustedPhoneNumber(phoneNumberInput)
            isSaved = true
        }
    }
}