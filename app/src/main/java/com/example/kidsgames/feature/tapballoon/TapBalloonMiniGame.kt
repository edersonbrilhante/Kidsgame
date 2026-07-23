package com.example.kidsgames.feature.tapballoon

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.R
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidCircleButton
import com.example.kidsgames.framework.KidBackground
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Bubblegum
import com.example.kidsgames.ui.theme.SkyTop
import com.example.kidsgames.ui.theme.Tangerine
import kotlin.random.Random

/**
 * A deliberately tiny second game. It exists to prove the framework: adding it required
 * only this file plus one line in MiniGameRegistry. Use it as a template for new games.
 */
class TapBalloonMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "tapballoon",
        titleRes = R.string.game_tapballoon,
        iconRes = R.drawable.ic_balloon,
        gradient = listOf(Bubblegum, Tangerine),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        val colors = remember {
            listOf(Color(0xFFEF5DA8), Color(0xFF5A8DEE), Color(0xFF57C785), Color(0xFFFFC23C))
        }
        // Themed things to pop: balloon, soccer ball, star, dragon, hero.
        val glyphs = remember { listOf("🎈", "⚽", "⭐", "🐲", "🦸") }
        var color by remember { mutableStateOf(colors.random()) }
        var glyph by remember { mutableStateOf(glyphs.random()) }
        var fraction by remember { mutableStateOf(0.3f to 0.3f) }

        // Gentle continuous bob so the balloon feels alive.
        val bob = rememberInfiniteTransition(label = "bob")
        val bobScale by bob.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "bobScale",
        )

        KidBackground(colors = listOf(SkyTop, Color(0xFFFFE7F1))) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                // Smoothly glide to each new spot instead of teleporting.
                val targetX = (maxWidth - 140.dp) * fraction.first
                val targetY = (maxHeight - 140.dp) * fraction.second
                val x by animateDpAsState(targetX, tween(450), label = "balloonX")
                val y by animateDpAsState(targetY, tween(450), label = "balloonY")
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(140.dp)
                        .scale(bobScale)
                        .clip(CircleShape)
                        .background(color)
                        .clickable {
                            services.audio.playCorrect()
                            color = colors.random()
                            glyph = glyphs.random()
                            fraction = Random.nextFloat() to Random.nextFloat()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(glyph, fontSize = 64.sp)
                }
            }
            KidCircleButton(
                onClick = onExit,
                glyph = "\u25C0",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
