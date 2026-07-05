package com.example.panichelper.ui.screens.pmr

import com.example.panichelper.audio.SoundPlayer
import com.example.panichelper.ui.screens.breathing.BreathingSoundCategory
import kotlin.random.Random

/**
 * Es gibt (noch) keine PMR-spezifischen Sprach-Hinweise für "Anspannen" oder
 * "Loslassen", daher bleiben Tense-Phasen stumm (nur Kreis-Animation +
 * Vibrations-Tick). Während der deutlich längeren Loslassen-Phase wird
 * stattdessen gelegentlich (nicht bei jeder Wiederholung) eine der
 * allgemeinen "Constructive"-Zeilen eingestreut, die auch die Atemübung
 * während ihrer Halten-Phasen verwendet - motivierende Sätze sind nicht an
 * eine bestimmte Übung gebunden.
 */
private const val MIN_DURATION_FOR_CONSTRUCTIVE_MS = 5_000L
private const val CONSTRUCTIVE_CHANCE = 0.3f

fun SoundPlayer.playForPmrPhase(phase: PmrPhase) {
    if (phase.phaseState != PmrPhaseState.RELEASE) return
    val hasTimeForConstructive = phase.durationMillis >= MIN_DURATION_FOR_CONSTRUCTIVE_MS
    if (hasTimeForConstructive && Random.nextFloat() < CONSTRUCTIVE_CHANCE) {
        play(BreathingSoundCategory.CONSTRUCTIVE)
    }
}
