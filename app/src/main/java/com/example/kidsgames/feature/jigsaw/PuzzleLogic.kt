package com.example.kidsgames.feature.jigsaw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

data class Tile(val row: Int, val col: Int, val bitmap: Bitmap)

/**
 * A built-in puzzle picture, drawn programmatically so the app stays asset-free.
 * Each picture carries its name in English / Polish / Portuguese so the puzzle
 * doubles as a first-words vocabulary card.
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

    /** Hand-drawn "hero" pictures (nicer than a plain emoji), shown first. */
    private val specials: List<SamplePicture> = listOf(
        SamplePicture("soccer",  "⚽",  en = "Ball",    pl = "Piłka",   pt = "Bola")    { soccer(it) },
        SamplePicture("dragon",  "🐲",  en = "Dragon",  pl = "Smok",    pt = "Dragão")  { dragon(it) },
        SamplePicture("web",     "🕷️", en = "Spider",  pl = "Pająk",   pt = "Aranha")  { webHero(it) },
        SamplePicture("iron",    "🦾",  en = "Robot",   pl = "Robot",   pt = "Robô")    { ironHero(it) },
        SamplePicture("hulk",    "💪",  en = "Hero",    pl = "Bohater", pt = "Herói")   { greenHero(it) },
        SamplePicture("puppy",   "🐶",  en = "Dog",     pl = "Pies",    pt = "Cachorro"){ puppy(it) },
        SamplePicture("mouse",   "🐭",  en = "Mouse",   pl = "Mysz",    pt = "Rato")    { mouse(it) },
        SamplePicture("bunny",   "🐰",  en = "Rabbit",  pl = "Królik",  pt = "Coelho")  { bunny(it) },
        SamplePicture("snowman", "⛄",  en = "Snowman", pl = "Bałwan",  pt = "Boneco")  { snowman(it) },
        SamplePicture("monster", "⚡",  en = "Monster", pl = "Potwór",  pt = "Monstro") { monster(it) },
        SamplePicture("home",    "🏠",  en = "House",   pl = "Dom",     pt = "Casa")    { scene(it) },
    )

    /**
     * The full picture library: the hand-drawn specials followed by the whole emoji
     * [VOCAB]. Each vocabulary word becomes a puzzle + spoken flashcard automatically.
     */
    val samples: List<SamplePicture> = specials + VOCAB.mapIndexed { i, w ->
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
    fun sample(size: Int = 900): Bitmap = soccer(size)

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

    /** Renders a big color emoji centered on a colored background as a puzzle picture. */
    fun emojiPicture(emoji: String, bg: Int, size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
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

    // --- Drawing helpers -----------------------------------------------------

    private fun blank(size: Int): Pair<Bitmap, Canvas> {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        return bmp to Canvas(bmp)
    }

    /** Regular pentagon centered at (cx,cy). */
    private fun pentagon(cx: Float, cy: Float, r: Float, rotDeg: Float = -90f): Path =
        Path().apply {
            for (i in 0 until 5) {
                val a = Math.toRadians((rotDeg + i * 72f).toDouble())
                val px = cx + r * cos(a).toFloat()
                val py = cy + r * sin(a).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

    // --- Themed pictures -----------------------------------------------------

    /** ⚽ Soccer ball on grass. */
    fun soccer(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x57, 0xC7, 0x85)                 // grass
        canvas.drawRect(0f, 0f, s, s, p)

        val cx = s / 2f; val cy = s / 2f; val r = s * 0.40f
        p.color = Color.WHITE
        canvas.drawCircle(cx, cy, r, p)

        // Central black pentagon + five around it = classic ball.
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawPath(pentagon(cx, cy, r * 0.34f), p)
        for (i in 0 until 5) {
            val a = Math.toRadians((-90f + i * 72f).toDouble())
            val px = cx + r * 0.66f * cos(a).toFloat()
            val py = cy + r * 0.66f * sin(a).toFloat()
            canvas.drawPath(pentagon(px, py, r * 0.20f, (-90f + i * 72f) + 36f), p)
        }
        // Ball outline.
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.012f
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawCircle(cx, cy, r, p)
        p.style = Paint.Style.FILL
        return bmp
    }

    /** 🐲 Friendly green dragon face. */
    fun dragon(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0xD7, 0xF3, 0xDD)                 // soft mint sky
        canvas.drawRect(0f, 0f, s, s, p)

        // Horns.
        p.color = Color.rgb(0xFF, 0xE0, 0x8A)
        val leftHorn = Path().apply {
            moveTo(s * 0.30f, s * 0.30f); lineTo(s * 0.20f, s * 0.10f); lineTo(s * 0.40f, s * 0.24f); close()
        }
        val rightHorn = Path().apply {
            moveTo(s * 0.70f, s * 0.30f); lineTo(s * 0.80f, s * 0.10f); lineTo(s * 0.60f, s * 0.24f); close()
        }
        canvas.drawPath(leftHorn, p)
        canvas.drawPath(rightHorn, p)

        // Head.
        p.color = Color.rgb(0x4C, 0xC7, 0x6A)
        canvas.drawCircle(s * 0.5f, s * 0.52f, s * 0.32f, p)

        // Snout.
        p.color = Color.rgb(0x8A, 0xDD, 0x9E)
        canvas.drawOval(RectF(s * 0.34f, s * 0.58f, s * 0.66f, s * 0.82f), p)

        // Nostrils.
        p.color = Color.rgb(0x2E, 0x7D, 0x46)
        canvas.drawCircle(s * 0.44f, s * 0.68f, s * 0.02f, p)
        canvas.drawCircle(s * 0.56f, s * 0.68f, s * 0.02f, p)

        // Eyes.
        p.color = Color.WHITE
        canvas.drawCircle(s * 0.40f, s * 0.46f, s * 0.075f, p)
        canvas.drawCircle(s * 0.60f, s * 0.46f, s * 0.075f, p)
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawCircle(s * 0.41f, s * 0.47f, s * 0.035f, p)
        canvas.drawCircle(s * 0.61f, s * 0.47f, s * 0.035f, p)

        // Smile.
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.02f
        p.color = Color.rgb(0x2E, 0x7D, 0x46)
        val smile = Path().apply {
            moveTo(s * 0.40f, s * 0.74f)
            quadTo(s * 0.5f, s * 0.82f, s * 0.60f, s * 0.74f)
        }
        canvas.drawPath(smile, p)
        p.style = Paint.Style.FILL
        return bmp
    }

    /** 🕷️ Red web-hero mask. */
    fun webHero(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0xE5, 0x3E, 0x3E)                 // hero red
        canvas.drawRect(0f, 0f, s, s, p)

        // Web: radial lines from top center + concentric arcs.
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.006f
        p.color = Color.rgb(0x8E, 0x1B, 0x1B)
        val ox = s * 0.5f; val oy = s * 0.06f
        for (i in 0 until 9) {
            val a = Math.toRadians((30.0 + i * 15.0))
            canvas.drawLine(ox, oy, ox + (s * 1.1f) * cos(a).toFloat(), oy + (s * 1.1f) * sin(a).toFloat(), p)
        }
        for (ring in 1..6) {
            val rr = s * 0.14f * ring
            canvas.drawArc(RectF(ox - rr, oy - rr, ox + rr, oy + rr), 30f, 120f, false, p)
        }

        // Eyes (angled white almonds with black rim).
        p.style = Paint.Style.FILL
        fun eye(mirror: Boolean) {
            val dir = if (mirror) -1f else 1f
            val bx = s * 0.5f + dir * s * 0.14f
            val path = Path().apply {
                moveTo(bx, s * 0.44f)
                quadTo(bx + dir * s * 0.16f, s * 0.40f, bx + dir * s * 0.15f, s * 0.56f)
                quadTo(bx + dir * s * 0.02f, s * 0.60f, bx, s * 0.44f)
                close()
            }
            p.color = Color.rgb(0x22, 0x22, 0x22)
            canvas.drawPath(path, p)
            val inner = Path().apply {
                moveTo(bx + dir * s * 0.01f, s * 0.455f)
                quadTo(bx + dir * s * 0.135f, s * 0.425f, bx + dir * s * 0.125f, s * 0.55f)
                quadTo(bx + dir * s * 0.03f, s * 0.575f, bx + dir * s * 0.01f, s * 0.455f)
                close()
            }
            p.color = Color.WHITE
            canvas.drawPath(inner, p)
        }
        eye(false)
        eye(true)
        return bmp
    }

    /** 💪 Big friendly green hero. */
    fun greenHero(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x6A, 0x4C, 0x93)                 // purple sky
        canvas.drawRect(0f, 0f, s, s, p)

        // Shoulders.
        p.color = Color.rgb(0x3E, 0xA5, 0x55)
        canvas.drawOval(RectF(s * 0.12f, s * 0.72f, s * 0.88f, s * 1.10f), p)

        // Head.
        p.color = Color.rgb(0x57, 0xC7, 0x6A)
        canvas.drawOval(RectF(s * 0.26f, s * 0.24f, s * 0.74f, s * 0.80f), p)

        // Hair.
        p.color = Color.rgb(0x22, 0x22, 0x22)
        val hair = Path().apply {
            moveTo(s * 0.28f, s * 0.34f)
            lineTo(s * 0.34f, s * 0.22f)
            lineTo(s * 0.40f, s * 0.32f)
            lineTo(s * 0.47f, s * 0.20f)
            lineTo(s * 0.53f, s * 0.32f)
            lineTo(s * 0.60f, s * 0.22f)
            lineTo(s * 0.66f, s * 0.34f)
            close()
        }
        canvas.drawPath(hair, p)

        // Brows (friendly, slightly angled).
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.02f
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawLine(s * 0.36f, s * 0.44f, s * 0.47f, s * 0.48f, p)
        canvas.drawLine(s * 0.64f, s * 0.44f, s * 0.53f, s * 0.48f, p)

        // Eyes.
        p.style = Paint.Style.FILL
        p.color = Color.WHITE
        canvas.drawCircle(s * 0.41f, s * 0.53f, s * 0.05f, p)
        canvas.drawCircle(s * 0.59f, s * 0.53f, s * 0.05f, p)
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawCircle(s * 0.42f, s * 0.54f, s * 0.022f, p)
        canvas.drawCircle(s * 0.60f, s * 0.54f, s * 0.022f, p)

        // Smile.
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.022f
        val smile = Path().apply {
            moveTo(s * 0.42f, s * 0.66f)
            quadTo(s * 0.5f, s * 0.73f, s * 0.58f, s * 0.66f)
        }
        canvas.drawPath(smile, p)
        p.style = Paint.Style.FILL
        return bmp
    }

    /** 🦾 Red-and-gold iron hero mask. */
    fun ironHero(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x2A, 0x2A, 0x3A)                 // dark backdrop
        canvas.drawRect(0f, 0f, s, s, p)

        p.color = Color.rgb(0xD3, 0x2F, 0x2F)                 // red mask
        canvas.drawOval(RectF(s * 0.24f, s * 0.16f, s * 0.76f, s * 0.86f), p)

        p.color = Color.rgb(0xFF, 0xC1, 0x07)                 // gold faceplate
        val plate = Path().apply {
            moveTo(s * 0.40f, s * 0.34f)
            lineTo(s * 0.60f, s * 0.34f)
            lineTo(s * 0.64f, s * 0.60f)
            lineTo(s * 0.55f, s * 0.80f)
            lineTo(s * 0.45f, s * 0.80f)
            lineTo(s * 0.36f, s * 0.60f)
            close()
        }
        canvas.drawPath(plate, p)

        p.color = Color.rgb(0xE0, 0xFF, 0xFF)                 // glowing eyes
        canvas.drawPath(Path().apply {
            moveTo(s * 0.40f, s * 0.50f); lineTo(s * 0.48f, s * 0.47f); lineTo(s * 0.48f, s * 0.53f); close()
        }, p)
        canvas.drawPath(Path().apply {
            moveTo(s * 0.60f, s * 0.50f); lineTo(s * 0.52f, s * 0.47f); lineTo(s * 0.52f, s * 0.53f); close()
        }, p)
        return bmp
    }

    /** 🐶 Friendly blue puppy. */
    fun puppy(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0xFF, 0xF0, 0xD0)                 // warm cream
        canvas.drawRect(0f, 0f, s, s, p)

        val blue = Color.rgb(0x6C, 0x9B, 0xD2)
        val darkBlue = Color.rgb(0x3E, 0x6F, 0xA8)

        p.color = darkBlue                                    // pointy ears
        canvas.drawPath(Path().apply {
            moveTo(s * 0.28f, s * 0.42f); lineTo(s * 0.20f, s * 0.14f); lineTo(s * 0.44f, s * 0.30f); close()
        }, p)
        canvas.drawPath(Path().apply {
            moveTo(s * 0.72f, s * 0.42f); lineTo(s * 0.80f, s * 0.14f); lineTo(s * 0.56f, s * 0.30f); close()
        }, p)

        p.color = blue                                        // head
        canvas.drawCircle(s * 0.5f, s * 0.52f, s * 0.30f, p)

        p.color = Color.rgb(0xEC, 0xE6, 0xDA)                 // muzzle
        canvas.drawOval(RectF(s * 0.36f, s * 0.56f, s * 0.64f, s * 0.80f), p)

        p.color = Color.rgb(0x22, 0x22, 0x22)                 // nose
        canvas.drawOval(RectF(s * 0.46f, s * 0.60f, s * 0.54f, s * 0.66f), p)

        p.color = Color.WHITE                                 // eyes
        canvas.drawCircle(s * 0.40f, s * 0.48f, s * 0.06f, p)
        canvas.drawCircle(s * 0.60f, s * 0.48f, s * 0.06f, p)
        p.color = Color.rgb(0x22, 0x22, 0x22)
        canvas.drawCircle(s * 0.41f, s * 0.49f, s * 0.028f, p)
        canvas.drawCircle(s * 0.61f, s * 0.49f, s * 0.028f, p)
        return bmp
    }

    /** 🐭 Cheerful round-eared mouse. */
    fun mouse(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0xFF, 0xC2, 0x3C)                 // sunny bg
        canvas.drawRect(0f, 0f, s, s, p)

        p.color = Color.rgb(0x2B, 0x2B, 0x2B)                 // ears + head
        canvas.drawCircle(s * 0.30f, s * 0.26f, s * 0.14f, p)
        canvas.drawCircle(s * 0.70f, s * 0.26f, s * 0.14f, p)
        canvas.drawCircle(s * 0.5f, s * 0.50f, s * 0.28f, p)

        p.color = Color.rgb(0xF6, 0xC9, 0xA0)                 // face
        canvas.drawOval(RectF(s * 0.30f, s * 0.50f, s * 0.70f, s * 0.82f), p)

        p.color = Color.rgb(0x2B, 0x2B, 0x2B)                 // nose
        canvas.drawOval(RectF(s * 0.46f, s * 0.58f, s * 0.54f, s * 0.64f), p)

        p.color = Color.WHITE                                 // eyes
        canvas.drawOval(RectF(s * 0.40f, s * 0.40f, s * 0.48f, s * 0.54f), p)
        canvas.drawOval(RectF(s * 0.52f, s * 0.40f, s * 0.60f, s * 0.54f), p)
        p.color = Color.rgb(0x2B, 0x2B, 0x2B)
        canvas.drawCircle(s * 0.45f, s * 0.48f, s * 0.02f, p)
        canvas.drawCircle(s * 0.55f, s * 0.48f, s * 0.02f, p)

        p.style = Paint.Style.STROKE                          // smile
        p.strokeWidth = s * 0.02f
        canvas.drawArc(RectF(s * 0.40f, s * 0.60f, s * 0.60f, s * 0.76f), 20f, 140f, false, p)
        p.style = Paint.Style.FILL
        return bmp
    }

    /** 🐰 Grey cartoon bunny. */
    fun bunny(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x9B, 0xD7, 0xFF)                 // sky
        canvas.drawRect(0f, 0f, s, s, p)

        val grey = Color.rgb(0xB4, 0xBB, 0xC4)
        val pink = Color.rgb(0xF3, 0x9B, 0xB4)

        p.color = grey                                        // ears
        canvas.drawRoundRect(RectF(s * 0.34f, s * 0.06f, s * 0.46f, s * 0.42f), s * 0.06f, s * 0.06f, p)
        canvas.drawRoundRect(RectF(s * 0.54f, s * 0.06f, s * 0.66f, s * 0.42f), s * 0.06f, s * 0.06f, p)
        p.color = pink
        canvas.drawRoundRect(RectF(s * 0.37f, s * 0.10f, s * 0.43f, s * 0.38f), s * 0.03f, s * 0.03f, p)
        canvas.drawRoundRect(RectF(s * 0.57f, s * 0.10f, s * 0.63f, s * 0.38f), s * 0.03f, s * 0.03f, p)

        p.color = grey                                        // head
        canvas.drawCircle(s * 0.5f, s * 0.56f, s * 0.28f, p)

        p.color = Color.WHITE                                 // cheeks
        canvas.drawCircle(s * 0.44f, s * 0.64f, s * 0.10f, p)
        canvas.drawCircle(s * 0.56f, s * 0.64f, s * 0.10f, p)

        p.color = pink                                        // nose
        canvas.drawOval(RectF(s * 0.47f, s * 0.58f, s * 0.53f, s * 0.63f), p)

        p.color = Color.WHITE                                 // teeth
        canvas.drawRect(s * 0.475f, s * 0.63f, s * 0.498f, s * 0.72f, p)
        canvas.drawRect(s * 0.502f, s * 0.63f, s * 0.525f, s * 0.72f, p)

        p.color = Color.rgb(0x22, 0x22, 0x22)                 // eyes
        canvas.drawCircle(s * 0.42f, s * 0.50f, s * 0.03f, p)
        canvas.drawCircle(s * 0.58f, s * 0.50f, s * 0.03f, p)
        return bmp
    }

    /** ⛄ Happy snowman on ice. */
    fun snowman(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0xCF, 0xED, 0xFF)                 // icy sky
        canvas.drawRect(0f, 0f, s, s, p)
        p.color = Color.rgb(0xEF, 0xF8, 0xFF)                 // snow ground
        canvas.drawRect(0f, s * 0.82f, s, s, p)

        p.color = Color.WHITE                                 // body + head
        canvas.drawCircle(s * 0.5f, s * 0.70f, s * 0.24f, p)
        canvas.drawCircle(s * 0.5f, s * 0.36f, s * 0.17f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.008f
        p.color = Color.rgb(0xC0, 0xDA, 0xEA)
        canvas.drawCircle(s * 0.5f, s * 0.70f, s * 0.24f, p)
        canvas.drawCircle(s * 0.5f, s * 0.36f, s * 0.17f, p)
        p.style = Paint.Style.FILL

        p.color = Color.rgb(0x22, 0x22, 0x22)                 // eyes
        canvas.drawCircle(s * 0.45f, s * 0.33f, s * 0.02f, p)
        canvas.drawCircle(s * 0.55f, s * 0.33f, s * 0.02f, p)

        p.color = Color.rgb(0xFF, 0x8A, 0x2B)                 // carrot nose
        canvas.drawPath(Path().apply {
            moveTo(s * 0.5f, s * 0.37f); lineTo(s * 0.62f, s * 0.40f); lineTo(s * 0.5f, s * 0.42f); close()
        }, p)

        p.color = Color.rgb(0x22, 0x22, 0x22)                 // buttons
        canvas.drawCircle(s * 0.5f, s * 0.64f, s * 0.018f, p)
        canvas.drawCircle(s * 0.5f, s * 0.72f, s * 0.018f, p)

        p.color = Color.rgb(0x5A, 0x8D, 0xEE)                 // scarf
        canvas.drawRect(s * 0.36f, s * 0.49f, s * 0.64f, s * 0.54f, p)
        return bmp
    }

    /** ⚡ Round yellow pocket-monster. */
    fun monster(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = size.toFloat()

        p.color = Color.rgb(0x5A, 0x8D, 0xEE)                 // blue sky
        canvas.drawRect(0f, 0f, s, s, p)

        val yellow = Color.rgb(0xFF, 0xD6, 0x2E)

        // Ears with black tips.
        p.color = yellow
        canvas.drawPath(Path().apply {
            moveTo(s * 0.32f, s * 0.34f); lineTo(s * 0.22f, s * 0.08f); lineTo(s * 0.44f, s * 0.26f); close()
        }, p)
        canvas.drawPath(Path().apply {
            moveTo(s * 0.68f, s * 0.34f); lineTo(s * 0.78f, s * 0.08f); lineTo(s * 0.56f, s * 0.26f); close()
        }, p)
        p.color = Color.rgb(0x2B, 0x2B, 0x2B)
        canvas.drawPath(Path().apply {
            moveTo(s * 0.22f, s * 0.08f); lineTo(s * 0.30f, s * 0.06f); lineTo(s * 0.30f, s * 0.16f); close()
        }, p)
        canvas.drawPath(Path().apply {
            moveTo(s * 0.78f, s * 0.08f); lineTo(s * 0.70f, s * 0.06f); lineTo(s * 0.70f, s * 0.16f); close()
        }, p)

        // Head.
        p.color = yellow
        canvas.drawCircle(s * 0.5f, s * 0.54f, s * 0.30f, p)

        // Red cheeks.
        p.color = Color.rgb(0xE5, 0x3E, 0x3E)
        canvas.drawCircle(s * 0.34f, s * 0.60f, s * 0.06f, p)
        canvas.drawCircle(s * 0.66f, s * 0.60f, s * 0.06f, p)

        // Eyes.
        p.color = Color.rgb(0x2B, 0x2B, 0x2B)
        canvas.drawCircle(s * 0.40f, s * 0.48f, s * 0.045f, p)
        canvas.drawCircle(s * 0.60f, s * 0.48f, s * 0.045f, p)
        p.color = Color.WHITE
        canvas.drawCircle(s * 0.415f, s * 0.465f, s * 0.016f, p)
        canvas.drawCircle(s * 0.615f, s * 0.465f, s * 0.016f, p)

        // Nose + smile.
        p.color = Color.rgb(0x2B, 0x2B, 0x2B)
        canvas.drawCircle(s * 0.5f, s * 0.56f, s * 0.012f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = s * 0.016f
        canvas.drawArc(RectF(s * 0.42f, s * 0.56f, s * 0.58f, s * 0.70f), 20f, 140f, false, p)
        p.style = Paint.Style.FILL
        return bmp
    }

    /** 🏠 The original cheerful house scene. */
    fun scene(size: Int = 900): Bitmap {
        val (bmp, canvas) = blank(size)
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
