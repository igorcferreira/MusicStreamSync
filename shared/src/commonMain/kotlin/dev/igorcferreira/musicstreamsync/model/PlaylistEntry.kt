package dev.igorcferreira.musicstreamsync.model

data class PlaylistEntry(
    override val id: String,
    val entryId: String,
    val name: String,
    val canEdit: Boolean,
    val hasCatalog: Boolean,
    val isPublic: Boolean,
    val dateAdded: String? = null,
    val lastModifiedDate: String? = null,
    val entryDescription: String? = null,
    override val artworkUrl: String? = null,
    val songs: List<MusicEntry>? = null
) : EntryData {
    override val title: String
        get() = name
    override val body: String?
        get() = entryDescription
    override val footer: String?
        get() = null
}
