package com.example.kidsgames.framework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Renders words/pictures using bundled Twemoji images (CC-BY 4.0), downloaded at build time
 * into assets/twemoji/. Runtime is fully offline. If an image is missing, the system emoji
 * glyph is used instead, so nothing ever breaks.
 */

/** Twemoji asset filename key: hex codepoints joined by '-', dropping the VS16 selector. */
fun twemojiKey(emoji: String): String =
    emoji.codePoints().toArray()
        .filter { it != 0xFE0F }
        .joinToString("-") { Integer.toHexString(it) }

// key -> bitmap (or null when the asset is absent). Small 72px PNGs; cheap to keep.
private val twCache = ConcurrentHashMap<String, Optional>()

private class Optional(val bitmap: Bitmap?)

/** Loads the bundled Twemoji bitmap for [emoji], or null if it isn't bundled. */
fun loadTwemoji(context: Context, emoji: String): Bitmap? {
    val key = twemojiKey(emoji)
    twCache[key]?.let { return it.bitmap }
    val bmp = try {
        context.assets.open("twemoji/$key.png").use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        null
    }
    twCache[key] = Optional(bmp)
    return bmp
}

/** Shows the Twemoji image for [emoji] if bundled, else the system emoji glyph. */
@Composable
fun EmojiIcon(emoji: String, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, emoji) {
        value = withContext(Dispatchers.IO) { loadTwemoji(context, emoji)?.asImageBitmap() }
    }
    val bmp = image
    if (bmp != null) {
        Image(bitmap = bmp, contentDescription = null, modifier = modifier.size(size))
    } else {
        Text(emoji, fontSize = fontSizeFor(size), modifier = modifier)
    }
}

private fun fontSizeFor(size: Dp): TextUnit = (size.value * 0.92f).sp
