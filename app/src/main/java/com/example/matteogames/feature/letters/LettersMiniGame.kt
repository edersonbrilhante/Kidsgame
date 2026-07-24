package com.example.matteogames.feature.letters

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
import com.example.matteogames.R
import com.example.matteogames.core.EN
import com.example.matteogames.core.PL
import com.example.matteogames.core.PT
import com.example.matteogames.core.SpeechService
import com.example.matteogames.core.Word
import com.example.matteogames.feature.jigsaw.VOCAB
import com.example.matteogames.framework.EmojiIcon
import com.example.matteogames.framework.GameServices
import com.example.matteogames.framework.KidCircleButton
import com.example.matteogames.framework.KidScreen
import com.example.matteogames.framework.MiniGame
import com.example.matteogames.framework.MiniGameInfo
import com.example.matteogames.ui.theme.Coral
import com.example.matteogames.ui.theme.Sunshine
import com.example.matteogames.ui.theme.Tangerine
import java.util.Locale

private data class Lang(val flag: String, val locale: Locale, val pick: (Word) -> String)

private val LANGS = listOf(
    Lang("🇬🇧", EN) { it.en },
    Lang("🇵🇱", PL) { it.pl },
    Lang("🇧🇷", PT) { it.pt },
)

/** The letters that actually have words in the selected language, A–Z order. */
private fun lettersFor(lang: Lang): List<Char> =
    VOCAB.mapNotNull { lang.pick(it).firstOrNull()?.uppercaseChar() }
        .filter { it in 'A'..'Z' }
        .distinct()
        .sorted()

/** Words whose name in [lang] starts with [letter]. */
private fun wordsFor(lang: Lang, letter: Char): List<Word> =
    VOCAB.filter { lang.pick(it).firstOrNull()?.uppercaseChar() == letter }
        .distinctBy { it.emoji }

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
        var langIdx by remember { mutableIntStateOf(0) }
        val lang = LANGS[langIdx]
        // Alphabet + word pool follow the SELECTED language.
        val letters = remember(langIdx) { lettersFor(lang) }
        var pos by remember(langIdx) { mutableIntStateOf(0) }
        val letter = letters[pos.coerceIn(0, letters.size - 1)]
        val pool = wordsFor(lang, letter)
        var word by remember { mutableStateOf(pool.random()) }

        // New random word (spoken in the selected language) when the language or letter changes.
        LaunchedEffect(langIdx, pos) {
            val l = LANGS[langIdx]
            val ls = lettersFor(l)
            val ltr = ls[pos.coerceIn(0, ls.size - 1)]
            val w = wordsFor(l, ltr).random()
            word = w
            speakLetter(services, ltr, l, w)
        }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFFFF0E4), Color(0xFFFFE7EC))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Choose the language: alphabet + words follow it.
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LANGS.forEachIndexed { idx, l ->
                        KidCircleButton(
                            onClick = { langIdx = idx },
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

                Spacer(Modifier.height(16.dp))
                EmojiIcon(word.emoji, 84.dp)
                Spacer(Modifier.height(6.dp))
                // Word in the selected language.
                Text(
                    lang.pick(word),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(10.dp))
                // The same word in the other languages — tap to hear it.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LANGS.filterIndexed { idx, _ -> idx != langIdx }.forEach { l ->
                        OtherLangChip(l.flag, l.pick(word)) {
                            services.speech.speak(l.pick(word), l.locale)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KidCircleButton(
                        onClick = { pos = (pos - 1 + letters.size) % letters.size },
                        glyph = "◀",
                        size = 72,
                    )
                    KidCircleButton(
                        onClick = { pos = (pos + 1) % letters.size },
                        glyph = "▶",
                        size = 72,
                    )
                }
            }
        }
    }
}

/** A flag + word chip that speaks the word in that language when tapped. */
@Composable
private fun OtherLangChip(flag: String, word: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(flag, fontSize = 20.sp)
            Text(
                word,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
