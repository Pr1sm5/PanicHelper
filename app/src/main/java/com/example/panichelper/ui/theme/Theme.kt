package com.example.panichelper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// Helles Farbschema (Standard für PanicHelper - bewusst kein Dark Theme
// forciert, da helle, ruhige Farben Teil des Beruhigungskonzepts sind).
// ---------------------------------------------------------------------------
private val LightColors = lightColorScheme(
    primary = TurquoisePrimary,
    onPrimary = White,
    primaryContainer = TurquoiseLight,
    onPrimaryContainer = TextPrimary,
    secondary = SkyBlue,
    onSecondary = White,
    secondaryContainer = SkyBlueLight,
    onSecondaryContainer = TextPrimary,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = GraySoft,
    onSurfaceVariant = TextSecondary,
    error = SoftError,
    onError = White
)

// Dunkles Farbschema als Fallback, falls Systemeinstellung Dark Mode erzwingt.
private val DarkColors = darkColorScheme(
    primary = TurquoiseDark,
    onPrimary = White,
    primaryContainer = TurquoiseDark,
    onPrimaryContainer = White,
    secondary = SkyBlueDark,
    onSecondary = White,
    background = Color(0xFF1B2224).let { it }, // dezentes, ruhiges Dunkelgrau
    onBackground = White,
    surface = Color(0xFF23292B),
    onSurface = White
)

@Composable
fun PanicHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamische Farben bewusst deaktiviert, um das ruhige Farbkonzept
    // konsistent zu halten (kein Einfluss durch Wallpaper-Farben).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}