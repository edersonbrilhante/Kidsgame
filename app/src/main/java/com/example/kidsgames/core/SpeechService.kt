package com.example.kidsgames.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    /** A word to say, the language, and how fast (1f = normal; lower = clearer/slower). */
    data class Utterance(val text: String, val locale: Locale, val rate: Float = 0.82f)

    private var ready = false
    private val queue = ArrayDeque<Utterance>()
    private var pendingOnReady: (() -> Unit)? = null

    private val _speaking = mutableStateOf(false)
    /** True while a word/sequence is being spoken. Observe to gate navigation, etc. */
    val speaking: State<Boolean> get() = _speaking

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            // Slightly higher pitch is clearer for a young listener; per-utterance rate below.
            tts.setPitch(1.05f)
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

    /** Speak a single word in one language, at an optional rate. */
    fun speak(text: String, locale: Locale, rate: Float = 0.82f) =
        speakSequence(listOf(Utterance(text, locale, rate)))

    /** Speak several words back-to-back, each in its own language. */
    fun speakSequence(items: List<Utterance>) {
        if (items.isEmpty()) return
        _speaking.value = true
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
        val item = queue.poll()
        if (item == null) {
            _speaking.value = false
            return
        }
        val res = tts.setLanguage(item.locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            playNext() // language voice not installed — skip to the next one
            return
        }
        tts.setSpeechRate(item.rate)
        tts.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, "u${item.hashCode()}")
    }

    /** Stop any current/queued speech without shutting the engine down. */
    fun stop() {
        queue.clear()
        _speaking.value = false
        if (ready) tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
