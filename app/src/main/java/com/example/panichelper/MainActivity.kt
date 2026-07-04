package com.example.panichelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panichelper.navigation.PanicHelperNavGraph
import com.example.panichelper.ui.theme.PanicHelperTheme

/**
 * Einziger Einstiegspunkt der App (Single-Activity-Architektur).
 * Die gesamte Navigation wird über Jetpack Navigation Compose
 * innerhalb dieser Activity abgewickelt.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanicHelperApp()
        }
    }
}

/**
 * Root-Composable der App.
 * Wendet das App-Theme an und hostet den Navigationsgraphen.
 */
@Composable
fun PanicHelperApp() {
    PanicHelperTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PanicHelperNavGraph()
        }
    }
}