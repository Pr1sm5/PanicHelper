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
 * Zeigt ausschließlich zufällige Inhalte (Bilder mit Beobachtungsfrage oder
 * Text-Prompts) - auch der allererste Inhalt beim Öffnen des Screens ist
 * bereits zufällig, damit sich nichts wiederholend/vorhersehbar anfühlt.
 */
class DistractionViewModel : ViewModel() {

    // Aktueller Gesamt-Index über alle Inhalte (Bilder + Text-Prompts).
    // Öffentlich lesbar, damit die UI z.B. einen Fortschritts-Indikator
    // (Dots) anzeigen kann.
    var currentIndex by mutableIntStateOf(DistractionRepository.randomContentIndex())
        private set

    // Aktuell angezeigter Inhalt, wird bei jedem Wechsel neu erzeugt.
    var currentContent: DistractionContent by mutableStateOf(
        DistractionRepository.getContentAt(currentIndex)
    )
        private set

    /** Gesamtanzahl aller rotierenden Inhalte (für den Dot-Indikator). */
    val totalCount: Int get() = DistractionRepository.totalContentCount()

    /** Wechselt zu einem neuen, zufälligen Inhalt (Bild oder Text-Prompt). */
    fun showNextContent() {
        val randomIndex = DistractionRepository.randomContentIndex()
        currentIndex = randomIndex
        currentContent = DistractionRepository.getContentAt(randomIndex)
    }
}
