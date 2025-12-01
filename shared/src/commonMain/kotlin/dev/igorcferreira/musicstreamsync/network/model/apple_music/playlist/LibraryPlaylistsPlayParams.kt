package dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class LibraryPlaylistsPlayParams(
    val id: String,
    val globalId: String? = null
)
