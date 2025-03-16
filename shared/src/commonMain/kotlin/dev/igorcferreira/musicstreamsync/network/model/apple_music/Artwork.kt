package dev.igorcferreira.musicstreamsync.network.model.apple_music

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class Artwork(
    val url: String,
    val height: Int,
    val width: Int,
    val bgColor: String? = null,
    val textColor1: String? = null,
    val textColor2: String? = null,
    val textColor3: String? = null,
    val textColor4: String? = null
)
