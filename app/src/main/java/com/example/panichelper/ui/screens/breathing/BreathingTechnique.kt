package com.example.panichelper.ui.screens.breathing

import androidx.annotation.StringRes
import com.example.panichelper.R

enum class BreathingVisualState { EXPAND, HOLD_LARGE, CONTRACT, HOLD_SMALL }

data class BreathingPhaseConfig(
    @StringRes val labelRes: Int,
    val durationMillis: Long,
    val visualState: BreathingVisualState
)

data class BreathingTechnique(
    @StringRes val nameRes: Int,
    val phases: List<BreathingPhaseConfig>
)

object BreathingTechniques {
    val FOUR_FOUR_SIX = BreathingTechnique(
        nameRes = R.string.technique_4_4_6,
        phases = listOf(
            BreathingPhaseConfig(R.string.breathing_phase_inhale, 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig(R.string.breathing_phase_hold,   4_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig(R.string.breathing_phase_exhale, 6_000L, BreathingVisualState.CONTRACT),
        )
    )

    val BOX_BREATHING = BreathingTechnique(
        nameRes = R.string.technique_box,
        phases = listOf(
            BreathingPhaseConfig(R.string.breathing_phase_inhale, 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig(R.string.breathing_phase_hold,   4_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig(R.string.breathing_phase_exhale, 4_000L, BreathingVisualState.CONTRACT),
            BreathingPhaseConfig(R.string.breathing_phase_hold,   4_000L, BreathingVisualState.HOLD_SMALL),
        )
    )

    val FOUR_SEVEN_EIGHT = BreathingTechnique(
        nameRes = R.string.technique_4_7_8,
        phases = listOf(
            BreathingPhaseConfig(R.string.breathing_phase_inhale, 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig(R.string.breathing_phase_hold,   7_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig(R.string.breathing_phase_exhale, 8_000L, BreathingVisualState.CONTRACT),
        )
    )

    val ALL = listOf(FOUR_FOUR_SIX, BOX_BREATHING, FOUR_SEVEN_EIGHT)
    val DEFAULT = FOUR_FOUR_SIX
}
