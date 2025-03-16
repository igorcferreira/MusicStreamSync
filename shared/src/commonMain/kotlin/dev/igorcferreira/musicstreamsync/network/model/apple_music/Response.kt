package dev.igorcferreira.musicstreamsync.network.model.apple_music

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class Response<T>(
    val next: String? = null,
    val data: List<T>
)
