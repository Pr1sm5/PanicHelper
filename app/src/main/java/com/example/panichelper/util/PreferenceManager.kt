package com.example.panichelper.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.panichelper.R
import com.example.panichelper.data.ContactRelationship
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.data.TrustedContact
import com.example.panichelper.data.UserProfile
import com.example.panichelper.data.withCompletionRecorded
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Erweiterungsproperty: erstellt eine einzelne, app-weite DataStore-Instanz.
private val Context.dataStore by preferencesDataStore(name = "panic_helper_prefs")

/**
 * Kapselt den Zugriff auf Jetpack DataStore (Preferences) zur
 * dauerhaften Speicherung der Vertrauenspersonen.
 * Ersetzt klassische SharedPreferences durch die empfohlene,
 * asynchrone DataStore-API.
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val TRUSTED_CONTACTS_KEY = stringPreferencesKey("trusted_contacts")

        // Alte, inzwischen ersetzte Einzel-Telefonnummer (nur noch für die Migration relevant).
        private val LEGACY_TRUSTED_PHONE_NUMBER_KEY = stringPreferencesKey("trusted_phone_number")

        private val USER_PROFILE_KEY = stringPreferencesKey("user_profile")
        private val ROUTINE_PROGRESS_KEY = stringPreferencesKey("routine_progress")

        private val AUDIO_ENABLED_KEY = booleanPreferencesKey("audio_enabled")
        private val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")
    }

    /** Beobachtbarer Strom der gespeicherten Vertrauenspersonen. */
    val trustedContacts: Flow<List<TrustedContact>> = context.dataStore.data.map { preferences ->
        preferences[TRUSTED_CONTACTS_KEY]?.let { Json.decodeFromString(it) } ?: emptyList()
    }

    /** Speichert die übergebene Liste an Vertrauenspersonen dauerhaft. */
    suspend fun saveTrustedContacts(contacts: List<TrustedContact>) {
        context.dataStore.edit { preferences ->
            preferences[TRUSTED_CONTACTS_KEY] = Json.encodeToString(contacts)
        }
    }

    /**
     * Migriert einmalig die frühere Einzel-Telefonnummer in die neue Kontaktliste,
     * falls noch keine Liste gespeichert wurde. Ohne Wirkung, wenn es nichts zu migrieren gibt.
     */
    suspend fun migrateLegacyPhoneNumberIfNeeded() {
        val preferences = context.dataStore.data.first()
        if (preferences[TRUSTED_CONTACTS_KEY] != null) return

        val legacyNumber = preferences[LEGACY_TRUSTED_PHONE_NUMBER_KEY]
        if (legacyNumber.isNullOrBlank()) return

        val migrated = listOf(
            TrustedContact(
                id = UUID.randomUUID().toString(),
                name = context.getString(R.string.migration_default_contact_name),
                phoneNumber = legacyNumber,
                relationship = ContactRelationship.FAMILY
            )
        )
        context.dataStore.edit { prefs ->
            prefs[TRUSTED_CONTACTS_KEY] = Json.encodeToString(migrated)
            prefs.remove(LEGACY_TRUSTED_PHONE_NUMBER_KEY)
        }
    }

    /** Beobachtbarer Strom des lokalen Profils für den täglichen Übungsmodus (null, falls noch keins angelegt wurde). */
    val userProfile: Flow<UserProfile?> = context.dataStore.data.map { preferences ->
        preferences[USER_PROFILE_KEY]?.let { Json.decodeFromString(it) }
    }

    /** Speichert das lokale Profil (Name + Farbe) dauerhaft. */
    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_KEY] = Json.encodeToString(profile)
        }
    }

    /** Beobachtbarer Strom des aktuellen Routine-Fortschritts. */
    val routineProgress: Flow<RoutineProgress> = context.dataStore.data.map { preferences ->
        preferences[ROUTINE_PROGRESS_KEY]?.let { Json.decodeFromString(it) } ?: RoutineProgress()
    }

    /**
     * Trägt den Abschluss einer täglichen Übung für heute ein und aktualisiert die Streak.
     * Gibt den neuen Fortschritt zurück, damit die UI ihn sofort anzeigen kann.
     */
    suspend fun recordDailyExerciseCompletion(): RoutineProgress {
        var updated = RoutineProgress()
        context.dataStore.edit { preferences ->
            val current = preferences[ROUTINE_PROGRESS_KEY]?.let { Json.decodeFromString<RoutineProgress>(it) }
                ?: RoutineProgress()
            updated = current.withCompletionRecorded(LocalDate.now().toEpochDay())
            preferences[ROUTINE_PROGRESS_KEY] = Json.encodeToString(updated)
        }
        return updated
    }

    /** Beobachtbarer Strom, ob Sound-Effekte/Hintergrundmusik aktiviert sind (Standard: an). */
    val audioEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUDIO_ENABLED_KEY] ?: true
    }

    /** Beobachtbarer Strom, ob Vibrations-Feedback aktiviert ist (Standard: an). */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTICS_ENABLED_KEY] ?: true
    }

    suspend fun setAudioEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[AUDIO_ENABLED_KEY] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[HAPTICS_ENABLED_KEY] = enabled }
    }
}
