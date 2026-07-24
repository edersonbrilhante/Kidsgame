package com.example.matteogames.feature.jigsaw

import androidx.compose.runtime.Composable
import com.example.matteogames.R
import com.example.matteogames.framework.GameServices
import com.example.matteogames.framework.MiniGame
import com.example.matteogames.framework.MiniGameInfo
import com.example.matteogames.ui.theme.Aqua
import com.example.matteogames.ui.theme.SkyBlue

class JigsawMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "jigsaw",
        titleRes = R.string.game_jigsaw,
        iconRes = R.drawable.ic_jigsaw,
        emoji = "🧩",
        gradient = listOf(SkyBlue, Aqua),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        JigsawScreen(services = services, onExit = onExit)
    }
}
