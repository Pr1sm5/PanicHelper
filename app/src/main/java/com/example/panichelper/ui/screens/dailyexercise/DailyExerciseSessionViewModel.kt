package com.example.panichelper.ui.screens.dailyexercise

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.ui.screens.breathing.BreathingTechnique
import com.example.panichelper.ui.screens.breathing.BreathingPhaseConfig
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Steuert eine zeitlich begrenzte, geführte Atemsitzung im täglichen Übungsmodus.
 * Läuft die feste Standard-Atemtechnik in einer Schleife, bis [totalDurationMillis]
 * erreicht ist, und trägt den Abschluss danach einmalig in die Routine-Statistik ein.
 *
 * @param totalDurationMillis Gesamtdauer der Sitzung in Millisekunden.
 * @param technique Gewählte Atemtechnik, deren Phasen in der Sitzung durchlaufen werden.
 */
class DailyExerciseSessionViewModel(
    application: Application,
    private val totalDurationMillis: Long,
    private val technique: BreathingTechnique
) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    var currentPhase by mutableStateOf<BreathingPhaseConfig>(technique.phases.first())
        private set

    // Index von currentPhase innerhalb technique.phases, separat gepflegt (statt
    // über Gleichheit von currentPhase ermittelt), da einzelne Techniken
    // (z. B. Box-Breathing) zwei Phasen mit identischer Konfiguration haben können.
    var currentPhaseIndex by mutableIntStateOf(0)
        private set

    var remainingMillis by mutableStateOf(totalDurationMillis)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    var updatedProgress by mutableStateOf<RoutineProgress?>(null)
        private set

    var vibrationIntensity by mutableStateOf(0.7f)
        private set

    // Kurzer Countdown vor dem Start, damit der Kreis nicht sofort ausgedehnt
    // erscheint, sondern klein beginnt und dann wächst. Läuft außerhalb der
    // eigentlichen Sitzungsdauer, zählt also nicht von totalDurationMillis ab.
    var isCountingDown by mutableStateOf(true)
        private set

    var countdownValue by mutableIntStateOf(COUNTDOWN_START)
        private set

    private var phaseJob: Job? = null
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            for (value in COUNTDOWN_START downTo 1) {
                countdownValue = value
                delay(1_000L)
            }
            isCountingDown = false
            start()
        }
    }

    private fun start() {
        val startTime = System.currentTimeMillis()

        // Steuert nur die Atemphasen-Animation (wechselt alle paar Sekunden).
        phaseJob = viewModelScope.launch {
            outer@ while (isActive) {
                for ((index, phase) in technique.phases.withIndex()) {
                    if (System.currentTimeMillis() - startTime >= totalDurationMillis) break@outer
                    currentPhase = phase
                    currentPhaseIndex = index
                    delay(phase.durationMillis)
                }
            }
        }

        // Eigener Sekundentakt für die Restzeit, unabhängig von der Phasenlänge,
        // damit die Anzeige durchgehend läuft statt nur bei jedem Phasenwechsel.
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val remaining = totalDurationMillis - (System.currentTimeMillis() - startTime)
                if (remaining <= 0) {
                    remainingMillis = 0
                    finishSession()
                    break
                }
                remainingMillis = remaining
                delay(minOf(1_000L, remaining))
            }
        }
    }

    fun updateVibrationIntensity(intensity: Float) {
        vibrationIntensity = intensity
    }

    private suspend fun finishSession() {
        phaseJob?.cancel()
        updatedProgress = preferencesManager.recordDailyExerciseCompletion()
        isCompleted = true
    }

    override fun onCleared() {
        phaseJob?.cancel()
        tickerJob?.cancel()
    }

    companion object {
        private const val COUNTDOWN_START = 5
    }
}
