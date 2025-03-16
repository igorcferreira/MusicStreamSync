package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.SongEntry

interface NativePlayer {
    val isPlaying: Boolean
    val currentPlaying: SongEntry?
    fun startPlayback()
    fun set(queue: List<SongEntry>)
    fun stopPlayback()
    fun pausePlayback()
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MediaPlayerNativePlayer : NativePlayer

internal expect fun buildNativePlayer(): MediaPlayerNativePlayer