package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry

interface NativePlayer {
    enum class PlayerState {
        PLAYING,
        NOT_PLAYING,
        UNKNOWN
    }

    val elapsedTime: Double
    val playerState: PlayerState
    val isPlaying: Boolean
    val currentPlaying: MusicEntry?
    fun startPlayback()
    fun set(queue: List<EntryData>)
    fun stopPlayback()
    fun pausePlayback()
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MediaPlayerNativePlayer : NativePlayer {
    override val elapsedTime: Double
    override val playerState: NativePlayer.PlayerState
    override val isPlaying: Boolean
    override val currentPlaying: MusicEntry?
    override fun startPlayback()
    override fun set(queue: List<EntryData>)
    override fun stopPlayback()
    override fun pausePlayback()
}

internal expect fun buildNativePlayer(): MediaPlayerNativePlayer
