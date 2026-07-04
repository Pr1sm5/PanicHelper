package com.example.panichelper.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Erweiterungsproperty: erstellt eine einzelne, app-weite DataStore-Instanz.
private val Context.dataStore by preferencesDataStore(name = "panic_helper_prefs")

/**
 * Kapselt den Zugriff auf Jetpack DataStore (Preferences) zur
 * dauerhaften Speicherung der Vertrauensperson-Telefonnummer.
 * Ersetzt klassische SharedPreferences durch die empfohlene,
 * asynchrone DataStore-API.
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val TRUSTED_PHONE_NUMBER_KEY = stringPreferencesKey("trusted_phone_number")
    }

    /**
     * Beobachtbarer Strom der gespeicherten Telefonnummer.
     * Liefert einen leeren String, falls noch keine Nummer gespeichert wurde.
     */
    val trustedPhoneNumber: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TRUSTED_PHONE_NUMBER_KEY] ?: ""
    }

    /**
     * Speichert die übergebene Telefonnummer dauerhaft.
     */
    suspend fun saveTrustedPhoneNumber(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[TRUSTED_PHONE_NUMBER_KEY] = phoneNumber
        }
    }
}