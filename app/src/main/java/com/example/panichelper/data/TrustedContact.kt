package com.example.panichelper.data

import androidx.annotation.StringRes
import com.example.panichelper.R
import kotlinx.serialization.Serializable

/**
 * Art der Beziehung zu einer gespeicherten Vertrauensperson.
 * Bestimmt nur die Anzeige (Label/Chip) auf der Hilfe-Karte.
 */
@Serializable
enum class ContactRelationship(@StringRes val labelRes: Int) {
    FAMILY(R.string.relationship_family),
    FRIEND(R.string.relationship_friend),
    THERAPIST(R.string.relationship_therapist)
}

/**
 * Eine gespeicherte Vertrauensperson mit Name, Telefonnummer und Beziehungsart.
 * Wird als Liste (JSON) über den PreferencesManager persistiert.
 */
@Serializable
data class TrustedContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: ContactRelationship
)
