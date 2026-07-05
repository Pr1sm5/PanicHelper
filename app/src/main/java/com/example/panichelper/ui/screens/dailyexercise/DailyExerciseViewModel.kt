package com.example.panichelper.ui.screens.dailyexercise

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panichelper.data.DailyExerciseDuration
import com.example.panichelper.data.RoutineProgress
import com.example.panichelper.data.UserProfile
import com.example.panichelper.ui.screens.breathing.BreathingTechnique
import com.example.panichelper.ui.screens.breathing.BreathingTechniques
import com.example.panichelper.util.PreferencesManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Verwaltet das lokale Profil, den Routine-Fortschritt und die gewählte
 * Übungslänge für die Übersicht des täglichen Übungsmodus.
 */
class DailyExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var routineProgress by mutableStateOf(RoutineProgress())
        private set

    var selectedDuration by mutableStateOf(DailyExerciseDuration.FIVE)
        private set

    var selectedTechnique by mutableStateOf(BreathingTechniques.DEFAULT)
        private set

    var selectedPmrDuration by mutableStateOf(DailyExerciseDuration.FIVE)
        private set

    init {
        preferencesManager.userProfile
            .onEach { profile = it }
            .launchIn(viewModelScope)

        preferencesManager.routineProgress
            .onEach { routineProgress = it }
            .launchIn(viewModelScope)
    }

    /** Speichert (oder aktualisiert) das lokale Profil. */
    fun saveProfile(username: String, colorArgb: Int) {
        viewModelScope.launch {
            preferencesManager.saveUserProfile(UserProfile(username, colorArgb))
        }
    }

    /** Wählt die Übungslänge für die nächste Sitzung. */
    fun selectDuration(duration: DailyExerciseDuration) {
        selectedDuration = duration
    }

    /** Wählt die Atemtechnik für die nächste Sitzung. */
    fun selectTechnique(technique: BreathingTechnique) {
        selectedTechnique = technique
    }

    /** Wählt die Übungslänge für die nächste PMR-Sitzung. */
    fun selectPmrDuration(duration: DailyExerciseDuration) {
        selectedPmrDuration = duration
    }
}
