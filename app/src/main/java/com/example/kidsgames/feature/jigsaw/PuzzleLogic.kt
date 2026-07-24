package com.example.kidsgames.feature.jigsaw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

data class Tile(val row: Int, val col: Int, val bitmap: Bitmap)

/**
 * A built-in puzzle picture. Each carries its name in English / Polish / Portuguese so
 * the puzzle doubles as a first-words vocabulary card. Pictures are emoji rendered onto a
 * colored background (no bundled art), so the app stays asset-free.
 */
data class SamplePicture(
    val id: String,
    val emoji: String,
    val en: String,
    val pl: String,
    val pt: String,
    val draw: (Int) -> Bitmap,
)

object PuzzleLogic {

    /** Center-crops to a square so the grid cells look even. */
    fun square(src: Bitmap): Bitmap {
        if (src.width == src.height) return src
        val size = minOf(src.width, src.height)
        val x = (src.width - size) / 2
        val y = (src.height - size) / 2
        return Bitmap.createBitmap(src, x, y, size, size)
    }

    /** Cuts a bitmap into rows x cols tiles. */
    fun slice(src: Bitmap, rows: Int, cols: Int): List<Tile> {
        val sq = square(src)
        val w = sq.width / cols
        val h = sq.height / rows
        val tiles = ArrayList<Tile>(rows * cols)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                tiles.add(Tile(r, c, Bitmap.createBitmap(sq, c * w, r * h, w, h)))
            }
        }
        return tiles
    }

    // Soft background colors cycled behind emoji puzzle pictures.
    private val EMOJI_BG = listOf(
        Color.rgb(0x9B, 0xD7, 0xFF),
        Color.rgb(0xFF, 0xD6, 0x8A),
        Color.rgb(0xB8, 0xE9, 0xC4),
        Color.rgb(0xF8, 0xC1, 0xDA),
        Color.rgb(0xCF, 0xC4, 0xF5),
        Color.rgb(0xFF, 0xC1, 0xA6),
        Color.rgb(0xA6, 0xE6, 0xE6),
    )

    /** The picture library: every vocabulary word rendered as an emoji puzzle picture. */
    val samples: List<SamplePicture> = VOCAB.mapIndexed { i, w ->
        val bg = EMOJI_BG[i % EMOJI_BG.size]
        SamplePicture(
            id = "v$i",
            emoji = w.emoji,
            en = w.en,
            pl = w.pl,
            pt = w.pt,
            draw = { size -> emojiPicture(w.emoji, bg, size) },
        )
    }

    /** Default picture so the game is playable before importing a photo. */
    fun sample(size: Int = 900): Bitmap = samples.first().draw(size)

    /** Renders a big color emoji centered on a colored background as a puzzle picture. */
    fun emojiPicture(emoji: String, bg: Int, size: Int = 900): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()
        p.color = bg
        canvas.drawRect(0f, 0f, s, s, p)
        p.color = Color.argb(70, 255, 255, 255)
        canvas.drawCircle(s / 2f, s / 2f, s * 0.42f, p)

        val tp = Paint(Paint.ANTI_ALIAS_FLAG)
        tp.textAlign = Paint.Align.CENTER
        tp.textSize = s * 0.6f
        val fm = tp.fontMetrics
        val baseline = s / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(emoji, s / 2f, baseline, tp)
        return bmp
    }
}
