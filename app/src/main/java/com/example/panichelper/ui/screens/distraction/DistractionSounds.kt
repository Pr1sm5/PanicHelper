package com.example.panichelper.ui.screens.distraction

import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.data.DistractionContent

/**
 * Sprach-Hinweise für die Ablenkungsübung, für den universellen [SoundPlayer].
 * Jede Zeile ist eindeutig (keine Varianten wie bei der Atemübung), daher
 * genügt jeweils eine einzelne Ressourcen-ID pro Kategorie.
 *
 * Upgrade-Pfad: Eine neue Bild-Frage oder einen neuen Text-Prompt hinzufügen?
 * MP3 in res/raw (und optional res/raw-de/res/raw-en) ablegen, hier einen
 * Eintrag ergänzen und ihn unten in [IMAGE_QUESTIONS] bzw. [FIXED_PROMPTS]
 * an der zum Index in DistractionRepository passenden Stelle einreihen.
 *
 * Lokalisierung passiert automatisch über Androids Ressourcen-Auflösung
 * (res/raw-de bevorzugt bei deutscher App-Sprache, sonst res/raw), genau wie
 * bei den Atem-Hinweisen.
 */
enum class DistractionSoundCategory(override val variants: List<Int>) : SoundCategory {
    QUESTION_FUR(listOf(R.raw.distraction_fur)),
    QUESTION_EARS(listOf(R.raw.distraction_ears)),
    QUESTION_EYES(listOf(R.raw.distraction_eyes)),
    QUESTION_EMOTION(listOf(R.raw.distraction_emotion)),
    QUESTION_PAWS(listOf(R.raw.distraction_paws)),
    PROMPT_FIVE_THINGS(listOf(R.raw.distraction_five_things)),
    PROMPT_SMELL(listOf(R.raw.distraction_smell)),
    PROMPT_SOUNDS(listOf(R.raw.distraction_sounds)),
    PROMPT_FEEL(listOf(R.raw.distraction_feel)),
    PROMPT_TASTE(listOf(R.raw.distraction_taste));

    companion object {
        // Reihenfolge muss zu R.array.distraction_image_questions passen.
        val IMAGE_QUESTIONS = listOf(QUESTION_FUR, QUESTION_EARS, QUESTION_EYES, QUESTION_EMOTION, QUESTION_PAWS)

        // Reihenfolge muss zu R.array.distraction_fixed_prompts passen.
        val FIXED_PROMPTS = listOf(PROMPT_FIVE_THINGS, PROMPT_SMELL, PROMPT_SOUNDS, PROMPT_FEEL, PROMPT_TASTE)
    }
}

/** Spielt den zum aktuell angezeigten Ablenkungs-Inhalt passenden Sprach-Hinweis. */
fun SoundPlayer.playForDistractionContent(content: DistractionContent) {
    when (content) {
        is DistractionContent.ImageContent ->
            play(DistractionSoundCategory.IMAGE_QUESTIONS[content.questionIndex])
        is DistractionContent.TextContent ->
            play(DistractionSoundCategory.FIXED_PROMPTS[content.promptIndex])
    }
}
