package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.crypt.KCrypto
import dev.igorcferreira.musicstreamsync.model.PrivateKey
import kotlinx.cinterop.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class JWTTokenSigner(
    private val crypt: KCrypto
): TokenSigner {
    class SignatureException: Exception()

    actual constructor(): this(KCrypto())

    override suspend fun sign(jwtToken: String, privateKey: PrivateKey): String {
        val key = crypt.loadWithPemKey(privateKey, null)  ?: throw SignatureException()
        val signature = crypt.sign(jwtToken, key, null) ?: throw SignatureException()
        return "$jwtToken.$signature"
    }
}
