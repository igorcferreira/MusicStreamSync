package dev.igorcferreira.musicstreamsync.model

import dev.igorcferreira.musicstreamsync.domain.JWTTokenSigner
import dev.igorcferreira.musicstreamsync.domain.createUserTokenProvider
import dev.igorcferreira.musicstreamsync.network.AppleMusicAPI
import dev.igorcferreira.musicstreamsync.network.URLSession

class Configuration internal constructor(
    internal val appleMusicAPI: AppleMusicAPI
) {
    constructor(
        developerToken: DeveloperToken
    ) : this(
        AppleMusicAPI(
            tokenSigner = JWTTokenSigner(),
            userTokenProvider = createUserTokenProvider(),
            developerToken = developerToken,
            urlSession = URLSession()
        )
    )
}