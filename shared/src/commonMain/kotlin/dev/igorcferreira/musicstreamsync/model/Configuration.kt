package dev.igorcferreira.musicstreamsync.model

import dev.igorcferreira.musicstreamsync.domain.TokenSigner
import dev.igorcferreira.musicstreamsync.domain.UserTokenProvider
import dev.igorcferreira.musicstreamsync.domain.createUserTokenProvider
import dev.igorcferreira.musicstreamsync.network.AppleMusicAPI
import dev.igorcferreira.musicstreamsync.network.URLSession

class Configuration internal constructor(
    internal val appleMusicAPI: AppleMusicAPI
) {
    val developerToken: DeveloperToken
        get() = appleMusicAPI.developerToken
    val tokenSigner: TokenSigner
        get() = appleMusicAPI.tokenSigner
    val userTokenProvider: UserTokenProvider
        get() = appleMusicAPI.userTokenProvider

    constructor(
        tokenSigner: TokenSigner
    ) : this(DeveloperToken(), tokenSigner)

    constructor(
        developerToken: DeveloperToken,
        tokenSigner: TokenSigner
    ) : this(
        AppleMusicAPI(
            tokenSigner = tokenSigner,
            userTokenProvider = createUserTokenProvider(),
            developerToken = developerToken,
            urlSession = URLSession()
        )
    )
}
