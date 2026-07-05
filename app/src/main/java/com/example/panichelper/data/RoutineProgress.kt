package com.example.panichelper.data

import kotlinx.serialization.Serializable

/**
 * Fortschritt der täglichen Übungsroutine.
 * @param lastCompletedEpochDay Letzter abgeschlossener Tag als LocalDate.toEpochDay(), oder null.
 */
@Serializable
data class RoutineProgress(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedEpochDay: Long? = null
)

/**
 * Berechnet den neuen Fortschritt nach Abschluss einer Übung an [todayEpochDay].
 * - Gleicher Tag wie zuletzt: unverändert (heute schon abgeschlossen).
 * - Vortag war der letzte abgeschlossene Tag: Streak wird fortgesetzt (+1).
 * - Sonst (Lücke oder erste Übung): Streak startet neu bei 1.
 */
fun RoutineProgress.withCompletionRecorded(todayEpochDay: Long): RoutineProgress {
    val newStreak = when (lastCompletedEpochDay) {
        todayEpochDay -> currentStreak
        todayEpochDay - 1 -> currentStreak + 1
        else -> 1
    }
    return copy(
        currentStreak = newStreak,
        longestStreak = maxOf(longestStreak, newStreak),
        lastCompletedEpochDay = todayEpochDay
    )
}
