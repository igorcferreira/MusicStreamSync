package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.ResultUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry

@Suppress("unused")
class PlaylistsUseCase(
    private val configuration: Configuration,
) : ResultUseCase<List<PlaylistEntry>>(listOf()) {

    override suspend fun operate() = configuration.appleMusicAPI
        .getUserPlaylists()

}
