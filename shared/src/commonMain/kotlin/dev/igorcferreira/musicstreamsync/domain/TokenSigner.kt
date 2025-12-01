package dev.igorcferreira.musicstreamsync.domain

interface TokenSigner {
    suspend fun sign(
        jwtToken: String,
        privateKey: String
    ): String
}
