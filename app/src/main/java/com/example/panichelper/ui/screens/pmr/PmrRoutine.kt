package com.example.panichelper.ui.screens.pmr

import androidx.annotation.StringRes
import com.example.panichelper.R

/** Ob eine PMR-Phase das An- oder das Loslassen einer Muskelgruppe ist. */
enum class PmrPhaseState { TENSE, RELEASE }

/** Eine einzelne Muskelgruppe der Übung, z. B. "Hände" oder "Schultern". */
data class MuscleGroup(@StringRes val nameRes: Int)

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
        MuscleGroup(R.string.pmr_group_hands),
        MuscleGroup(R.string.pmr_group_arms),
        MuscleGroup(R.string.pmr_group_shoulders),
        MuscleGroup(R.string.pmr_group_face),
        MuscleGroup(R.string.pmr_group_chest),
        MuscleGroup(R.string.pmr_group_abdomen),
        MuscleGroup(R.string.pmr_group_legs),
        MuscleGroup(R.string.pmr_group_feet)
    )

    val PHASES: List<PmrPhase> = MUSCLE_GROUPS.flatMap { group ->
        listOf(
            PmrPhase(group, PmrPhaseState.TENSE, TENSE_DURATION_MS),
            PmrPhase(group, PmrPhaseState.RELEASE, RELEASE_DURATION_MS)
        )
    }
}
