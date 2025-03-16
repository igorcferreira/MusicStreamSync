package dev.igorcferreira.musicstreamsync.domain

import kotlin.native.HiddenFromObjC

interface UserTokenProvider {
    @Throws(Exception::class)
    suspend fun getUserToken(
        developerToken: String
    ): String
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MusicUserTokenProvider(): UserTokenProvider

@HiddenFromObjC
internal expect fun createUserTokenProvider(): UserTokenProvider