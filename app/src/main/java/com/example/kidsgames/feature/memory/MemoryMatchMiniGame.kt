package com.example.kidsgames.feature.memory

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.R
import com.example.kidsgames.core.Word
import com.example.kidsgames.core.sayOne
import com.example.kidsgames.feature.jigsaw.PuzzleLogic
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Bubblegum
import com.example.kidsgames.ui.theme.Grape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class MCard(val pairId: Int, val emoji: String, val word: Word) {
    var faceUp by mutableStateOf(false)
    var matched by mutableStateOf(false)
}

/** Three pairs (six cards), shuffled — a gentle size for a 4-year-old. */
private fun newDeck(): List<MCard> {
    val picks = PuzzleLogic.samples.shuffled().take(3)
    val cards = ArrayList<MCard>(6)
    picks.forEachIndexed { i, s ->
        val w = Word(s.en, s.pl, s.pt)
        cards.add(MCard(i, s.emoji, w))
        cards.add(MCard(i, s.emoji, w))
    }
    return cards.shuffled()
}

class MemoryMatchMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "memory",
        titleRes = R.string.game_memory,
        emoji = "🧠",
        gradient = listOf(Grape, Bubblegum),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var deck by remember { mutableStateOf(newDeck()) }
        var first by remember { mutableStateOf<MCard?>(null) }
        var busy by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        fun onTap(card: MCard) {
            if (busy || card.faceUp || card.matched) return
            card.faceUp = true
            services.speech.sayOne(card.word, rate = 1.1f)
            val a = first
            if (a == null) {
                first = card
            } else {
                first = null
                if (a.pairId == card.pairId) {
                    a.matched = true
                    card.matched = true
                    services.audio.playCorrect()
                    if (deck.all { it.matched }) {
                        scope.launch {
                            delay(1000)
                            deck = newDeck()
                        }
                    }
                } else {
                    busy = true
                    scope.launch {
                        delay(800)
                        a.faceUp = false
                        card.faceUp = false
                        busy = false
                    }
                }
            }
        }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFEFE7FF), Color(0xFFFFE7F3))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            ) {
                deck.chunked(3).forEach { rowCards ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        rowCards.forEach { card ->
                            MemoryCard(card) { onTap(card) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(card: MCard, onClick: () -> Unit) {
    val revealed = card.faceUp || card.matched
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (card.matched) Color(0xFFCDEFD6) else Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.size(96.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(if (revealed) card.emoji else "❓", fontSize = 46.sp)
        }
    }
}
