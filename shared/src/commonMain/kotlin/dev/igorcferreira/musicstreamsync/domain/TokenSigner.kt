package dev.igorcferreira.musicstreamsync.domain

import co.touchlab.skie.configuration.annotations.SuspendInterop

interface TokenSigner {
    // Implemented in Swift (JWTTokenSigner): keep the Kotlin/Native async
    // requirement so Swift can conform with a plain `async throws` method.
    @SuspendInterop.Disabled
    suspend fun sign(
        jwtToken: String,
        privateKey: String,
    ): String
}
