package com.example.panichelper.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Farbpalette für PanicHelper
// Bewusst ruhig gehalten: Weiß, Hellblau, Türkis, dezente Grautöne.
// Keine aggressiven / grellen Farben, um Beruhigung zu unterstützen.
// ---------------------------------------------------------------------------

// Primärfarben (Türkis-Töne)
val TurquoisePrimary = Color(0xFF4FB3AC)
val TurquoiseDark = Color(0xFF2E8B85)
val TurquoiseLight = Color(0xFFB2DFDB)

// Sekundärfarben (Hellblau-Töne)
val SkyBlue = Color(0xFF90CAF9)
val SkyBlueLight = Color(0xFFE3F2FD)
val SkyBlueDark = Color(0xFF5C99C6)

// Neutrale Töne
val White = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF7FAFA)
val GraySoft = Color(0xFFECEFF1)
val GrayMedium = Color(0xFFB0BEC5)
val GrayDark = Color(0xFF546E7A)
val TextPrimary = Color(0xFF37474F)
val TextSecondary = Color(0xFF78909C)

// Atemübung – Farbverlauf während der Animation (hell -> dunkel)
val BreathingLight = Color(0xFFB2EBF2)
val BreathingDark = Color(0xFF26A69A)

// Fehler-/Hinweisfarbe (dezent, nicht aggressiv)
val SoftError = Color(0xFFE57373)

// Auswählbare Akzentfarben für das lokale Profil im täglichen Übungsmodus.
val ProfileColorPalette = listOf(
    Color(0xFFEF5350),
    Color(0xFFAB47BC),
    Color(0xFF5C6BC0),
    Color(0xFF29B6F6),
    Color(0xFF66BB6A),
    Color(0xFFFFCA28),
    Color(0xFFFF7043),
    Color(0xFF8D6E63)
)