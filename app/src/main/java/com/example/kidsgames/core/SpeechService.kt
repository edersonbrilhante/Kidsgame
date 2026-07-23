package com.example.kidsgames.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.ArrayDeque
import java.util.Locale

/**
 * Thin wrapper over Android [TextToSpeech]. The child cannot read yet, so every word is
 * spoken aloud. Supports speaking a short sequence across different languages
 * (English → Polish → Portuguese) by switching the engine's locale between utterances.
 *
 * No runtime permission is required. If a language's voice data is missing on the device,
 * that utterance is skipped quietly rather than failing.
 */
class SpeechService(context: Context) {

    /** A word to say plus the language to say it in. */
    data class Utterance(val text: String, val locale: Locale)

    private var ready = false
    private val queue = ArrayDeque<Utterance>()
    private var pendingOnReady: (() -> Unit)? = null

    private val tts = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { playNext() }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { playNext() }
                override fun onError(utteranceId: String?, errorCode: Int) { playNext() }
            })
            pendingOnReady?.invoke()
            pendingOnReady = null
        }
    }

    /** Speak a single word in one language. */
    fun speak(text: String, locale: Locale) = speakSequence(listOf(Utterance(text, locale)))

    /** Speak several words back-to-back, each in its own language. */
    fun speakSequence(items: List<Utterance>) {
        if (items.isEmpty()) return
        if (!ready) {
            pendingOnReady = { startSequence(items) }
            return
        }
        startSequence(items)
    }

    private fun startSequence(items: List<Utterance>) {
        queue.clear()
        queue.addAll(items)
        tts.stop()
        playNext()
    }

    private fun playNext() {
        val item = queue.poll() ?: return
        val res = tts.setLanguage(item.locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            playNext() // language voice not installed — skip to the next one
            return
        }
        tts.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, "u${item.hashCode()}")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
