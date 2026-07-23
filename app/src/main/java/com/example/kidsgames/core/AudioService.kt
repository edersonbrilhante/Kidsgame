package com.example.kidsgames.core

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

/**
 * Lightweight feedback using the built-in ToneGenerator and haptics.
 * Uses no bundled audio files so the project builds without binary assets;
 * swap in SoundPool + real sfx later if desired.
 */
class AudioService(context: Context) {

    private val appContext = context.applicationContext
    private val vibrator: Vibrator? =
        ContextCompat.getSystemService(appContext, Vibrator::class.java)

    private val tone: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 90)
    } catch (e: RuntimeException) {
        null
    }

    fun playTap() {
        safeTone(ToneGenerator.TONE_PROP_BEEP, 80)
        buzz(20)
    }

    fun playCorrect() {
        safeTone(ToneGenerator.TONE_PROP_ACK, 150)
        buzz(35)
    }

    fun playWin() {
        safeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
        buzz(90)
    }

    private fun safeTone(type: Int, durationMs: Int) {
        try {
            tone?.startTone(type, durationMs)
        } catch (e: RuntimeException) {
            // ignore — feedback is best-effort
        }
    }

    private fun buzz(ms: Long) {
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // ignore
        }
    }
}
