package com.example.kidsgames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.kidsgames.framework.AppRoot
import com.example.kidsgames.framework.rememberGameServices
import com.example.kidsgames.ui.theme.KidsGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KidsGameTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(services = rememberGameServices())
                }
            }
        }
    }
}
