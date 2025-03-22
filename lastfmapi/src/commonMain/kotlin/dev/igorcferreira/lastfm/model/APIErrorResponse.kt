package dev.igorcferreira.lastfm.model

import kotlinx.serialization.Serializable

@Serializable
data class APIErrorResponse(
    val message: String,
    val error: Int
)
