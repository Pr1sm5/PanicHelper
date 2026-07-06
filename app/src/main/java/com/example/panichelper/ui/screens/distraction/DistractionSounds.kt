package com.example.panichelper.ui.screens.distraction

import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.data.DistractionContent
import kotlinx.coroutines.delay

/**
 * Sprach-Hinweise für die Ablenkungsübung, für den universellen [SoundPlayer].
 * Die meisten Zeilen sind eindeutig (keine Varianten wie bei der Atemübung),
 * daher genügt meist eine einzelne Ressourcen-ID pro Kategorie. Für die feste
 * 5-4-3-2-1-Grounding-Reihenfolge im geführten Komplett-Durchlauf gibt es
 * zusätzlich "Early/Mid/Late"-Zwischenansagen je Schritt (teils mit mehreren
 * Varianten) - siehe [FILLER_SEQUENCES] und [playForDistractionContent].
 *
 * Upgrade-Pfad: Eine neue Bild-Frage oder einen neuen Text-Prompt hinzufügen?
 * MP3 in res/raw (und optional res/raw-de/res/raw-en) ablegen, hier einen
 * Eintrag ergänzen und ihn unten in [IMAGE_QUESTIONS] bzw. [FIXED_PROMPTS]
 * an der zum Index in DistractionRepository passenden Stelle einreihen.
 *
 * Lokalisierung passiert automatisch über Androids Ressourcen-Auflösung
 * (res/raw-de bevorzugt bei deutscher App-Sprache, sonst res/raw), genau wie
 * bei den Atem-Hinweisen. WICHTIG: Ein Ressourcenname muss dafür in allen
 * genutzten raw-Ordnern identisch heißen, sonst wird er nicht als Übersetzung
 * derselben Datei erkannt (das war z. B. bei distraction_five_things_early
 * kurzzeitig der Fall - in res/raw-de fehlte das "_1"-Suffix aus res/raw).
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
    PROMPT_TASTE(listOf(R.raw.distraction_taste)),

    FILLER_FIVE_THINGS_EARLY(listOf(R.raw.distraction_five_things_early)),
    FILLER_FIVE_THINGS_MID(listOf(R.raw.distraction_five_things_mid_1, R.raw.distraction_five_things_mid_2)),
    FILLER_FIVE_THINGS_LATE(listOf(R.raw.distraction_five_things_late_1, R.raw.distraction_five_things_late_2)),

    FILLER_FEEL_EARLY(listOf(R.raw.distraction_feel_early)),
    FILLER_FEEL_MID(listOf(R.raw.distraction_feel_mid_1, R.raw.distraction_feel_mid_2)),
    FILLER_FEEL_LATE(listOf(R.raw.distraction_feel_late)),

    FILLER_SOUND_EARLY(listOf(R.raw.distraction_sound_early)),
    FILLER_SOUND_MID(listOf(R.raw.distraction_sound_mid)),
    FILLER_SOUND_LATE(listOf(R.raw.distraction_sound_late)),

    FILLER_SMELL_EARLY(listOf(R.raw.distraction_smell_early)),
    FILLER_SMELL_LATE(listOf(R.raw.distraction_smell_late)),

    FILLER_TASTE_EARLY(listOf(R.raw.distraction_taste_early)),
    FILLER_TASTE_LATE(listOf(R.raw.distraction_taste_late));

    companion object {
        // Reihenfolge muss zu R.array.distraction_image_questions passen.
        val IMAGE_QUESTIONS = listOf(QUESTION_FUR, QUESTION_EARS, QUESTION_EYES, QUESTION_EMOTION, QUESTION_PAWS)

        // Reihenfolge muss zu R.array.distraction_fixed_prompts passen.
        val FIXED_PROMPTS = listOf(PROMPT_FIVE_THINGS, PROMPT_SMELL, PROMPT_SOUNDS, PROMPT_FEEL, PROMPT_TASTE)

        /**
         * Zwischenansagen je fixem Prompt (Index wie [FIXED_PROMPTS]/
         * `DistractionRepository`), mit Verzögerung ab dem jeweils vorherigen
         * Ereignis (nicht ab Schrittbeginn). Riechen/Schmecken sind zu kurz für
         * eine "Mid"-Zeile, deshalb nur Early/Late dort. Die Summe der
         * Verzögerungen bleibt bei jedem Schritt deutlich unter der jeweiligen
         * Schrittdauer aus GuidedSessionViewModel.GROUNDING_SEQUENCE, damit am
         * Ende noch ein paar Sekunden Ruhe vor dem nächsten Schritt bleiben.
         */
        val FILLER_SEQUENCES: Map<Int, List<DistractionFillerCue>> = mapOf(
            0 to listOf( // 5 Dinge sehen, 40s
                DistractionFillerCue(FILLER_FIVE_THINGS_EARLY, delayMs = 8_000L),
                DistractionFillerCue(FILLER_FIVE_THINGS_MID, delayMs = 12_000L),
                DistractionFillerCue(FILLER_FIVE_THINGS_LATE, delayMs = 11_000L)
            ),
            3 to listOf( // 4 Dinge berühren, 35s
                DistractionFillerCue(FILLER_FEEL_EARLY, delayMs = 7_000L),
                DistractionFillerCue(FILLER_FEEL_MID, delayMs = 10_000L),
                DistractionFillerCue(FILLER_FEEL_LATE, delayMs = 10_000L)
            ),
            2 to listOf( // 3 Geräusche hören, 30s
                DistractionFillerCue(FILLER_SOUND_EARLY, delayMs = 6_000L),
                DistractionFillerCue(FILLER_SOUND_MID, delayMs = 9_000L),
                DistractionFillerCue(FILLER_SOUND_LATE, delayMs = 8_000L)
            ),
            1 to listOf( // 2 Gerüche wahrnehmen, 20s - kein Mid-Clip vorhanden
                DistractionFillerCue(FILLER_SMELL_EARLY, delayMs = 6_000L),
                DistractionFillerCue(FILLER_SMELL_LATE, delayMs = 8_000L)
            ),
            4 to listOf( // 1 Sache schmecken, 15s - kein Mid-Clip vorhanden
                DistractionFillerCue(FILLER_TASTE_EARLY, delayMs = 4_000L),
                DistractionFillerCue(FILLER_TASTE_LATE, delayMs = 6_000L)
            )
        )
    }
}

/** Eine Zwischenansage, [delayMs] nach dem vorherigen Ereignis der Sequenz fällig. */
data class DistractionFillerCue(val category: DistractionSoundCategory, val delayMs: Long)

/**
 * Spielt den zum aktuell angezeigten Ablenkungs-Inhalt passenden Sprach-Hinweis.
 *
 * @param activeDurationMillis Restliche Dauer des aktuellen Schritts - nur im
 * geführten Komplett-Durchlauf bekannt und übergeben. Ist ein Wert gesetzt,
 * werden zusätzlich die passenden Early/Mid/Late-Zwischenansagen aus
 * [DistractionSoundCategory.FILLER_SEQUENCES] zeitversetzt nachgeschoben, ohne
 * die verbleibende Schrittdauer zu überschreiten. Bleibt `null` (Standard) für
 * den eigenständigen Ablenkungs-Screen, der keine feste Schrittdauer kennt.
 */
suspend fun SoundPlayer.playForDistractionContent(content: DistractionContent, activeDurationMillis: Long? = null) {
    when (content) {
        is DistractionContent.ImageContent ->
            play(DistractionSoundCategory.IMAGE_QUESTIONS[content.questionIndex])
        is DistractionContent.TextContent -> {
            play(DistractionSoundCategory.FIXED_PROMPTS[content.promptIndex])
            if (activeDurationMillis != null) {
                playDistractionFillerSequence(content.promptIndex, activeDurationMillis)
            }
        }
    }
}

private suspend fun SoundPlayer.playDistractionFillerSequence(promptIndex: Int, activeDurationMillis: Long) {
    val cues = DistractionSoundCategory.FILLER_SEQUENCES[promptIndex] ?: return
    var elapsedMillis = 0L
    for (cue in cues) {
        elapsedMillis += cue.delayMs
        if (elapsedMillis >= activeDurationMillis) break
        delay(cue.delayMs)
        play(cue.category)
    }
}
