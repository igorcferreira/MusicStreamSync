package dev.igorcferreira.lastfm.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Scrobble(
    val track: CorrectableText,
    val artist: CorrectableText,
    val timestamp: Long,
    val album: CorrectableText? = null,
    val albumArtist: CorrectableText? = null,
) {
    constructor(
        track: String,
        artist: String,
        timestamp: Instant,
        album: String? = null,
        albumArtist: String? = null,
    ) : this(
        track = CorrectableText(track),
        artist = CorrectableText(artist),
        timestamp = timestamp.epochSeconds,
        album = album?.let { CorrectableText(it) },
        albumArtist = albumArtist?.let { CorrectableText(it) }
    )
}
