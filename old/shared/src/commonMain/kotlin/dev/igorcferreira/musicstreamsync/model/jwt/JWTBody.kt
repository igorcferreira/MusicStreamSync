package dev.igorcferreira.musicstreamsync.model.jwt

import kotlinx.serialization.Serializable
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@Serializable
internal data class JWTBody(
    val iss: String,
    val iat: Long,
    val exp: Long,
    val origin: String? = null
)