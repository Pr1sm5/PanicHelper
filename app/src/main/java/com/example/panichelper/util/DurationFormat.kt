package com.example.panichelper.util

/** Formatiert eine Restzeit in Millisekunden als "m:ss", z. B. "4:05". */
fun formatRemainingTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutesPart = totalSeconds / 60
    val secondsPart = totalSeconds % 60
    return "%d:%02d".format(minutesPart, secondsPart)
}
