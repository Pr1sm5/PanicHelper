package com.example.panichelper.ui.screens.breathing

import com.example.panichelper.R
import com.example.panichelper.audio.SoundCategory
import com.example.panichelper.audio.SoundPlayer
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Sound-Kategorien der Atemübung, für den universellen [SoundPlayer].
 *
 * Upgrade-Pfad: Eine weitere Sprecher-Variante hinzufügen? Einfach die MP3 in
 * res/raw (und optional res/raw-de bzw. res/raw-en) ablegen und ihre R.raw-ID
 * hier in die passende Liste eintragen – Zufallsauswahl und "nicht zweimal
 * hintereinander" übernimmt [SoundPlayer] automatisch. Eine neue Kategorie
 * hinzufügen? Einen weiteren Eintrag ergänzen und unten in
 * [playForBreathingPhase] an der gewünschten Stelle auslösen.
 *
 * Lokalisierung passiert automatisch über Androids Ressourcen-Auflösung:
 * res/raw-de wird bei deutscher App-Sprache bevorzugt, res/raw-en bei
 * englischer; fehlt eine Sprachvariante (aktuell Englisch), fällt Android
 * automatisch auf res/raw (Standard) zurück – genau wie bei den strings.xml.
 */
enum class BreathingSoundCategory(override val variants: List<Int>) : SoundCategory {
    START(listOf(R.raw.start_1)),
    END_1(listOf(R.raw.end_1)),
    END_2(listOf(R.raw.end_2)),
    BREATH_IN(listOf(R.raw.breath_in_1, R.raw.breath_in_2, R.raw.breath_in_3)),
    BREATH_OUT(listOf(R.raw.breath_out_1, R.raw.breath_out_2, R.raw.breath_out_3)),
    BREATH_HOLD(listOf(R.raw.breath_hold_1, R.raw.breath_hold_2, R.raw.breath_hold_3)),
    CONSTRUCTIVE(listOf(R.raw.constructive_1, R.raw.constructive_2))
}

private const val MIN_DURATION_FOR_CONSTRUCTIVE_MS = 5_000L
private const val CONSTRUCTIVE_CHANCE = 0.3f
private const val END_SEQUENCE_GAP_MS = 700L

/** Spielt die beiden Abschluss-Zeilen nacheinander, mit einer kurzen Pause dazwischen. */
suspend fun SoundPlayer.playEndSequence() {
    play(BreathingSoundCategory.END_1)
    delay(END_SEQUENCE_GAP_MS)
    play(BreathingSoundCategory.END_2)
}

/**
 * Spielt den zur Atemphase passenden Hinweis. Während einer Halten-Phase wird
 * gelegentlich (nicht bei jeder Wiederholung) stattdessen eine
 * "Constructive"-Zeile eingestreut – aber nur, wenn die Phase lang genug ist,
 * damit der (meist längere) Clip nicht abgeschnitten wirkt.
 */
suspend fun SoundPlayer.playForBreathingPhase(phase: BreathingPhaseConfig) {
    when (phase.visualState) {
        BreathingVisualState.EXPAND -> play(BreathingSoundCategory.BREATH_IN)
        BreathingVisualState.CONTRACT -> play(BreathingSoundCategory.BREATH_OUT)
        BreathingVisualState.HOLD_LARGE, BreathingVisualState.HOLD_SMALL -> {
            val hasTimeForConstructive = phase.durationMillis >= MIN_DURATION_FOR_CONSTRUCTIVE_MS
            val category = if (hasTimeForConstructive && Random.nextFloat() < CONSTRUCTIVE_CHANCE) {
                BreathingSoundCategory.CONSTRUCTIVE
            } else {
                BreathingSoundCategory.BREATH_HOLD
            }
            play(category)
        }
    }
}
