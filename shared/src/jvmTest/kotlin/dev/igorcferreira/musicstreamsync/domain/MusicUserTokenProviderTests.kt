package dev.igorcferreira.musicstreamsync.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MusicUserTokenProviderTests {
    @Test
    fun testReturnsInjectedToken() =
        runTest {
            val provider = MusicUserTokenProvider(token = "user-token")
            assertEquals("user-token", provider.getUserToken(developerToken = "developer-token"))
        }

    @Test
    fun testReturnsTokenInjectedAfterConstruction() =
        runTest {
            val provider = MusicUserTokenProvider()
            provider.token = "late-token"
            assertEquals("late-token", provider.getUserToken(developerToken = "developer-token"))
        }

    @Test
    fun testThrowsWhenNoTokenInjected() =
        runTest {
            val provider = MusicUserTokenProvider()
            assertFailsWith<MusicUserTokenProvider.UserTokenNotSetException> {
                provider.getUserToken(developerToken = "developer-token")
            }
        }
}
