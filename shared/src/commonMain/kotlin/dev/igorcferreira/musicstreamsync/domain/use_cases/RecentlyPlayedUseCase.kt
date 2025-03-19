package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.ResultUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.MusicEntry

class RecentlyPlayedUseCase(
    private val configuration: Configuration,
) : ResultUseCase<List<MusicEntry>>(listOf()) {
    override suspend fun operate() = configuration.appleMusicAPI
        .getRecentlyPlayed()
}
