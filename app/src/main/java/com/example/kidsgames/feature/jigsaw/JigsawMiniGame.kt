package com.example.kidsgames.feature.jigsaw

import androidx.compose.runtime.Composable
import com.example.kidsgames.R
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Aqua
import com.example.kidsgames.ui.theme.SkyBlue

class JigsawMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "jigsaw",
        titleRes = R.string.game_jigsaw,
        iconRes = R.drawable.ic_jigsaw,
        gradient = listOf(SkyBlue, Aqua),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        JigsawScreen(services = services, onExit = onExit)
    }
}
