package com.example.kidsgames.feature.tapballoon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.R
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
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
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        val colors = remember {
            listOf(Color(0xFFEF5DA8), Color(0xFF5A8DEE), Color(0xFF57C785), Color(0xFFFFC23C))
        }
        var color by remember { mutableStateOf(colors.random()) }
        var fraction by remember { mutableStateOf(0.3f to 0.3f) }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Button(onClick = onExit, modifier = Modifier.padding(12.dp)) {
                Text("\u25C0", fontSize = 20.sp)
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val x = (maxWidth - 140.dp) * fraction.first
                val y = (maxHeight - 140.dp) * fraction.second
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable {
                            services.audio.playCorrect()
                            color = colors.random()
                            fraction = Random.nextFloat() to Random.nextFloat()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("\uD83C\uDF88", fontSize = 56.sp)
                }
            }
        }
    }
}
