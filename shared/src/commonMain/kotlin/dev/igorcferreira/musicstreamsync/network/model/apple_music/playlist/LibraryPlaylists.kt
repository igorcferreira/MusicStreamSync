package dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class LibraryPlaylists(
    val id: String,
    val type: String,
    val href: String,
    val attributes: LibraryPlaylistsAttributes,
    val relationships: LibraryPlaylistsRelationships? = null
)
