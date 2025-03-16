package dev.igorcferreira.musicstreamsync.player

import androidx.lifecycle.ViewModel
import dev.igorcferreira.musicstreamsync.domain.use_cases.PlayerUseCase
import dev.igorcferreira.musicstreamsync.model.SongEntry
import kotlinx.coroutines.flow.Flow

class PlayerViewModel(
    private val playerUseCase: PlayerUseCase
) : ViewModel() {
    val isPlaying: Flow<Boolean>
        get() = playerUseCase.isPlaying
    val playingItem: Flow<SongEntry?>
        get() = playerUseCase.playingItem

    fun play() = playerUseCase.play()
    fun play(songEntry: SongEntry) = playerUseCase.play(songEntry)
    fun pause() = playerUseCase.pause()
}