package com.example.panichelper.ui.screens.distraction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.panichelper.data.DistractionContent
import com.example.panichelper.data.DistractionRepository

/**
 * Verwaltet den Zustand des DistractionScreens.
 * Rotiert sowohl durch Bilder (mit Beobachtungsfrage) als auch durch
 * reine Text-Prompts (Grounding-Übungen ohne Bild).
 */
class DistractionViewModel : ViewModel() {

    // Aktueller Gesamt-Index über alle Inhalte (Bilder + Text-Prompts).
    private var currentIndex by mutableIntStateOf(0)

    // Aktuell angezeigter Inhalt, wird bei jedem Wechsel neu erzeugt.
    var currentContent: DistractionContent by mutableStateOf(
        DistractionRepository.getContentAt(0)
    )
        private set

    /**
     * Wechselt zum nächsten Inhalt in der Reihenfolge (zyklisch durch
     * Bilder und Text-Prompts hinweg).
     */
    fun showNextContent() {
        val nextIndex = (currentIndex + 1) % DistractionRepository.totalContentCount()
        currentIndex = nextIndex
        currentContent = DistractionRepository.getContentAt(nextIndex)
    }

    /**
     * Wechselt zu einem zufälligen Inhalt (Bild oder Text-Prompt).
     */
    fun showRandomContent() {
        val randomIndex = DistractionRepository.randomContentIndex()
        currentIndex = randomIndex
        currentContent = DistractionRepository.getContentAt(randomIndex)
    }
}