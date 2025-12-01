package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.os.bridge.MusicKitBridge
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class MusicUserTokenProvider(
    private val bridge: MusicKitBridge
) : UserTokenProvider {
    class UserTokenNotSetException : Exception()

    actual constructor() : this(MusicKitBridge())

    actual override suspend fun getUserToken(
        developerToken: String
    ): String  {
        try {
            val token = bridge.getUserToken(developerToken)
            return token
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun MusicKitBridge.getUserToken(developerToken: String): String = suspendCoroutine { continuation ->
        getUserTokenWithDeveloperToken(developerToken) { userToken, error ->
            if (error != null) {
                continuation.resumeWithException(Exception(error.localizedDescription))
            } else if (userToken == null) {
                continuation.resumeWithException(UserTokenNotSetException())
            } else {
                continuation.resume(userToken)
            }
        }
    }
}

@HiddenFromObjC
internal actual fun createUserTokenProvider(): UserTokenProvider = MusicUserTokenProvider()
