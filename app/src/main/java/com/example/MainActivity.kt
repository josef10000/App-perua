package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.MainScreen
import com.example.ui.theme.RotaEscolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.e("RotaEscolar", "enableEdgeToEdge failed", e)
        }
        setContent {
            RotaEscolarTheme {
                MainScreen()
            }
        }
    }
}
