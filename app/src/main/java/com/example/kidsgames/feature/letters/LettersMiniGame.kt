package com.example.kidsgames.feature.letters

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
import com.example.kidsgames.core.EN
import com.example.kidsgames.core.PL
import com.example.kidsgames.core.PT
import com.example.kidsgames.core.SpeechService
import com.example.kidsgames.core.Word
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidCircleButton
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Coral
import com.example.kidsgames.ui.theme.Tangerine

private data class LetterCard(val letter: String, val word: Word)

private val LETTERS = listOf(
    LetterCard("A", Word("apple", "jabłko", "maçã", "🍎")),
    LetterCard("B", Word("ball", "piłka", "bola", "⚽")),
    LetterCard("C", Word("cat", "kot", "gato", "🐱")),
    LetterCard("D", Word("dog", "pies", "cachorro", "🐶")),
    LetterCard("E", Word("elephant", "słoń", "elefante", "🐘")),
    LetterCard("F", Word("fish", "ryba", "peixe", "🐟")),
    LetterCard("G", Word("grapes", "winogrona", "uvas", "🍇")),
    LetterCard("H", Word("house", "dom", "casa", "🏠")),
    LetterCard("I", Word("ice", "lód", "gelo", "🧊")),
    LetterCard("J", Word("juice", "sok", "suco", "🧃")),
    LetterCard("K", Word("key", "klucz", "chave", "🔑")),
    LetterCard("L", Word("lion", "lew", "leão", "🦁")),
    LetterCard("M", Word("mouse", "mysz", "rato", "🐭")),
    LetterCard("N", Word("nose", "nos", "nariz", "👃")),
    LetterCard("O", Word("orange", "pomarańcza", "laranja", "🍊")),
    LetterCard("P", Word("pig", "świnia", "porco", "🐷")),
    LetterCard("Q", Word("queen", "królowa", "rainha", "👑")),
    LetterCard("R", Word("rabbit", "królik", "coelho", "🐰")),
    LetterCard("S", Word("sun", "słońce", "sol", "☀️")),
    LetterCard("T", Word("tree", "drzewo", "árvore", "🌳")),
    LetterCard("U", Word("umbrella", "parasol", "guarda-chuva", "☂️")),
    LetterCard("V", Word("violin", "skrzypce", "violino", "🎻")),
    LetterCard("W", Word("water", "woda", "água", "💧")),
    LetterCard("X", Word("fox", "lis", "raposa", "🦊")),
    LetterCard("Y", Word("yo-yo", "jo-jo", "ioiô", "🪀")),
    LetterCard("Z", Word("zebra", "zebra", "zebra", "🦓")),
)

private fun speakCard(services: GameServices, card: LetterCard) {
    services.speech.speakSequence(
        listOf(
            SpeechService.Utterance(card.letter, EN),
            SpeechService.Utterance(card.word.en, EN),
            SpeechService.Utterance(card.word.pl, PL),
            SpeechService.Utterance(card.word.pt, PT),
        )
    )
}

class LettersMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "letters",
        titleRes = R.string.game_letters,
        emoji = "🔤",
        gradient = listOf(Tangerine, Coral),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var i by remember { mutableIntStateOf(0) }
        val card = LETTERS[i]

        LaunchedEffect(i) { speakCard(services, card) }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFFFF0E4), Color(0xFFFFE7EC))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Big letter — tap to hear the letter and the word.
                Surface(
                    onClick = { speakCard(services, card) },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(180.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            card.letter,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(card.word.emoji, fontSize = 88.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    card.word.en,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(28.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KidCircleButton(
                        onClick = { i = (i - 1 + LETTERS.size) % LETTERS.size },
                        glyph = "◀",
                        size = 72,
                    )
                    KidCircleButton(
                        onClick = { i = (i + 1) % LETTERS.size },
                        glyph = "▶",
                        size = 72,
                    )
                }
            }
        }
    }
}
