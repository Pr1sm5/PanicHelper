package com.example.panichelper.data

import kotlinx.serialization.Serializable

/**
 * Lokales Profil für den täglichen Übungsmodus.
 * @param colorArgb Gewählte Akzentfarbe als ARGB-Int (Color.toArgb()).
 */
@Serializable
data class UserProfile(
    val username: String,
    val colorArgb: Int
)
