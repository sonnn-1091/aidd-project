package com.example.aiddproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.navigation.AppNavigation
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIDDProjectTheme {
                LanguageProvider {
                    AppNavigation()
                }
            }
        }
    }
}
