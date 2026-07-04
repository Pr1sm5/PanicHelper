package com.example.panichelper.ui.screens.distraction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.panichelper.data.DistractionRepository

/**
 * Verwaltet den Zustand des DistractionScreens:
 * aktuelles Bild (per Index) und die aktuell angezeigte Beobachtungsfrage.
 */
class DistractionViewModel : ViewModel() {

    // Index des aktuell angezeigten Bildes innerhalb der Repository-Liste.
    var currentImageIndex by mutableIntStateOf(0)
        private set

    // Aktuell angezeigte Beobachtungsfrage unter dem Bild.
    var currentQuestion by mutableStateOf(DistractionRepository.randomQuestion())
        private set

    /**
     * Wechselt zum nächsten Bild in der Liste (zyklisch) und
     * wählt eine neue zufällige Beobachtungsfrage.
     */
    fun showNextImage() {
        val nextIndex = (currentImageIndex + 1) % DistractionRepository.imageCount()
        currentImageIndex = nextIndex
        currentQuestion = DistractionRepository.randomQuestion()
    }

    /**
     * Wechselt zu einem zufälligen Bild aus der Liste und
     * wählt eine neue zufällige Beobachtungsfrage.
     */
    fun showRandomImage() {
        currentImageIndex = DistractionRepository.randomImageIndex()
        currentQuestion = DistractionRepository.randomQuestion()
    }
}