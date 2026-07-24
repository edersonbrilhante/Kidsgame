package com.example.matteogames.framework

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.matteogames.ui.theme.SkyBlue
import com.example.matteogames.ui.theme.Grape

/** Static metadata shown on the home carousel. */
data class MiniGameInfo(
    val id: String,
    @StringRes val titleRes: Int,
    /** Optional drawable icon; ignored when [emoji] is set. */
    @DrawableRes val iconRes: Int = 0,
    /** Big emoji shown on the card (preferred over [iconRes] when present). */
    val emoji: String? = null,
    val minAgeMonths: Int = 36,
    /** Two-stop gradient that gives each game card its own color. */
    val gradient: List<Color> = listOf(SkyBlue, Grape),
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
