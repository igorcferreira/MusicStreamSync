package dev.igorcferreira.lastfm.model.responses

import dev.igorcferreira.lastfm.model.NowPlaying
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NowPlayingResponse(
    @SerialName("nowplaying")
    val nowPlaying: NowPlaying,
)
