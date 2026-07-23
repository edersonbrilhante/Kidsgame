package com.example.kidsgames.framework

import com.example.kidsgames.feature.jigsaw.JigsawMiniGame
import com.example.kidsgames.feature.tapballoon.TapBalloonMiniGame

/**
 * Single source of truth for available minigames.
 * ADD A NEW GAME HERE (one line) after implementing [MiniGame] in its own feature package.
 */
object MiniGameRegistry {
    val games: List<MiniGame> = listOf(
        JigsawMiniGame(),
        TapBalloonMiniGame(),
    )

    fun byId(id: String): MiniGame = games.first { it.info.id == id }
}
