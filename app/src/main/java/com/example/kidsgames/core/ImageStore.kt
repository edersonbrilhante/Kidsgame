package com.example.kidsgames.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Stores imported pictures inside the app's private internal storage.
 * Nothing is written to shared/external storage, and no runtime permission is needed.
 */
class ImageStore(context: Context) {

    private val appContext = context.applicationContext
    private val dir: File = File(appContext.filesDir, "images").apply { mkdirs() }

    /** Copies the picked image into internal storage and returns the saved file. */
    fun importFromUri(uri: Uri): File? {
        return try {
            val file = File(dir, "img_${System.currentTimeMillis()}.jpg")
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            if (file.length() > 0) file else null
        } catch (e: Exception) {
            null
        }
    }

    fun list(): List<File> =
        dir.listFiles { f -> f.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()

    fun latest(): File? = list().firstOrNull()

    /** Decodes a stored image, downsampled so large photos don't blow up memory. */
    fun loadBitmap(file: File, maxDim: Int = 1200): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            val longEdge = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
            var sample = 1
            while (longEdge / sample > maxDim) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (e: Exception) {
            null
        }
    }
}
