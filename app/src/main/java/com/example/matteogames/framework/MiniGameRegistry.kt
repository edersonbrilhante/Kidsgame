package com.example.matteogames.framework

import com.example.matteogames.feature.colors.ColorsShapesMiniGame
import com.example.matteogames.feature.counting.CountingMiniGame
import com.example.matteogames.feature.findit.FindItMiniGame
import com.example.matteogames.feature.jigsaw.JigsawMiniGame
import com.example.matteogames.feature.letters.LettersMiniGame
import com.example.matteogames.feature.memory.MemoryMatchMiniGame
import com.example.matteogames.feature.numbers.NumbersMiniGame
import com.example.matteogames.feature.tapballoon.TapBalloonMiniGame

/**
 * Single source of truth for available minigames.
 * ADD A NEW GAME HERE (one line) after implementing [MiniGame] in its own feature package.
 */
object MiniGameRegistry {
    val games: List<MiniGame> = listOf(
        JigsawMiniGame(),
        NumbersMiniGame(),
        LettersMiniGame(),
        CountingMiniGame(),
        ColorsShapesMiniGame(),
        MemoryMatchMiniGame(),
        FindItMiniGame(),
        TapBalloonMiniGame(),
    )

    fun byId(id: String): MiniGame = games.first { it.info.id == id }
}
