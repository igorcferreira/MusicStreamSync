package dev.igorcferreira.musicstreamsync.model.jwt

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class JWTHeader(
    val alg: String,
    val kid: String
)
