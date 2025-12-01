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
