package com.example.panichelper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EnsoDarkColors = darkColorScheme(
    primary = EnsoSage,
    onPrimary = EnsoBackgroundDeep,
    primaryContainer = EnsoSurfaceSoft,
    onPrimaryContainer = EnsoTextPrimary,
    secondary = EnsoBlueGray,
    onSecondary = EnsoBackgroundDeep,
    secondaryContainer = EnsoCard,
    onSecondaryContainer = EnsoTextPrimary,
    tertiary = EnsoSageStrong,
    onTertiary = EnsoBackgroundDeep,
    background = EnsoBackground,
    onBackground = EnsoTextPrimary,
    surface = EnsoSurface,
    onSurface = EnsoTextPrimary,
    surfaceVariant = EnsoCard,
    onSurfaceVariant = EnsoTextSecondary,
    outline = EnsoOutline,
    error = EnsoHelp,
    onError = EnsoBackgroundDeep,
    errorContainer = EnsoHelpContainer,
    onErrorContainer = EnsoTextPrimary
)

@Composable
fun PanicHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> EnsoDarkColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
