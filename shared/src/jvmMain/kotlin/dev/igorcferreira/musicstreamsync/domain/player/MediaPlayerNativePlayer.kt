package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry

/**
 * Inert JVM stub: the sync server never plays media, this actual only satisfies the
 * `expect` declaration. It never plays, reports [NativePlayer.PlayerState.NOT_PLAYING],
 * and emits no items.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {
    actual override val elapsedTime: Double = 0.0
    actual override val playerState: NativePlayer.PlayerState = NativePlayer.PlayerState.NOT_PLAYING
    actual override val isPlaying: Boolean = false
    actual override val currentPlaying: MusicEntry? = null

    actual override fun startPlayback() = Unit

    actual override fun set(queue: List<EntryData>) = Unit

    actual override fun stopPlayback() = Unit

    actual override fun pausePlayback() = Unit
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()
