package dev.igorcferreira.musicstreamsync.domain

/**
 * JVM provider for the Apple Music **Music-User-Token**.
 *
 * The token cannot be minted on the JVM (there is no MusicKit login); it is minted
 * on-device by the native apps and injected here, one provider instance per user.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MusicUserTokenProvider(
    var token: String?,
) : UserTokenProvider {
    class UserTokenNotSetException :
        IllegalStateException("No Music-User-Token was injected into this MusicUserTokenProvider")

    actual constructor() : this(null)

    actual override suspend fun getUserToken(developerToken: String): String = token ?: throw UserTokenNotSetException()
}

@Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
internal actual fun createUserTokenProvider(): UserTokenProvider = MusicUserTokenProvider()
