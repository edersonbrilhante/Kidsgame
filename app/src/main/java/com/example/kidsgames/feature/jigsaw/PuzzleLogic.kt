package com.example.kidsgames.feature.jigsaw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

data class Tile(val row: Int, val col: Int, val bitmap: Bitmap)

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

    /** A cheerful built-in picture so the game is playable before importing a photo. */
    fun sample(size: Int = 900): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x9B, 0xD7, 0xFF)                 // sky
        canvas.drawRect(0f, 0f, s, s * 0.7f, p)
        p.color = Color.rgb(0x7A, 0xD6, 0x8A)                 // grass
        canvas.drawRect(0f, s * 0.7f, s, s, p)
        p.color = Color.rgb(0xFF, 0xC7, 0x3C)                 // sun
        canvas.drawCircle(s * 0.78f, s * 0.22f, s * 0.12f, p)
        p.color = Color.rgb(0xEF, 0x8A, 0x5D)                 // house body
        canvas.drawRect(s * 0.28f, s * 0.45f, s * 0.60f, s * 0.72f, p)
        val roof = Path().apply {                             // roof
            moveTo(s * 0.24f, s * 0.45f)
            lineTo(s * 0.44f, s * 0.30f)
            lineTo(s * 0.64f, s * 0.45f)
            close()
        }
        p.color = Color.rgb(0xC0, 0x4A, 0x3A)
        canvas.drawPath(roof, p)
        p.color = Color.rgb(0x6B, 0x3F, 0x2A)                 // door
        canvas.drawRect(s * 0.40f, s * 0.58f, s * 0.48f, s * 0.72f, p)
        return bmp
    }
}
