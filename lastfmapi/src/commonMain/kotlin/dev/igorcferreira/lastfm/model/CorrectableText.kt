package dev.igorcferreira.lastfm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CorrectableText(
    val corrected: String,
    @SerialName("#text")
    val text: String,
) {
    override fun toString(): String = text
}
