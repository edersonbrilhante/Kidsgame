package com.example.kidsgames.framework

import com.example.kidsgames.feature.colors.ColorsShapesMiniGame
import com.example.kidsgames.feature.findit.FindItMiniGame
import com.example.kidsgames.feature.jigsaw.JigsawMiniGame
import com.example.kidsgames.feature.letters.LettersMiniGame
import com.example.kidsgames.feature.numbers.NumbersMiniGame
import com.example.kidsgames.feature.tapballoon.TapBalloonMiniGame

/**
 * Single source of truth for available minigames.
 * ADD A NEW GAME HERE (one line) after implementing [MiniGame] in its own feature package.
 */
object MiniGameRegistry {
    val games: List<MiniGame> = listOf(
        JigsawMiniGame(),
        NumbersMiniGame(),
        LettersMiniGame(),
        ColorsShapesMiniGame(),
        FindItMiniGame(),
        TapBalloonMiniGame(),
    )

    fun byId(id: String): MiniGame = games.first { it.info.id == id }
}
