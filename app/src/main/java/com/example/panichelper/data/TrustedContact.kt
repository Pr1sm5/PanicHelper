package com.example.panichelper.data

import kotlinx.serialization.Serializable

/**
 * Art der Beziehung zu einer gespeicherten Vertrauensperson.
 * Bestimmt nur die Anzeige (Label/Chip) auf der Hilfe-Karte.
 */
@Serializable
enum class ContactRelationship(val label: String) {
    FAMILY("Familie"),
    FRIEND("Freund:in"),
    THERAPIST("Therapeut:in")
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
