package com.example.panichelper.data

import androidx.annotation.DrawableRes
import com.example.panichelper.R

/**
 * Repräsentiert einen einzelnen Ablenkungs-Inhalt.
 * Es gibt drei Varianten:
 * - ImageContent: Bild + Beobachtungsfrage darunter (Frage per Index in ein String-Array).
 * - ColorPromptContent: "Suche dir 5 Objekte mit der Farbe X"-Übung, Farbe per Index.
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

    data class ColorPromptContent(
        val colorIndex: Int
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
        R.drawable.distraction_8
    )

    // Anzahl der Einträge in R.array.distraction_image_questions.
    private const val IMAGE_QUESTION_COUNT = 5

    // Anzahl der Einträge in R.array.distraction_grounding_colors.
    private const val GROUNDING_COLOR_COUNT = 8

    // Anzahl der Einträge in R.array.distraction_fixed_prompts.
    private const val FIXED_PROMPT_COUNT = 5

    /** Anzahl der Bilder. */
    fun imageCount(): Int = imageIds.size

    /**
     * Gesamtanzahl aller Inhalte (Bilder + Farb-Übung + Text-Prompts).
     * Reihenfolge: zuerst alle Bilder, danach die Farb-Übung, danach die festen Prompts.
     */
    fun totalContentCount(): Int = imageIds.size + 1 + FIXED_PROMPT_COUNT

    /** Gibt eine zufällige, gültige Index-Position über alle Inhalte zurück. */
    fun randomContentIndex(): Int = (0 until totalContentCount()).random()

    /**
     * Erzeugt den Inhalt für einen gegebenen Gesamt-Index.
     * Bild-Indizes liefern ein neues ImageContent mit zufälligem Fragen-Index,
     * der nächste Index liefert die Farb-Übung mit zufälligem Farb-Index,
     * die restlichen Indizes liefern die Grounding-Text-Prompts.
     */
    fun getContentAt(index: Int): DistractionContent {
        val imageCount = imageIds.size
        return when {
            index < imageCount -> DistractionContent.ImageContent(
                imageRes = imageIds[index],
                questionIndex = (0 until IMAGE_QUESTION_COUNT).random()
            )
            index == imageCount -> DistractionContent.ColorPromptContent(
                colorIndex = (0 until GROUNDING_COLOR_COUNT).random()
            )
            else -> {
                // index - imageCount - 1 -> Position innerhalb der festen Prompts
                val promptIndex = index - imageCount - 1
                DistractionContent.TextContent(promptIndex = promptIndex)
            }
        }
    }
}
