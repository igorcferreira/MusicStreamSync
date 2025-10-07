package dev.igorcferreira.musicstreamsync.model

data class MusicEntry(
    override val id: String,
    val entryId: String,
    override val title: String,
    val artist: String,
    override val artworkUrl: String,
    val duration: Long,
    val album: String? = null,
    val albumArtist: String? = null,
    val genres: List<String> = listOf()
) : EntryData {
    override val body: String?
        get() = album
    override val footer: String?
        get() = artist

    val isPlaylist: Boolean
        get() = id.startsWith("p.") || id.startsWith("pl.")

    constructor(
        id: String,
        title: String,
        artist: String,
        artworkUrl: String,
        album: String? = null,
        albumArtist: String? = null,
    ) : this(id, id, title, artist, artworkUrl, 0, album, albumArtist, emptyList())
}
