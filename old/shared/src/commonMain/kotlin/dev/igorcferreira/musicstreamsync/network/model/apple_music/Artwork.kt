package dev.igorcferreira.musicstreamsync.network.model.apple_music

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class Artwork(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
    val bgColor: String? = null,
    val textColor1: String? = null,
    val textColor2: String? = null,
    val textColor3: String? = null,
    val textColor4: String? = null
) {
    val mappedUrl: String
        get() {
            val width = width ?: 600
            val height = height ?: width

            return url.replace("{w}", "$width")
                .replace("{h}", "$height")
        }
}
