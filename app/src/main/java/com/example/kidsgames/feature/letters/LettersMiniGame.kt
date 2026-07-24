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
import com.example.kidsgames.core.EN
import com.example.kidsgames.core.PL
import com.example.kidsgames.core.PT
import com.example.kidsgames.core.SpeechService
import com.example.kidsgames.core.Word
import com.example.kidsgames.feature.jigsaw.VOCAB
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidCircleButton
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Coral
import com.example.kidsgames.ui.theme.Sunshine
import com.example.kidsgames.ui.theme.Tangerine
import java.util.Locale

// One guaranteed word per letter (covers letters the vocabulary may lack, e.g. Q, X, Y).
private val BASE: List<Pair<Char, Word>> = listOf(
    'A' to Word("apple", "jabłko", "maçã", "🍎"),
    'B' to Word("ball", "piłka", "bola", "⚽"),
    'C' to Word("cat", "kot", "gato", "🐱"),
    'D' to Word("dog", "pies", "cachorro", "🐶"),
    'E' to Word("elephant", "słoń", "elefante", "🐘"),
    'F' to Word("fish", "ryba", "peixe", "🐟"),
    'G' to Word("grapes", "winogrona", "uvas", "🍇"),
    'H' to Word("house", "dom", "casa", "🏠"),
    'I' to Word("ice", "lód", "gelo", "🧊"),
    'J' to Word("juice", "sok", "suco", "🧃"),
    'K' to Word("key", "klucz", "chave", "🔑"),
    'L' to Word("lion", "lew", "leão", "🦁"),
    'M' to Word("mouse", "mysz", "rato", "🐭"),
    'N' to Word("nose", "nos", "nariz", "👃"),
    'O' to Word("orange", "pomarańcza", "laranja", "🍊"),
    'P' to Word("pig", "świnia", "porco", "🐷"),
    'Q' to Word("queen", "królowa", "rainha", "👑"),
    'R' to Word("rabbit", "królik", "coelho", "🐰"),
    'S' to Word("sun", "słońce", "sol", "☀️"),
    'T' to Word("tree", "drzewo", "árvore", "🌳"),
    'U' to Word("umbrella", "parasol", "guarda-chuva", "☂️"),
    'V' to Word("violin", "skrzypce", "violino", "🎻"),
    'W' to Word("water", "woda", "água", "💧"),
    'X' to Word("fox", "lis", "raposa", "🦊"),
    'Y' to Word("yo-yo", "jo-jo", "ioiô", "🪀"),
    'Z' to Word("zebra", "zebra", "zebra", "🦓"),
)

/**
 * For each letter A–Z, a pool of words = the curated base word plus every vocabulary
 * word that starts with that letter. A random one is shown each time the letter is
 * selected (grows automatically as the vocabulary grows).
 */
private val LETTER_POOL: Map<Char, List<Word>> = ('A'..'Z').associateWith { c ->
    val base = BASE.filter { it.first == c }.map { it.second }
    val fromVocab = VOCAB.filter { it.en.firstOrNull()?.uppercaseChar() == c }
    (base + fromVocab).distinctBy { it.emoji }
}

private data class Lang(val flag: String, val locale: Locale, val pick: (Word) -> String)

private val LANGS = listOf(
    Lang("🇬🇧", EN) { it.en },
    Lang("🇵🇱", PL) { it.pl },
    Lang("🇧🇷", PT) { it.pt },
)

/** Speak the letter then the word, both in the chosen language. */
private fun speakLetter(services: GameServices, letter: Char, lang: Lang, word: Word) {
    services.speech.speakSequence(
        listOf(
            SpeechService.Utterance(letter.toString(), lang.locale),
            SpeechService.Utterance(lang.pick(word), lang.locale),
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
        var langIdx by remember { mutableIntStateOf(0) }
        val lang = LANGS[langIdx]
        val letter = 'A' + i
        val pool = LETTER_POOL.getValue(letter)
        var word by remember { mutableStateOf(pool.random()) }

        // Pick a new random word (and say it in the chosen language) when the letter changes.
        LaunchedEffect(i) {
            val w = LETTER_POOL.getValue('A' + i).random()
            word = w
            speakLetter(services, 'A' + i, LANGS[langIdx], w)
        }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFFFF0E4), Color(0xFFFFE7EC))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Choose a language (and tap again to hear the word in it).
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LANGS.forEachIndexed { idx, l ->
                        KidCircleButton(
                            onClick = {
                                langIdx = idx
                                speakLetter(services, letter, l, word)
                            },
                            glyph = l.flag,
                            size = 58,
                            containerColor = if (idx == langIdx) Sunshine else Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Big letter — tap for another random word starting with this letter.
                Surface(
                    onClick = {
                        val w = pool.random()
                        word = w
                        speakLetter(services, letter, lang, w)
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(180.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            letter.toString(),
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(word.emoji, fontSize = 88.sp)
                Spacer(Modifier.height(8.dp))
                // Word shown in the chosen language.
                Text(
                    lang.pick(word),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KidCircleButton(
                        onClick = { i = (i - 1 + 26) % 26 },
                        glyph = "◀",
                        size = 72,
                    )
                    KidCircleButton(
                        onClick = { i = (i + 1) % 26 },
                        glyph = "▶",
                        size = 72,
                    )
                }
            }
        }
    }
}
