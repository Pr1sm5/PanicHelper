package com.example.panichelper.ui.screens.breathing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BreathingViewModel : ViewModel() {

    var selectedTechnique by mutableStateOf(BreathingTechniques.DEFAULT)
        private set

    var currentPhase by mutableStateOf(BreathingTechniques.DEFAULT.phases.first())
        private set

    var completedCycles by mutableIntStateOf(0)
        private set

    var vibrationIntensity by mutableStateOf(0.7f)
        private set

    private var cycleJob: Job? = null

    init {
        startCycle()
    }

    fun setTechnique(technique: BreathingTechnique) {
        if (technique == selectedTechnique) return
        selectedTechnique = technique
        completedCycles = 0
        cycleJob?.cancel()
        startCycle()
    }

    fun updateVibrationIntensity(intensity: Float) {
        vibrationIntensity = intensity
    }

    private fun startCycle() {
        cycleJob = viewModelScope.launch {
            while (true) {
                for (phase in selectedTechnique.phases) {
                    currentPhase = phase
                    delay(phase.durationMillis)
                }
                completedCycles++
            }
        }
    }
}
