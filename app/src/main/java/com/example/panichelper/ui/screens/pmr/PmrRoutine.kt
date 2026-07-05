package com.example.panichelper.ui.screens.pmr

import androidx.annotation.StringRes
import com.example.panichelper.R

/** Ob eine PMR-Phase das An- oder das Loslassen einer Muskelgruppe ist. */
enum class PmrPhaseState { TENSE, RELEASE }

/**
 * Eine einzelne Muskelgruppe der Übung, z. B. "Hände" oder "Schultern".
 *
 * @param index Position innerhalb von [PmrRoutine.MUSCLE_GROUPS] - wird genutzt,
 * um die passende Anspannen-/Loslassen-Sprachzeile in [PmrSoundCategory] zu finden.
 */
data class MuscleGroup(@StringRes val nameRes: Int, val index: Int)

/** Eine einzelne Phase (Anspannen oder Loslassen) einer bestimmten Muskelgruppe. */
data class PmrPhase(
    val muscleGroup: MuscleGroup,
    val phaseState: PmrPhaseState,
    val durationMillis: Long
)

/**
 * Feste Abfolge der klassischen progressiven Muskelentspannung nach Jacobson:
 * pro Muskelgruppe kurz anspannen, dann deutlich länger loslassen.
 *
 * Upgrade-Pfad: Eine weitere Muskelgruppe hinzufügen? MUSCLE_GROUPS ergänzen -
 * PHASES baut sich daraus automatisch neu auf.
 */
object PmrRoutine {
    private const val TENSE_DURATION_MS = 6_000L
    private const val RELEASE_DURATION_MS = 15_000L

    val MUSCLE_GROUPS = listOf(
        R.string.pmr_group_hands,
        R.string.pmr_group_arms,
        R.string.pmr_group_shoulders,
        R.string.pmr_group_face,
        R.string.pmr_group_chest,
        R.string.pmr_group_abdomen,
        R.string.pmr_group_legs,
        R.string.pmr_group_feet
    ).mapIndexed { index, nameRes -> MuscleGroup(nameRes, index) }

    val PHASES: List<PmrPhase> = MUSCLE_GROUPS.flatMap { group ->
        listOf(
            PmrPhase(group, PmrPhaseState.TENSE, TENSE_DURATION_MS),
            PmrPhase(group, PmrPhaseState.RELEASE, RELEASE_DURATION_MS)
        )
    }
}
