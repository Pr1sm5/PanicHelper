package com.example.panichelper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panichelper.navigation.PanicHelperNavGraph
import com.example.panichelper.ui.theme.PanicHelperTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanicHelperApp()
        }
    }
}

@Composable
fun PanicHelperApp() {
    PanicHelperTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PanicHelperNavGraph()
        }
    }
}