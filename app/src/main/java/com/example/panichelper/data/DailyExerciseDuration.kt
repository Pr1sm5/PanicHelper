package com.example.panichelper.data

/** Auswählbare Länge einer täglichen Übungssitzung. */
enum class DailyExerciseDuration(val minutes: Int, val label: String) {
    THREE(3, "3 Min"),
    FIVE(5, "5 Min"),
    TEN(10, "10 Min")
}
