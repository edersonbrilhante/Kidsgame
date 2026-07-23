package com.example.kidsgames.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.kidsgames.core.AudioService
import com.example.kidsgames.core.ImageStore
import com.example.kidsgames.core.SettingsRepository

/** Shared plumbing so individual minigames don't re-implement audio, storage, etc. */
interface GameServices {
    val audio: AudioService
    val imageStore: ImageStore
    val settings: SettingsRepository

    /** Reusable "you won" feedback. */
    fun celebrate()
}

class DefaultGameServices(
    override val audio: AudioService,
    override val imageStore: ImageStore,
    override val settings: SettingsRepository,
) : GameServices {
    override fun celebrate() {
        audio.playWin()
    }
}

@Composable
fun rememberGameServices(): GameServices {
    val context = LocalContext.current.applicationContext
    return remember {
        DefaultGameServices(
            audio = AudioService(context),
            imageStore = ImageStore(context),
            settings = SettingsRepository(context),
        )
    }
}
