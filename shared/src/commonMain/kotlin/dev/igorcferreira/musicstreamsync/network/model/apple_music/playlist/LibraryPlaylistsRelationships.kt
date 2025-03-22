package dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class LibraryPlaylistsRelationships(
    val tracks: Tracks? = null,
) {
    val href: String?
        get() = tracks?.href
    val next: String?
        get() = tracks?.next
    val data: List<LibrarySongs>
        get() = tracks?.data ?: emptyList()

    @Serializable
    internal data class Tracks(
        val href: String? = null,
        val next: String? = null,
        val data: List<LibrarySongs> = emptyList()
    )
}
