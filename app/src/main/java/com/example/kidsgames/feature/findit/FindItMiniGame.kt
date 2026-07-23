package com.example.kidsgames.feature.findit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.R
import com.example.kidsgames.core.EN
import com.example.kidsgames.core.PL
import com.example.kidsgames.core.PT
import com.example.kidsgames.core.Phrases
import com.example.kidsgames.core.SpeechService
import com.example.kidsgames.core.Word
import com.example.kidsgames.core.utterances
import com.example.kidsgames.feature.jigsaw.PuzzleLogic
import com.example.kidsgames.feature.jigsaw.SamplePicture
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Aqua
import com.example.kidsgames.ui.theme.Grass
import java.util.Locale

private data class FindRound(
    val options: List<SamplePicture>,
    val correctIndex: Int,
    val prompt: String,
    val locale: Locale,
)

private fun newRound(): FindRound {
    val options = PuzzleLogic.samples.shuffled().take(4)
    val target = options.indices.random()
    val pic = options[target]
    // Rotate which language he must recognise, so he hears all three over time.
    val (locale, text) = listOf(EN to pic.en, PL to pic.pl, PT to pic.pt).random()
    return FindRound(options, target, text, locale)
}

private fun SamplePicture.word() = Word(en, pl, pt)

class FindItMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "findit",
        titleRes = R.string.game_findit,
        emoji = "🔎",
        gradient = listOf(Aqua, Grass),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var round by remember { mutableStateOf(newRound()) }
        var cheerStep by remember { mutableIntStateOf(0) }

        // Speak the first word to find; later prompts are spoken from onCorrect below.
        LaunchedEffect(Unit) { services.speech.speak(round.prompt, round.locale) }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFE6FBF3), Color(0xFFEAF7EA))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Tap the speaker to hear the word again.
                Surface(
                    onClick = { services.speech.speak(round.prompt, round.locale) },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Text("🔊", fontSize = 56.sp, modifier = Modifier.padding(20.dp))
                }

                for (r in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (c in 0..1) {
                            val index = r * 2 + c
                            val pic = round.options[index]
                            PictureTile(emoji = pic.emoji, sizeDp = 140) {
                                if (index == round.correctIndex) {
                                    services.audio.playCorrect()
                                    val cheer = Phrases.cheer(cheerStep)
                                    cheerStep++
                                    val next = newRound()
                                    // Confirm the word (3 languages) + cheer, then the next prompt.
                                    services.speech.speakSequence(
                                        pic.word().utterances() + cheer.utterances() +
                                            listOf(SpeechService.Utterance(next.prompt, next.locale))
                                    )
                                    round = next
                                } else {
                                    services.speech.speak(round.prompt, round.locale)
                                }
                            }
                        }
                    }
                    if (r == 0) Box(Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PictureTile(emoji: String, sizeDp: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.size(sizeDp.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 72.sp)
        }
    }
}
