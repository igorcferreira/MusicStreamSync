package dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist

import dev.igorcferreira.musicstreamsync.network.model.apple_music.song.SongAttributes
import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class LibrarySongs(
    val id: String,
    val href: String,
    val attributes: SongAttributes
)
