package dev.igorcferreira.lastfm.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class Scrobble(
    val track: CorrectableText,
    val artist: CorrectableText,
    val timestamp: Long,
    val album: CorrectableText? = null,
    val albumArtist: CorrectableText? = null,
) {
    @OptIn(ExperimentalTime::class)
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
