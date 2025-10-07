package dev.igorcferreira.musicstreamsync.domain

interface TokenSigner {
    suspend fun sign(
        jwtToken: String,
        privateKey: String
    ): String
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class JWTTokenSigner() : TokenSigner