package com.example.kidsgames.feature.counting

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Grass
import com.example.kidsgames.ui.theme.Sunshine

private val NUMBER_WORDS = listOf(
    Word("one", "jeden", "um"),
    Word("two", "dwa", "dois"),
    Word("three", "trzy", "três"),
    Word("four", "cztery", "quatro"),
    Word("five", "pięć", "cinco"),
)

private data class CountRound(val n: Int, val emoji: String, val choices: List<Int>)

private fun newRound(): CountRound {
    val n = (1..5).random()
    val emoji = listOf("⚽", "🍎", "⭐", "🐶", "🎈", "🐱", "🐟").random()
    val distractors = ((1..5).toList() - n).shuffled().take(2)
    val choices = (distractors + n).shuffled()
    return CountRound(n, emoji, choices)
}

class CountingMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "counting",
        titleRes = R.string.game_counting,
        emoji = "🔟",
        gradient = listOf(Sunshine, Grass),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var round by remember { mutableStateOf(newRound()) }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFFFF6DE), Color(0xFFE9F7E6))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "How many?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(20.dp))

                // The objects to count.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..round.n).toList().chunked(3).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { _ -> Text(round.emoji, fontSize = 52.sp) }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Number choices.
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    round.choices.forEach { choice ->
                        Surface(
                            onClick = {
                                if (choice == round.n) {
                                    services.audio.playCorrect()
                                    services.speech.say(NUMBER_WORDS[round.n - 1])
                                    round = newRound()
                                }
                            },
                            shape = MaterialTheme.shapes.extraLarge,
                            color = Color.White,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(96.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "$choice",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
