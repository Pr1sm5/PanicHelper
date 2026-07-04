package com.example.panichelper.ui.screens.breathing

enum class BreathingVisualState { EXPAND, HOLD_LARGE, CONTRACT, HOLD_SMALL }

data class BreathingPhaseConfig(
    val label: String,
    val durationMillis: Long,
    val visualState: BreathingVisualState
)

data class BreathingTechnique(
    val name: String,
    val phases: List<BreathingPhaseConfig>
)

object BreathingTechniques {
    val FOUR_FOUR_SIX = BreathingTechnique(
        name = "4-4-6",
        phases = listOf(
            BreathingPhaseConfig("Einatmen", 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig("Halten",   4_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig("Ausatmen", 6_000L, BreathingVisualState.CONTRACT),
        )
    )

    val BOX_BREATHING = BreathingTechnique(
        name = "Box",
        phases = listOf(
            BreathingPhaseConfig("Einatmen", 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig("Halten",   4_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig("Ausatmen", 4_000L, BreathingVisualState.CONTRACT),
            BreathingPhaseConfig("Halten",   4_000L, BreathingVisualState.HOLD_SMALL),
        )
    )

    val FOUR_SEVEN_EIGHT = BreathingTechnique(
        name = "4-7-8",
        phases = listOf(
            BreathingPhaseConfig("Einatmen", 4_000L, BreathingVisualState.EXPAND),
            BreathingPhaseConfig("Halten",   7_000L, BreathingVisualState.HOLD_LARGE),
            BreathingPhaseConfig("Ausatmen", 8_000L, BreathingVisualState.CONTRACT),
        )
    )

    val ALL = listOf(FOUR_FOUR_SIX, BOX_BREATHING, FOUR_SEVEN_EIGHT)
    val DEFAULT = FOUR_FOUR_SIX
}
