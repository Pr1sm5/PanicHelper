package com.example.panichelper.data

import androidx.annotation.DrawableRes
import com.example.panichelper.R

/**
 * Repräsentiert einen einzelnen Ablenkungs-Inhalt.
 * Es gibt zwei Varianten:
 * - ImageContent: Bild + Beobachtungsfrage darunter.
 * - TextContent: Nur ein großer Text ohne Bild (z. B. Grounding-Übungen).
 */
sealed class DistractionContent {
    data class ImageContent(
        @DrawableRes val imageRes: Int,
        val question: String
    ) : DistractionContent()

    data class TextContent(
        val text: String
    ) : DistractionContent()
}

/**
 * Stellt alle lokal verfügbaren Ablenkungs-Inhalte bereit:
 * sowohl Bilder mit Beobachtungsfragen als auch reine Text-Prompts
 * (Grounding-Übungen ohne Bild). Komplett offline, keine Netzwerkanfragen.
 */
object DistractionRepository {

    // Liste der verfügbaren Bild-Ressourcen-IDs (lokale Drawables).
    private val imageIds: List<Int> = listOf(
        R.drawable.distraction_1,
        R.drawable.distraction_2,
        R.drawable.distraction_3,
        R.drawable.distraction_4,
        R.drawable.distraction_5,
        R.drawable.distraction_6,
        R.drawable.distraction_7,
        R.drawable.distraction_8
    )

    // Liste der möglichen Beobachtungsfragen, die unter einem Bild erscheinen.
    private val imageQuestions: List<String> = listOf(
        "Welche Farbe hat das Fell?",
        "Wie viele Ohren siehst du?",
        "Schaue auf die Augen.",
        "Welche Emotion vermittelt das Bild?",
        "Zähle die Pfoten."
    )

    // Farben für die "Suche 5 Objekte..."-Übung. Die Farbe wird bei jeder
    // Anzeige neu zufällig gewählt, damit die Übung variiert.
    private val groundingColors: List<String> = listOf(
        "Rot", "Blau", "Grün", "Gelb", "Orange", "Lila", "Türkis", "Rosa"
    )

    // Feste Text-Prompts ohne Bild (Grounding-Techniken).
    // Der Farb-Prompt wird separat behandelt, da die Farbe variiert.
    private val fixedTextPrompts: List<String> = listOf(
        "Benennen Sie fünf Dinge, die Sie sehen.",
        "Nehmen Sie zwei Gerüche wahr.",
        "Hören Sie drei Geräusche in Ihrer Umgebung?",
        "Spüren Sie vier Dinge, die Sie berühren können.",
        "Und konzentrieren Sie sich auf eine Sache, die Sie schmecken."
    )

    /** Anzahl der Bilder. */
    fun imageCount(): Int = imageIds.size

    /**
     * Gesamtanzahl aller Inhalte (Bilder + Text-Prompts).
     * Reihenfolge: zuerst alle Bilder, danach alle Text-Prompts
     * (Farb-Übung zuerst, danach die festen Prompts).
     */
    fun totalContentCount(): Int = imageIds.size + 1 + fixedTextPrompts.size

    /** Gibt eine zufällige, gültige Index-Position über alle Inhalte zurück. */
    fun randomContentIndex(): Int = (0 until totalContentCount()).random()

    /**
     * Erzeugt den Inhalt für einen gegebenen Gesamt-Index.
     * Bild-Indizes liefern ein neues ImageContent mit zufälliger Frage,
     * die restlichen Indizes liefern die Grounding-Text-Prompts.
     */
    fun getContentAt(index: Int): DistractionContent {
        val imageCount = imageIds.size
        return when {
            index < imageCount -> DistractionContent.ImageContent(
                imageRes = imageIds[index],
                question = imageQuestions.random()
            )
            index == imageCount -> DistractionContent.TextContent(
                text = "Suche dir 5 Objekte mit der Farbe ${groundingColors.random()}."
            )
            else -> {
                // index - imageCount - 1 -> Position innerhalb fixedTextPrompts
                val promptIndex = index - imageCount - 1
                DistractionContent.TextContent(text = fixedTextPrompts[promptIndex])
            }
        }
    }
}