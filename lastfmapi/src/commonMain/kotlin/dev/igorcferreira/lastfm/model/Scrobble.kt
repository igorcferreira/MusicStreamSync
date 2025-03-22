package dev.igorcferreira.lastfm.model

import kotlinx.serialization.Serializable

@Serializable
data class Scrobble(
    val album: CorrectableText,
    val albumArtist: CorrectableText,
    val artist: CorrectableText,
    val track: CorrectableText,
    val timestamp: Long,
)
