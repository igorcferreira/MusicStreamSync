package dev.igorcferreira.musicstreamsync.network.model.apple_music.song

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal class SongResource(
    val id: String,
    val type: String,
    val href: String,
    val attributes: SongAttributes
)
