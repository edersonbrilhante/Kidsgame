package com.example.matteogames.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.matteogames.core.AudioService
import com.example.matteogames.core.ImageStore
import com.example.matteogames.core.SettingsRepository
import com.example.matteogames.core.SpeechService

/** Shared plumbing so individual minigames don't re-implement audio, storage, etc. */
interface GameServices {
    val audio: AudioService
    val imageStore: ImageStore
    val settings: SettingsRepository

    /** Speaks words aloud (the child cannot read yet). */
    val speech: SpeechService

    /** Reusable "you won" feedback. */
    fun celebrate()
}

class DefaultGameServices(
    override val audio: AudioService,
    override val imageStore: ImageStore,
    override val settings: SettingsRepository,
    override val speech: SpeechService,
) : GameServices {
    override fun celebrate() {
        audio.playWin()
    }
}

@Composable
fun rememberGameServices(): GameServices {
    val context = LocalContext.current.applicationContext
    val services = remember {
        DefaultGameServices(
            audio = AudioService(context),
            imageStore = ImageStore(context),
            settings = SettingsRepository(context),
            speech = SpeechService(context),
        )
    }
    // Release the TextToSpeech engine when the app leaves composition.
    DisposableEffect(services) {
        onDispose { services.speech.shutdown() }
    }
    return services
}
