package dev.igorcferreira.lastfm.network.authentication

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHasherTests {
    @Test
    fun testMD5Validity() {
        val hasher = KeyHasher(
            "api-key",
            "api-secret"
        )

        val hashed = hasher.hash(
            mapOf(
                "method" to "track.updateNowPlaying",
                "artist" to "Artist",
                "track" to "Track",
                "album" to "Album"
            )
        )

        assertEquals("2ea21f67c97af5c5370abe183c9874bf", hashed)
    }
}
