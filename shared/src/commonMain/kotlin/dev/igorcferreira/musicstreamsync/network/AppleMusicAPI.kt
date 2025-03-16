package dev.igorcferreira.musicstreamsync.network

import dev.igorcferreira.musicstreamsync.domain.TokenSigner
import dev.igorcferreira.musicstreamsync.domain.UserTokenProvider
import dev.igorcferreira.musicstreamsync.domain.signWith
import dev.igorcferreira.musicstreamsync.model.DeveloperToken
import dev.igorcferreira.musicstreamsync.network.model.HTTPException
import dev.igorcferreira.musicstreamsync.network.model.apple_music.Response
import dev.igorcferreira.musicstreamsync.network.model.apple_music.SongResource
import io.ktor.http.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
internal open class AppleMusicAPI(
    private val host: String = "https://api.music.apple.com/v1",
    private val tokenSigner: TokenSigner,
    private val userTokenProvider: UserTokenProvider,
    private val developerToken: DeveloperToken,
    private val urlSession: URLSession
) {
    @Throws(HTTPException::class, CancellationException::class)
    open suspend fun getHistory(): Response<SongResource> = get(
        "/me/recent/played/tracks?extend=artistUrl&l=en"
    )

    private suspend inline fun <reified T> get(
        path: String
    ): T = urlSession.perform("$host$path", HttpMethod.Get, buildHeaders())

    private suspend fun buildHeaders(): Map<String, String> {
        val developerToken = buildDeveloperToken()
        val userToken = userTokenProvider.getUserToken(developerToken)
        return mapOf(
            HttpHeaders.Authorization to "Bearer $developerToken",
            "Music-User-Token" to userToken
        )
    }

    private suspend fun buildDeveloperToken(): String = developerToken
        .signWith(tokenSigner)
}