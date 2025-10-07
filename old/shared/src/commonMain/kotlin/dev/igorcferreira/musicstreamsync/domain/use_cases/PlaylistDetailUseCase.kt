package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.ResultUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry

@Suppress("unused")
class PlaylistDetailUseCase(
    private val entry: PlaylistEntry,
    private val configuration: Configuration,
) : ResultUseCase<PlaylistEntry>(entry) {
    override suspend fun operate() = configuration.appleMusicAPI.getPlaylist(entry.id) ?: entry
}
