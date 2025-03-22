package dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist

import dev.igorcferreira.musicstreamsync.network.model.apple_music.Artwork
import dev.igorcferreira.musicstreamsync.network.model.apple_music.DescriptionAttribute
import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class LibraryPlaylistsAttributes(
    val name: String,
    val canEdit: Boolean,
    val hasCatalog: Boolean,
    val isPublic: Boolean,
    val playParams: LibraryPlaylistsPlayParams,
    val dateAdded: String? = null,
    val lastModifiedDate: String? = null,
    val artwork: Artwork? = null,
    val description: DescriptionAttribute? = null,
)
