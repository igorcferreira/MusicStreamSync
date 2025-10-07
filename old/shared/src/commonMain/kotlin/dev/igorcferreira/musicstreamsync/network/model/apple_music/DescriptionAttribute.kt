package dev.igorcferreira.musicstreamsync.network.model.apple_music

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class DescriptionAttribute(
    val standard: String,
    val short: String? = null
)
