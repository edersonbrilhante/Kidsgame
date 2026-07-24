package com.example.kidsgames.feature.colors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.kidsgames.R
import com.example.kidsgames.core.EN
import com.example.kidsgames.core.PL
import com.example.kidsgames.core.PT
import com.example.kidsgames.core.Word
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidScreen
import com.example.kidsgames.framework.MiniGame
import com.example.kidsgames.framework.MiniGameInfo
import com.example.kidsgames.ui.theme.Aqua
import com.example.kidsgames.ui.theme.Grape
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private enum class ShapeKind { CIRCLE, SQUARE, TRIANGLE, STAR }

private data class ColorOpt(val color: Color, val word: Word)

private val COLORS = listOf(
    ColorOpt(Color(0xFFE53E3E), Word("red", "czerwony", "vermelho")),
    ColorOpt(Color(0xFF5A8DEE), Word("blue", "niebieski", "azul")),
    ColorOpt(Color(0xFF57C785), Word("green", "zielony", "verde")),
    ColorOpt(Color(0xFFFFC23C), Word("yellow", "żółty", "amarelo")),
    ColorOpt(Color(0xFFEF5DA8), Word("pink", "różowy", "rosa")),
    ColorOpt(Color(0xFFFF8A47), Word("orange", "pomarańczowy", "laranja")),
)

private val SHAPE_WORDS = mapOf(
    ShapeKind.CIRCLE to Word("circle", "koło", "círculo"),
    ShapeKind.SQUARE to Word("square", "kwadrat", "quadrado"),
    ShapeKind.TRIANGLE to Word("triangle", "trójkąt", "triângulo"),
    ShapeKind.STAR to Word("star", "gwiazda", "estrela"),
)

private data class Tile(val shape: ShapeKind, val color: Color)

private data class Round(
    val tiles: List<Tile>,
    val correctIndex: Int,
    val promptText: String,
    val promptLocale: Locale,
)

private fun roundFor(tiles: List<Tile>, target: Int, prompt: Word): Round {
    // One random language per round (not all three).
    val (locale, text) = listOf(EN to prompt.en, PL to prompt.pl, PT to prompt.pt).random()
    return Round(tiles, target, text, locale)
}

private fun newRound(): Round {
    val byColor = Random.nextBoolean()
    return if (byColor) {
        val cols = COLORS.shuffled().take(4)
        val shapes = ShapeKind.entries.shuffled()
        val tiles = cols.mapIndexed { idx, c -> Tile(shapes[idx % shapes.size], c.color) }
        val target = (0..3).random()
        roundFor(tiles, target, cols[target].word)
    } else {
        val shapes = ShapeKind.entries.shuffled().take(4)
        val tiles = shapes.map { Tile(it, COLORS.random().color) }
        val target = (0..3).random()
        roundFor(tiles, target, SHAPE_WORDS.getValue(shapes[target]))
    }
}

class ColorsShapesMiniGame : MiniGame {
    override val info = MiniGameInfo(
        id = "colors",
        titleRes = R.string.game_colors,
        emoji = "🎨",
        gradient = listOf(Aqua, Grape),
    )

    @Composable
    override fun Screen(services: GameServices, onExit: () -> Unit) {
        var round by remember { mutableStateOf(newRound()) }

        // Say the first prompt once; later prompts are spoken from onCorrect below.
        LaunchedEffect(Unit) { services.speech.speak(round.promptText, round.promptLocale) }

        KidScreen(onExit = onExit, colors = listOf(Color(0xFFE6FBFA), Color(0xFFEFE7FF))) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                for (r in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (c in 0..1) {
                            val index = r * 2 + c
                            val tile = round.tiles[index]
                            ShapeTile(tile = tile, sizeDp = 150) {
                                if (index == round.correctIndex) {
                                    services.audio.playCorrect()
                                    val next = newRound()
                                    round = next
                                    services.speech.speak(next.promptText, next.promptLocale)
                                } else {
                                    // Gentle retry: say the same prompt again (same language).
                                    services.speech.speak(round.promptText, round.promptLocale)
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
private fun ShapeTile(tile: Tile, sizeDp: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.size(sizeDp.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(22.dp)) {
            drawTileShape(tile.shape, tile.color)
        }
    }
}

private fun DrawScope.drawTileShape(shape: ShapeKind, color: Color) {
    val w = size.width
    val h = size.height
    when (shape) {
        ShapeKind.CIRCLE -> drawCircle(color)
        ShapeKind.SQUARE -> drawRoundRect(color, cornerRadius = CornerRadius(w * 0.12f, w * 0.12f))
        ShapeKind.TRIANGLE -> {
            val p = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(p, color)
        }
        ShapeKind.STAR -> drawPath(starPath(w, h), color)
    }
}

private fun starPath(w: Float, h: Float): Path {
    val cx = w / 2f
    val cy = h / 2f
    val outer = min(w, h) / 2f
    val inner = outer * 0.5f
    return Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val a = Math.toRadians((-90.0 + i * 36.0))
            val x = cx + r * cos(a).toFloat()
            val y = cy + r * sin(a).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
