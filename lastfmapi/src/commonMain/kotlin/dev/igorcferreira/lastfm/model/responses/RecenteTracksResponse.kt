package dev.igorcferreira.lastfm.model.responses

import dev.igorcferreira.lastfm.model.Track
import kotlinx.serialization.Serializable

@Serializable
internal data class RecenteTracksResponse(
    val recenttracks: Metadata
) {
    @Serializable
    data class Metadata(
        val track: List<Track>,
    )

    val tracks: List<Track>
        get() = recenttracks.track
}
