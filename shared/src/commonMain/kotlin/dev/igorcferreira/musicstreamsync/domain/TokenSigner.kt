package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.musicstreamsync.model.PrivateKey

interface TokenSigner {
    suspend fun sign(
        jwtToken: String,
        privateKey: PrivateKey
    ): String
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class JWTTokenSigner(): TokenSigner