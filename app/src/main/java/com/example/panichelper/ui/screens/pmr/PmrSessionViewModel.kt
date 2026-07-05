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

        phaseJob = viewModelScope.launch {
            outer@ while (isActive) {
                for (phase in PmrRoutine.PHASES) {
                    if (System.currentTimeMillis() - startTime >= totalDurationMillis) break@outer
                    currentPhase = phase
                    delay(phase.durationMillis)
                }
            }
        }

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
