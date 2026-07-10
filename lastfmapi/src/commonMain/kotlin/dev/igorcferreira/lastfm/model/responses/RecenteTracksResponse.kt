package dev.igorcferreira.lastfm.model.responses

import dev.igorcferreira.lastfm.model.Track
import kotlinx.serialization.Serializable

@Serializable
internal data class RecenteTracksResponse(
    val recenttracks: Metadata,
) {
    @Serializable
    data class Metadata(
        val track: List<Track>,
    )

    /** All entries as returned by Last.fm, including the `@attr nowplaying` one. */
    val tracks: List<Track>
        get() = recenttracks.track

    /** Tracks with a timestamp — drops the `@attr nowplaying` entry, which has none. */
    val scrobbledTracks: List<Track>
        get() = recenttracks.track.filter { it.uts != null }
}
