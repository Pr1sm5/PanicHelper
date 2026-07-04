package com.example.panichelper.ui.screens.breathing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Repräsentiert die aktuelle Phase der Atemübung.
 */
enum class BreathingPhase(val label: String, val durationMillis: Long) {
    INHALE(label = "Einatmen", durationMillis = 4000L),
    HOLD(label = "Halten", durationMillis = 4000L),
    EXHALE(label = "Ausatmen", durationMillis = 6000L)
}

/**
 * Steuert den zeitlichen Ablauf der Atemübung (4-4-6 Sekunden-Zyklus).
 * Die UI (BreathingScreen) beobachtet lediglich die aktuelle Phase und
 * animiert Kreisgröße/Farbe entsprechend selbst mittels Compose Animation APIs.
 */
class BreathingViewModel : ViewModel() {

    // Aktuelle Phase der Atemübung, von der UI beobachtet.
    var currentPhase by mutableStateOf(BreathingPhase.INHALE)
        private set

    init {
        startCycle()
    }

    /**
     * Startet den endlosen Atemzyklus: Einatmen -> Halten -> Ausatmen -> von vorne.
     * Läuft in viewModelScope, damit die Coroutine automatisch beendet wird,
     * sobald das ViewModel (und damit der Screen) zerstört wird.
     */
    private fun startCycle() {
        viewModelScope.launch {
            while (true) {
                currentPhase = BreathingPhase.INHALE
                delay(BreathingPhase.INHALE.durationMillis)

                currentPhase = BreathingPhase.HOLD
                delay(BreathingPhase.HOLD.durationMillis)

                currentPhase = BreathingPhase.EXHALE
                delay(BreathingPhase.EXHALE.durationMillis)
            }
        }
    }
}