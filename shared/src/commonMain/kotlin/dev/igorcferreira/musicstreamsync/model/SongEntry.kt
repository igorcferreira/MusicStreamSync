package dev.igorcferreira.musicstreamsync.model

data class SongEntry(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val duration: Long,
    val album: String? = null,
    val genres: List<String> = listOf()
) {
    constructor(
        id: String,
        title: String,
        artist: String,
        artworkUrl: String,
        album: String? = null
    ) : this(id, title, artist, artworkUrl, 0, album)
}
