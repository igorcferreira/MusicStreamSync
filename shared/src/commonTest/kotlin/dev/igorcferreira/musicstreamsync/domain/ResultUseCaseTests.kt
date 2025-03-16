package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.musicstreamsync.domain.use_cases.RecentlyPlayedUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.network.AppleMusicAPI
import dev.igorcferreira.musicstreamsync.network.model.apple_music.Artwork
import dev.igorcferreira.musicstreamsync.network.model.apple_music.Response
import dev.igorcferreira.musicstreamsync.network.model.apple_music.SongAttributes
import dev.igorcferreira.musicstreamsync.network.model.apple_music.SongResource
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResultUseCaseTests {

    @Test
    fun testNetworkParsing() = runTest {
        val apiResponse = Response(
            next = "", data = listOf(
                SongResource(
                    id = "1", href = "", type = "song", attributes = SongAttributes(
                        name = "Song Name",
                        albumName = "Album Name",
                        artistName = "Artist",
                        url = "url",
                        durationInMillis = 0,
                        isrc = "",
                        artwork = Artwork(
                            url = "url",
                            width = 100,
                            height = 100
                        )
                    )
                )
            )
        )

        val api = mock<AppleMusicAPI> {
            everySuspend { getHistory() } returns apiResponse
        }
        val configuration = Configuration(api)
        val operation = RecentlyPlayedUseCase(configuration)

        val response = operation.perform()
        assertTrue(response.isNotEmpty())
        assertEquals(response, operation.result.value)

        val first = response.first()
        assertEquals(first.id, "1")
        assertEquals(first.title, "Song Name")
        assertEquals(first.artist, "Artist")
        assertEquals(first.album, "Album Name")
    }

    @Test
    fun testErrorStorage() = runTest {
        val api = mock<AppleMusicAPI> {
            everySuspend { getHistory() } throws RuntimeException()
        }
        val configuration = Configuration(api)
        val operation = RecentlyPlayedUseCase(configuration)

        try {
            operation.perform()
        } catch (ignored: RuntimeException) {
        }

        assertIs<RuntimeException>(operation.error.value)
    }
}