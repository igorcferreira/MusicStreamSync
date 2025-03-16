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
): UserTokenProvider {
    object UserTokenNotSetException : Exception()

    actual constructor(): this(MusicKitBridge())

    override suspend fun getUserToken(
        developerToken: String
    ): String = suspendCoroutine { continuation ->
        bridge.getUserTokenWithDeveloperToken(developerToken) { userToken, error ->
            if (error != null) {
                continuation.resumeWithException(Exception(error.localizedDescription))
            } else if (userToken == null) {
                continuation.resumeWithException(UserTokenNotSetException)
            } else {
                continuation.resume(userToken)
            }
        }
    }
}

internal actual fun createUserTokenProvider(): UserTokenProvider = MusicUserTokenProvider()