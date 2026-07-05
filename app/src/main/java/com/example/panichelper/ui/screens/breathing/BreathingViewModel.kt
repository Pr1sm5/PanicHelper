package com.example.panichelper.ui.screens.breathing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BreathingViewModel : ViewModel() {

    var selectedTechnique by mutableStateOf(BreathingTechniques.DEFAULT)
        private set

    var currentPhase by mutableStateOf(BreathingTechniques.DEFAULT.phases.first())
        private set

    // Index von currentPhase innerhalb selectedTechnique.phases. Wird separat
    // gepflegt (statt über Gleichheit von currentPhase ermittelt), da einzelne
    // Techniken (z. B. Box-Breathing) zwei Phasen mit identischer Konfiguration
    // enthalten können (zwei "Halten"-Schritte).
    var currentPhaseIndex by mutableIntStateOf(0)
        private set

    var completedCycles by mutableIntStateOf(0)
        private set

    var vibrationIntensity by mutableStateOf(0.7f)
        private set

    // Kurzer Countdown vor dem allerersten Start, damit der Kreis nicht
    // sofort ausgedehnt startet, sondern klein beginnt und dann wächst.
    var isCountingDown by mutableStateOf(true)
        private set

    var countdownValue by mutableIntStateOf(COUNTDOWN_START)
        private set

    var remainingMillis by mutableLongStateOf(TOTAL_DURATION_MS)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    private var sessionJob: Job? = null

    init {
        viewModelScope.launch {
            for (value in COUNTDOWN_START downTo 1) {
                countdownValue = value
                delay(1_000L)
            }
            isCountingDown = false
            startCycle()
        }
    }

    fun setTechnique(technique: BreathingTechnique) {
        if (technique == selectedTechnique) return
        selectedTechnique = technique
        completedCycles = 0
        currentPhaseIndex = 0
        remainingMillis = TOTAL_DURATION_MS
        isCompleted = false
        sessionJob?.cancel()
        startCycle()
    }

    fun updateVibrationIntensity(intensity: Float) {
        vibrationIntensity = intensity
    }

    private fun startCycle() {
        val startTime = System.currentTimeMillis()

        sessionJob = viewModelScope.launch {
            coroutineScope {
                val tickerJob = launch {
                    while (isActive) {
                        val remaining = (TOTAL_DURATION_MS - (System.currentTimeMillis() - startTime)).coerceAtLeast(0)
                        remainingMillis = remaining
                        if (remaining <= 0) break
                        delay(minOf(1_000L, remaining))
                    }
                }

                // Läuft jeden Atemzyklus (alle Phasen der Technik) immer
                // vollständig zu Ende - die Zeit wird erst danach geprüft,
                // damit die Sitzung nie mitten in einer Ein-/Ausatmung endet.
                while (isActive) {
                    for ((index, phase) in selectedTechnique.phases.withIndex()) {
                        currentPhase = phase
                        currentPhaseIndex = index
                        delay(phase.durationMillis)
                    }
                    completedCycles++
                    if (System.currentTimeMillis() - startTime >= TOTAL_DURATION_MS) break
                }

                tickerJob.cancel()
                remainingMillis = 0
                isCompleted = true
            }
        }
    }

    companion object {
        private const val COUNTDOWN_START = 5
        private const val TOTAL_DURATION_MS = 3 * 60_000L
    }
}
