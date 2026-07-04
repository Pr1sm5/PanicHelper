package com.example.panichelper.data

import androidx.annotation.DrawableRes
import com.example.panichelper.R

/**
 * Datenmodell für ein einzelnes Ablenkungsbild inkl. zugehöriger Frage.
 */
data class DistractionItem(
    @DrawableRes val imageRes: Int,
    val question: String
)

/**
 * Stellt die lokal verfügbaren Ablenkungsbilder und Beobachtungsfragen bereit.
 * Komplett offline, keine Netzwerkanfragen nötig.
 */
object DistractionRepository {

    // Liste der verfügbaren Bild-Ressourcen-IDs (lokale Drawables).
    private val imageIds: List<Int> = listOf(
        R.drawable.distraction_1,
        R.drawable.distraction_2,
        R.drawable.distraction_3,
        R.drawable.distraction_4
    )

    // Liste der möglichen Beobachtungsfragen, die unter einem Bild erscheinen.
    private val questions: List<String> = listOf(
        "Welche Farbe hat das Fell?",
        "Wie viele Ohren siehst du?",
        "Schaue auf die Augen.",
        "Welche Emotion vermittelt das Bild?",
        "Zähle die Pfoten."
    )

    /** Gesamtanzahl der verfügbaren Bilder. */
    fun imageCount(): Int = imageIds.size

    /** Gibt die Bild-Ressourcen-ID für einen gegebenen Index zurück. */
    fun getImageResAt(index: Int): Int = imageIds[index]

    /** Gibt eine zufällige, gültige Bild-Index-Position zurück. */
    fun randomImageIndex(): Int = imageIds.indices.random()

    /** Gibt eine zufällige Beobachtungsfrage zurück. */
    fun randomQuestion(): String = questions.random()
}