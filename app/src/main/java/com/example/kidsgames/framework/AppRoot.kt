package com.example.kidsgames.framework

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private sealed interface Screen {
    data object Home : Screen
    data class Game(val id: String) : Screen
}

@Composable
fun AppRoot(services: GameServices) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val current = screen) {
        Screen.Home -> HomeScreen(onSelect = { id -> screen = Screen.Game(id) })
        is Screen.Game -> {
            BackHandler { screen = Screen.Home }
            // Stop any speech when this game leaves the screen (back or switching games).
            DisposableEffect(current.id) {
                onDispose { services.speech.stop() }
            }
            val game = MiniGameRegistry.byId(current.id)
            game.Screen(services = services, onExit = { screen = Screen.Home })
        }
    }
}
