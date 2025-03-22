package dev.igorcferreira.lastfm.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val name: String,
    val key: String,
    val subscriber: Int
)
