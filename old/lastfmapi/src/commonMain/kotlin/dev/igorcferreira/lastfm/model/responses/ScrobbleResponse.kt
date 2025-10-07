package dev.igorcferreira.lastfm.model.responses

import dev.igorcferreira.lastfm.model.Scrobble
import kotlinx.serialization.Serializable

@Serializable
internal data class ScrobbleResponse(
    val scrobbles: ScrobbleBody,
) {
    @Serializable
    internal data class ScrobbleBody(
        val scrobble: Scrobble
    )

    val scrobble: Scrobble
        get() = scrobbles.scrobble
}
