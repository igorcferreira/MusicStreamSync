package dev.igorcferreira.musicstreamsync.domain.use_cases

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.igorcferreira.musicstreamsync.domain.UseCase
import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer
import dev.igorcferreira.musicstreamsync.domain.player.buildNativePlayer
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("unused", "MemberVisibilityCanBePrivate")
class PlayerUseCase(
    private val nativePlayer: NativePlayer,
) : UseCase() {

    constructor() : this(buildNativePlayer())

    @NativeCoroutinesState
    val isPlaying: StateFlow<Boolean>
        get() = PlayerWatcher.getWatcher(nativePlayer)._isPlaying.asStateFlow()
    @NativeCoroutinesState
    val playingItem: StateFlow<MusicEntry?>
        get() = PlayerWatcher.getWatcher(nativePlayer)._playingItem.asStateFlow()
    val playerState: NativePlayer.PlayerState
        get() = nativePlayer.playerState

    fun play() = nativePlayer.currentPlaying?.let { play(it) }
    fun pause() = nativePlayer.pausePlayback()
    fun stop() = nativePlayer.stopPlayback()
    fun play(item: EntryData) {
        if (nativePlayer.currentPlaying?.id == item.id) {
            nativePlayer.startPlayback()
        } else {
            nativePlayer.stopPlayback()
            nativePlayer.set(listOf(item))
            nativePlayer.startPlayback()
        }
    }
}
