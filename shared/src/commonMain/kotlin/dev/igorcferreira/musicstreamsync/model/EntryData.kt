package dev.igorcferreira.musicstreamsync.model

interface EntryData {
    val id: String
    val title: String
    val artworkUrl: String?
    val body: String?
    val footer: String?
}

val EntryData.entryId: String
    get() = when (this) {
        is PlaylistEntry -> entryId
        is MusicEntry -> entryId
        else -> this.id
    }

val EntryData.isPlaylist: Boolean
    get() = this is PlaylistEntry
