package com.example.kidsgames.feature.numbers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.R
import com.example.kidsgames.core.Word
import com.example.kidsgames.core.say
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidCircleButton
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Grape
import com.example.kidsgames.ui.theme.SkyBlue

private val NUMBERS = listOf(
    Word("one", "jeden", "um"),
    Word("two", "dwa", "dois"),
    Word("three", "trzy", "três"),
    Word("four", "cztery", "quatro"),
    Word("five", "pięć", "cinco"),
    Word("six", "sześć", "seis"),
    Word("seven", "siedem", "sete"),
    Word("eight", "osiem", "oito"),
    Word("nine", "dziewięć", "nove"),
    Word("ten", "dziesięć", "dez"),
)

class NumbersMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "numbers",
        titleRes = R.string.game_numbers,
        emoji = "🔢",
        gradient = listOf(Grape, SkyBlue),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var n by remember { mutableIntStateOf(1) }
        val objects = remember { listOf("⚽", "⚡", "🐶", "⭐", "🍎") }

        // Count it out loud in all three languages whenever the number changes.
        LaunchedEffect(n) { services.speech.say(NUMBERS[n - 1]) }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFEDE7FF), Color(0xFFEAF2FF))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Big number — tap to hear it again.
                Surface(
                    onClick = { services.speech.say(NUMBERS[n - 1]) },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(180.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$n",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // That many objects, arranged in rows of five.
                val emoji = objects[(n - 1) % objects.size]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..n).toList().chunked(5).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { _ -> Text(emoji, fontSize = 40.sp) }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KidCircleButton(onClick = { if (n > 1) n-- }, glyph = "◀", size = 72)
                    KidCircleButton(onClick = { if (n < 10) n++ }, glyph = "▶", size = 72)
                }
            }
        }
    }
}
