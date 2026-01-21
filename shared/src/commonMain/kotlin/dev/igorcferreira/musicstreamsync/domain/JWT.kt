package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.musicstreamsync.model.IDeveloperToken
import dev.igorcferreira.musicstreamsync.model.jwt.JWTBody
import dev.igorcferreira.musicstreamsync.model.jwt.JWTHeader
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.native.HiddenFromObjC
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@HiddenFromObjC
internal suspend fun IDeveloperToken.signWith(
    signer: TokenSigner
): String {
    val header = JWTHeader(
        alg = "ES256",
        kid = keyId
    )
    val body = JWTBody(
        iss = teamId,
        iat = Clock.System.now().epochSeconds,
        exp = Clock.System.now().plus(1.days).epochSeconds
    )

    val unsignedJWT = listOf(
        Json.encodeToString(header),
        Json.encodeToString(body),
    ).map(String::encodeToByteArray).joinToString(".", transform = ByteArray::toBase64)

    return signer.sign(unsignedJWT, privateKey)
}

@HiddenFromObjC
@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.toBase64(): String = Base64.encode(this)
