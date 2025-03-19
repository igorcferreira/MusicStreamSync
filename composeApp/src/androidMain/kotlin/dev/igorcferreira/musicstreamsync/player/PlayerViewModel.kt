package dev.igorcferreira.musicstreamsync.player

import androidx.lifecycle.ViewModel
import dev.igorcferreira.musicstreamsync.domain.use_cases.PlayerUseCase
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry
import kotlinx.coroutines.flow.Flow

class PlayerViewModel(
    private val playerUseCase: PlayerUseCase
) : ViewModel() {
    val isPlaying: Flow<Boolean>
        get() = playerUseCase.isPlaying
    val playingItem: Flow<MusicEntry?>
        get() = playerUseCase.playingItem

    fun play() = playerUseCase.play()
    fun play(entry: EntryData) {
        if (entry is MusicEntry) {
            playerUseCase.play(entry)
        } else if (entry is PlaylistEntry) {
            playerUseCase.play(entry)
        }
    }

    fun pause() = playerUseCase.pause()
}
