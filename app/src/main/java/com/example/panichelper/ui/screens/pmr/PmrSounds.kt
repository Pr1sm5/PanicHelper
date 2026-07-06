package com.example.panichelper.ui.screens.pmr

import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer
import kotlinx.coroutines.delay

/**
 * PMR-spezifische Sprach-Hinweise für den universellen [SoundPlayer]: eine
 * Einleitung vor Sitzungsbeginn ([START], ca. 8-9 Sekunden lang) sowie je eine
 * Anspannen- ("pm_a_...") und Loslassen-Zeile ("pm_e_...") pro Muskelgruppe.
 *
 * Für die lange Loslassen-Phase gibt es zusätzlich zwei generische, von der
 * Muskelgruppe unabhängige Füll-Zeilen ([RELEASE_FILLER_SECOND],
 * [RELEASE_FILLER_THIRD]) mit mehreren Varianten - lange Stille während des
 * Loslassens kann sonst wieder in Gedankenspiralen kippen. Beide haben
 * mehrere Varianten, damit [SoundPlayer.play] automatisch nie zweimal
 * hintereinander dieselbe Variante wählt.
 *
 * [TENSE] und [RELEASE] müssen in derselben Reihenfolge stehen wie
 * [PmrRoutine.MUSCLE_GROUPS] - [PmrPhase.muscleGroup]s `index` dient direkt
 * als Position in diesen Listen.
 *
 * Lokalisierung passiert automatisch über Androids Ressourcen-Auflösung,
 * genau wie bei den Atem-Hinweisen.
 */
enum class PmrSoundCategory(override val variants: List<Int>) : SoundCategory {
    START(listOf(R.raw.pm_einleitung)),
    TENSE_HANDS(listOf(R.raw.pm_a_hand)),
    TENSE_ARMS(listOf(R.raw.pm_a_arme)),
    TENSE_SHOULDERS(listOf(R.raw.pm_a_schulter)),
    TENSE_FACE(listOf(R.raw.pm_a_gesicht)),
    TENSE_CHEST(listOf(R.raw.pm_a_brust)),
    TENSE_ABDOMEN(listOf(R.raw.pm_a_bauch)),
    TENSE_LEGS(listOf(R.raw.pm_a_beine)),
    TENSE_FEET(listOf(R.raw.pm_a_fuesse)),
    RELEASE_HANDS(listOf(R.raw.pm_e_hand)),
    RELEASE_ARMS(listOf(R.raw.pm_e_arm)),
    RELEASE_SHOULDERS(listOf(R.raw.pm_e_schulter)),
    RELEASE_FACE(listOf(R.raw.pm_e_gesicht)),
    RELEASE_CHEST(listOf(R.raw.pm_e_brust)),
    RELEASE_ABDOMEN(listOf(R.raw.pm_e_bauch)),
    RELEASE_LEGS(listOf(R.raw.pm_e_bein)),
    RELEASE_FEET(listOf(R.raw.pm_e_fuesse)),
    RELEASE_FILLER_SECOND(
        listOf(R.raw.pm_e_second_1, R.raw.pm_e_second_2, R.raw.pm_e_second_3)
    ),
    RELEASE_FILLER_THIRD(
        listOf(R.raw.pm_e_third_1, R.raw.pm_e_third_2, R.raw.pm_e_third_3, R.raw.pm_e_third_4, R.raw.pm_e_third_5)
    );

    companion object {
        // Reihenfolge muss zu PmrRoutine.MUSCLE_GROUPS passen.
        val TENSE = listOf(
            TENSE_HANDS, TENSE_ARMS, TENSE_SHOULDERS, TENSE_FACE,
            TENSE_CHEST, TENSE_ABDOMEN, TENSE_LEGS, TENSE_FEET
        )
        val RELEASE = listOf(
            RELEASE_HANDS, RELEASE_ARMS, RELEASE_SHOULDERS, RELEASE_FACE,
            RELEASE_CHEST, RELEASE_ABDOMEN, RELEASE_LEGS, RELEASE_FEET
        )
    }
}

// Zeitversatz innerhalb der Loslassen-Phase (PmrRoutine.RELEASE_DURATION_MS =
// 15s): die muskelgruppen-spezifische Zeile kommt sofort, die zwei generischen
// Füll-Zeilen folgen kurz danach gestaffelt, mit etwas Stille am Ende, bevor
// die nächste Muskelgruppe angesagt wird.
private const val RELEASE_FILLER_SECOND_DELAY_MS = 4_000L
private const val RELEASE_FILLER_THIRD_DELAY_MS = 5_000L

/** Spielt die zur Muskelgruppe und Phase (Anspannen/Loslassen) passende Zeile. */
suspend fun SoundPlayer.playForPmrPhase(phase: PmrPhase) {
    when (phase.phaseState) {
        PmrPhaseState.TENSE -> play(PmrSoundCategory.TENSE[phase.muscleGroup.index])
        PmrPhaseState.RELEASE -> playPmrReleaseSequence(phase)
    }
}

/**
 * Füllt die lange Loslassen-Phase mit drei zeitlich gestaffelten Zeilen statt
 * nur der einen Cue am Anfang, damit die Stille nicht wieder in
 * Gedankenspiralen kippen lässt: erst die muskelgruppen-spezifische Zeile,
 * dann kurz danach eine der generischen Füll-Zeilen, dann noch eine.
 */
private suspend fun SoundPlayer.playPmrReleaseSequence(phase: PmrPhase) {
    play(PmrSoundCategory.RELEASE[phase.muscleGroup.index])
    delay(RELEASE_FILLER_SECOND_DELAY_MS)
    play(PmrSoundCategory.RELEASE_FILLER_SECOND)
    delay(RELEASE_FILLER_THIRD_DELAY_MS)
    play(PmrSoundCategory.RELEASE_FILLER_THIRD)
}
