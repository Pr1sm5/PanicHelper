package com.example.panichelper.data

import androidx.annotation.DrawableRes
import com.example.panichelper.R

/**
 * Repräsentiert einen einzelnen Ablenkungs-Inhalt.
 * Es gibt zwei Varianten:
 * - ImageContent: Bild + Beobachtungsfrage darunter (Frage per Index in ein String-Array).
 * - TextContent: Nur ein großer Text ohne Bild (z. B. Grounding-Übungen), Text per Index.
 * Die eigentlichen Texte liegen als Ressourcen vor (strings.xml) und werden erst
 * im Composable über stringResource()/stringArrayResource() aufgelöst, damit sie
 * je nach eingestellter App-Sprache übersetzt angezeigt werden.
 */
sealed class DistractionContent {
    data class ImageContent(
        @DrawableRes val imageRes: Int,
        val questionIndex: Int
    ) : DistractionContent()

    data class TextContent(
        val promptIndex: Int
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
        R.drawable.distraction_8,
        R.drawable.distraction_9,
        R.drawable.distraction_10,
        R.drawable.distraction_11
    )

    // Anzahl der Einträge in R.array.distraction_image_questions.
    private const val IMAGE_QUESTION_COUNT = 5

    // Anzahl der Einträge in R.array.distraction_fixed_prompts.
    private const val FIXED_PROMPT_COUNT = 5

    /** Anzahl der Bilder. */
    fun imageCount(): Int = imageIds.size

    /**
     * Gesamtanzahl aller Inhalte (Bilder + Text-Prompts).
     * Reihenfolge: zuerst alle Bilder, danach die festen Text-Prompts.
     * WICHTIG: Hier darf keine zusätzliche "+1" mehr stehen - die Farb-Übung
     * wurde entfernt, es gibt keinen Extra-Slot mehr in der Rotation.
     */
    fun totalContentCount(): Int = imageIds.size + FIXED_PROMPT_COUNT

    /** Gibt eine zufällige, gültige Index-Position über alle Inhalte zurück. */
    fun randomContentIndex(): Int = (0 until totalContentCount()).random()

    /**
     * Erzeugt den Inhalt für einen gegebenen Gesamt-Index.
     * Bild-Indizes liefern ein neues ImageContent mit zufälligem Fragen-Index,
     * die restlichen Indizes liefern die Grounding-Text-Prompts.
     */
    fun getContentAt(index: Int): DistractionContent {
        val imageCount = imageIds.size
        return when {
            index < imageCount -> DistractionContent.ImageContent(
                imageRes = imageIds[index],
                questionIndex = (0 until IMAGE_QUESTION_COUNT).random()
            )
            else -> {
                // index - imageCount -> Position innerhalb der festen Prompts
                // (KEIN "- 1" mehr, da es keinen Farb-Slot mehr gibt).
                val promptIndex = index - imageCount
                DistractionContent.TextContent(promptIndex = promptIndex)
            }
        }
    }
}