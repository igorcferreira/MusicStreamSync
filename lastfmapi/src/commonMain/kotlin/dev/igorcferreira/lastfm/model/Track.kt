package dev.igorcferreira.lastfm.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val name: String,
    val artist: CorrectableText,
    @SerialName("date") val uts: TrackDate,
    val album: CorrectableText? = null,
) {
    val date: Instant
        get() = Instant.fromEpochSeconds(uts.uts)

    @Serializable
    data class TrackDate(
        val uts: Long
    )
}
