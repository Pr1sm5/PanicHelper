package com.example.panichelper.ui.screens.pmr

import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer

/**
 * PMR-spezifische Sprach-Hinweise für den universellen [SoundPlayer]: eine
 * Einleitung vor Sitzungsbeginn ([START], ca. 8-9 Sekunden lang) sowie je eine
 * Anspannen- ("pm_a_...") und Loslassen-Zeile ("pm_e_...") pro Muskelgruppe.
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
    RELEASE_FEET(listOf(R.raw.pm_e_fuesse));

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

/** Spielt die zur Muskelgruppe und Phase (Anspannen/Loslassen) passende Zeile. */
suspend fun SoundPlayer.playForPmrPhase(phase: PmrPhase) {
    val category = when (phase.phaseState) {
        PmrPhaseState.TENSE -> PmrSoundCategory.TENSE[phase.muscleGroup.index]
        PmrPhaseState.RELEASE -> PmrSoundCategory.RELEASE[phase.muscleGroup.index]
    }
    play(category)
}
