package com.example.panichelper.ui.screens.guidedsession

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.data.DistractionContent
import com.example.panichelper.data.DistractionRepository
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.ui.screens.breathing.BreathingTechniques
import com.example.panichelper.ui.screens.pmr.PmrRoutine
import com.example.panichelper.util.PreferencesManager
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Die aufeinanderfolgenden Abschnitte des geführten Komplett-Durchlaufs. */
enum class GuidedSessionStage { BREATHING, TRANSITION_TO_DISTRACTION, DISTRACTION, TRANSITION_TO_PMR, PMR, COMPLETED }

/**
 * Steuert den geführten Komplett-Durchlauf: eine feste Abfolge aus
 * Atemübung, kurzer Pause, mehreren Grounding-Übungen und progressiver
 * Muskelentspannung - alles in einer einzigen, ununterbrochenen Sitzung,
 * damit man nur einmal auf Start tippen muss. Technik/Dauer sind bewusst
 * fest vorgegeben (keine Auswahl nötig), Länge und Reihenfolge passend zu
 * einer kompletten Beruhigungs-Routine.
 *
 * Verwendet einen einzigen Coroutine-Durchlauf, der die Abschnitte
 * nacheinander abarbeitet (statt mehrerer parallel laufender Jobs wie bei
 * den einzelnen Übungs-ViewModels), da hier immer nur ein Abschnitt aktiv ist.
 */
class GuidedSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    var stage by mutableStateOf(GuidedSessionStage.BREATHING)
        private set

    // Kurzer Countdown vor dem allerersten Start.
    var isCountingDown by mutableStateOf(true)
        private set

    var countdownValue by mutableIntStateOf(COUNTDOWN_START)
        private set

    // Atemübung
    var breathingPhase by mutableStateOf(BreathingTechniques.DEFAULT.phases.first())
        private set

    var breathingRemainingMillis by mutableStateOf(BREATHING_DURATION_MS)
        private set

    // Übergangspause
    var transitionRemainingSeconds by mutableIntStateOf((TRANSITION_DURATION_MS / 1_000L).toInt())
        private set

    // Grounding/Ablenkung
    var distractionContent by mutableStateOf(randomDistractionContent())
        private set

    var distractionStepIndex by mutableIntStateOf(0)
        private set

    // Progressive Muskelentspannung
    var pmrPhase by mutableStateOf(PmrRoutine.PHASES.first())
        private set

    var pmrRemainingMillis by mutableStateOf(PMR_DURATION_MS)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    var updatedProgress by mutableStateOf<RoutineProgress?>(null)
        private set

    var vibrationIntensity by mutableStateOf(0.7f)
        private set

    private var sessionJob: Job? = null

    init {
        sessionJob = viewModelScope.launch {
            runCountdown()

            stage = GuidedSessionStage.BREATHING
            runBreathingStage()

            stage = GuidedSessionStage.TRANSITION_TO_DISTRACTION
            runTransitionStage(TRANSITION_DURATION_MS)

            stage = GuidedSessionStage.DISTRACTION
            runDistractionStage()

            stage = GuidedSessionStage.TRANSITION_TO_PMR
            runTransitionStage(PMR_TRANSITION_DURATION_MS)

            stage = GuidedSessionStage.PMR
            runPmrStage()

            finishSession()
        }
    }

    fun updateVibrationIntensity(intensity: Float) {
        vibrationIntensity = intensity
    }

    private suspend fun runCountdown() {
        for (value in COUNTDOWN_START downTo 1) {
            countdownValue = value
            delay(1_000L)
        }
        isCountingDown = false
    }

    private suspend fun runBreathingStage() = coroutineScope {
        val startTime = System.currentTimeMillis()
        val tickerJob = launch { tickRemaining(startTime, BREATHING_DURATION_MS) { breathingRemainingMillis = it } }

        // Läuft jeden Atemzyklus (alle Phasen) immer vollständig zu Ende - die
        // Zeit wird erst danach geprüft, damit die Übung nie mitten in einer
        // Ein-/Ausatmung endet.
        while (isActive) {
            for (currentPhase in BreathingTechniques.DEFAULT.phases) {
                breathingPhase = currentPhase
                delay(currentPhase.durationMillis)
            }
            if (System.currentTimeMillis() - startTime >= BREATHING_DURATION_MS) break
        }
        tickerJob.cancel()
        breathingRemainingMillis = 0
    }

    private suspend fun runTransitionStage(durationMillis: Long) {
        val totalSeconds = (durationMillis / 1_000L).toInt()
        for (value in totalSeconds downTo 1) {
            transitionRemainingSeconds = value
            delay(1_000L)
        }
    }

    private suspend fun runDistractionStage() {
        repeat(DISTRACTION_ITEM_COUNT) { index ->
            distractionStepIndex = index
            distractionContent = randomDistractionContent()
            delay(DISTRACTION_ITEM_DURATION_MS)
        }
    }

    private suspend fun runPmrStage() = coroutineScope {
        val startTime = System.currentTimeMillis()
        val tickerJob = launch { tickRemaining(startTime, PMR_DURATION_MS) { pmrRemainingMillis = it } }
        val pairs = PmrRoutine.PHASES.chunked(2)

        // Läuft das Anspannen+Loslassen-Paar der aktuellen Muskelgruppe immer
        // vollständig zu Ende, bevor die Zeit geprüft wird.
        outer@ while (isActive) {
            for (pair in pairs) {
                for (currentPhase in pair) {
                    pmrPhase = currentPhase
                    delay(currentPhase.durationMillis)
                }
                if (System.currentTimeMillis() - startTime >= PMR_DURATION_MS) break@outer
            }
        }
        tickerJob.cancel()
        pmrRemainingMillis = 0
    }

    /** Aktualisiert die Restzeit im Sekundentakt, unabhängig von der Phasenlänge. */
    private suspend fun tickRemaining(startTime: Long, totalDurationMillis: Long, onRemaining: (Long) -> Unit) {
        while (coroutineContext.isActive) {
            val remaining = totalDurationMillis - (System.currentTimeMillis() - startTime)
            if (remaining <= 0) {
                onRemaining(0)
                return
            }
            onRemaining(remaining)
            delay(minOf(1_000L, remaining))
        }
    }

    private suspend fun finishSession() {
        updatedProgress = preferencesManager.recordDailyExerciseCompletion()
        stage = GuidedSessionStage.COMPLETED
        isCompleted = true
    }

    override fun onCleared() {
        sessionJob?.cancel()
    }

    companion object {
        private const val COUNTDOWN_START = 5
        private const val BREATHING_DURATION_MS = 3 * 60_000L
        private const val TRANSITION_DURATION_MS = 5_000L

        // Länger als die Pause vor dem Grounding, damit die ca. 8-9 Sekunden
        // lange PMR-Einleitung (pm_einleitung) vollständig abspielen kann,
        // bevor die erste Anspannen-Phase beginnt.
        private const val PMR_TRANSITION_DURATION_MS = 9_000L

        private const val DISTRACTION_ITEM_DURATION_MS = 30_000L
        const val DISTRACTION_ITEM_COUNT = 5
        private const val PMR_DURATION_MS = 3 * 60_000L

        private fun randomDistractionContent(): DistractionContent =
            DistractionRepository.getContentAt(DistractionRepository.randomContentIndex())
    }
}
