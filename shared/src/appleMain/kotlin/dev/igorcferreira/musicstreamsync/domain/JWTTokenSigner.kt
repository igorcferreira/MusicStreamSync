package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.crypt.KCrypto
import kotlinx.cinterop.ExperimentalForeignApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class JWTTokenSigner(
    private val crypt: KCrypto
) : TokenSigner {
    class SignatureException : Exception()

    actual constructor() : this(KCrypto())

    override suspend fun sign(jwtToken: String, privateKey: String): String {
        val signature = crypt.sign(jwtToken, privateKey, null) ?: throw SignatureException()
        return "$jwtToken.$signature"
    }
}
