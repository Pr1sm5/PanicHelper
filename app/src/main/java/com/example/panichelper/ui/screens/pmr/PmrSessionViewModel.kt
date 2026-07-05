package com.example.panichelper.ui.screens.pmr

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Steuert eine zeitlich begrenzte, geführte Sitzung der progressiven
 * Muskelentspannung. Läuft [PmrRoutine.PHASES] in einer Schleife, bis
 * [totalDurationMillis] erreicht ist, und trägt den Abschluss danach in
 * dieselbe Routine-Statistik ein wie die Atemübung (eine gemeinsame Streak
 * über alle täglichen Routinen hinweg).
 *
 * @param totalDurationMillis Gesamtdauer der Sitzung in Millisekunden.
 */
class PmrSessionViewModel(
    application: Application,
    private val totalDurationMillis: Long
) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    var currentPhase by mutableStateOf(PmrRoutine.PHASES.first())
        private set

    var remainingMillis by mutableStateOf(totalDurationMillis)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    var updatedProgress by mutableStateOf<RoutineProgress?>(null)
        private set

    // Kurzer Countdown vor dem Start, analog zur Atemübung.
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
        val pairs = PmrRoutine.PHASES.chunked(2)

        // Läuft das Anspannen+Loslassen-Paar der aktuellen Muskelgruppe immer
        // vollständig zu Ende - die Zeit wird erst danach geprüft, damit die
        // Sitzung nie mitten im Anspannen oder Loslassen endet.
        phaseJob = viewModelScope.launch {
            outer@ while (isActive) {
                for (pair in pairs) {
                    for (phase in pair) {
                        currentPhase = phase
                        delay(phase.durationMillis)
                    }
                    if (System.currentTimeMillis() - startTime >= totalDurationMillis) {
                        finishSession()
                        break@outer
                    }
                }
            }
        }

        tickerJob = viewModelScope.launch {
            while (isActive) {
                val remaining = (totalDurationMillis - (System.currentTimeMillis() - startTime)).coerceAtLeast(0)
                remainingMillis = remaining
                if (remaining <= 0) break
                delay(minOf(1_000L, remaining))
            }
        }
    }

    private suspend fun finishSession() {
        tickerJob?.cancel()
        remainingMillis = 0
        updatedProgress = preferencesManager.recordDailyExerciseCompletion()
        isCompleted = true
    }

    override fun onCleared() {
        phaseJob?.cancel()
        tickerJob?.cancel()
    }

    companion object {
        // Länger als bei den anderen Übungen (5s), damit die ca. 8-9 Sekunden
        // lange Einleitung (pm_einleitung) vollständig abspielen kann, bevor
        // die erste Anspannen-Phase beginnt.
        private const val COUNTDOWN_START = 9
    }
}
