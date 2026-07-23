package com.example.kidsgames.framework

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

/** Static metadata shown on the home carousel. */
data class MiniGameInfo(
    val id: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val minAgeMonths: Int = 36,
)

/**
 * A minigame is a self-contained feature.
 * To add a new game: implement this interface and register it in [MiniGameRegistry].
 */
interface MiniGame {
    val info: MiniGameInfo

    /** The full-screen entry point for this game. */
    @Composable
    fun Screen(services: GameServices, onExit: () -> Unit)
}
