package dev.igorcferreira.lastfm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class Track(
    val name: String,
    val artist: CorrectableText,
    // Absent on the currently-playing entry of user.getRecentTracks (`@attr nowplaying`).
    @SerialName("date") val uts: TrackDate? = null,
    val album: CorrectableText? = null,
) {
    val date: Instant?
        get() = uts?.let { Instant.fromEpochSeconds(it.uts) }

    @Serializable
    data class TrackDate(
        val uts: Long,
    )
}
