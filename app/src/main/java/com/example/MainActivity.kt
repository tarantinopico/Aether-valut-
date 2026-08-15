package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.repository.VaultRepository
import com.example.ui.navigation.AetherApp
import com.example.ui.theme.AetherTheme
import com.example.ui.theme.AetherVoid

class MainActivity : ComponentActivity() {

    private lateinit var vaultRepository: VaultRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        vaultRepository = VaultRepository(applicationContext)

        setContent {
            AetherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AetherVoid
                ) {
                    AetherApp(vaultRepository = vaultRepository)
                }
            }
        }
    }
}
