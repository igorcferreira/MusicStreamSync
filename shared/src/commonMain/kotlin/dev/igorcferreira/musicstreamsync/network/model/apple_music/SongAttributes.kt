package dev.igorcferreira.musicstreamsync.network.model.apple_music

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class SongAttributes(
    val name: String,
    val albumName: String,
    val artistName: String,
    val artwork: Artwork,
    val url: String,
    val durationInMillis: Long,
    val isrc: String,
    val artistUrl: String? = null,
    val genreNames: List<String> = listOf()
)