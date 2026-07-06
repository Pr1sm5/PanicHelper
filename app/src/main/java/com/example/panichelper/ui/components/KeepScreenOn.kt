package com.example.panichelper.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Verhindert, dass der Bildschirm während einer laufenden Übung ausgeht bzw.
 * gesperrt wird. Ohne das kann das Gerät je nach Anzeige-Timeout mitten in
 * einer Sitzung in Standby gehen - dabei pausiert das System nicht nur den
 * Bildschirm, sondern nach kurzer Zeit auch die CPU, was Timer und die
 * Hintergrundmusik-Wiedergabe mitten in der Übung stoppen lässt.
 */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
