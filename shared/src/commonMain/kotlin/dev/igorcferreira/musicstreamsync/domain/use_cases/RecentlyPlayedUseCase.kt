package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.ResultUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.SongEntry
import dev.igorcferreira.musicstreamsync.network.model.apple_music.Response
import dev.igorcferreira.musicstreamsync.network.model.apple_music.SongResource
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.milliseconds

class RecentlyPlayedUseCase(
    private val configuration: Configuration,
) : ResultUseCase<List<SongEntry>>(listOf()) {
    override suspend fun operate() = configuration.appleMusicAPI
        .getHistory()
        .map()

    @HiddenFromObjC
    private fun Response<SongResource>.map() = data
        .distinctBy { it.id }
        .map { it.map() }

    @HiddenFromObjC
    private fun SongResource.map() = SongEntry(
        id = id,
        title = attributes.name,
        artist = attributes.artistName,
        artworkUrl = attributes.artwork.url
            .replace("{w}", attributes.artwork.width.toString())
            .replace("{h}", attributes.artwork.height.toString()),
        duration = attributes.durationInMillis.milliseconds.inWholeSeconds,
        album = attributes.albumName
    )
}