package dev.igorcferreira.musicstreamsync.network.model.apple_music.song

import dev.igorcferreira.musicstreamsync.network.model.apple_music.Artwork
import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class SongAttributes(
    val name: String,
    val albumName: String,
    val artistName: String,
    val artwork: Artwork,
    val durationInMillis: Long,
    val url: String? = null,
    val isrc: String? = null,
    val artistUrl: String? = null,
    val genreNames: List<String> = listOf()
)
