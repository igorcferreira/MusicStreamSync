package dev.igorcferreira.lastfm

import dev.igorcferreira.lastfm.model.Session
import dev.igorcferreira.lastfm.network.API
import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import dev.igorcferreira.lastfm.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LastFMClientSessionTests {
    @Test
    fun testSessionExportImportRoundTrip() {
        val original = Session(name = "igor", key = "session-key-a", subscriber = 1)
        val exporter = LastFMClient("api-key", "api-secret", original)

        assertTrue(exporter.isAuthenticated)
        val exported = assertNotNull(exporter.currentSession)
        assertEquals(original, exported)

        val importer = LastFMClient("api-key", "api-secret", exported)

        assertTrue(importer.isAuthenticated)
        assertEquals(original, importer.currentSession)
    }

    @Test
    fun testImportedSessionKeySignsRequests() {
        val settings = InMemorySettings()
        settings.session = Session(name = "igor", key = "imported-key")
        val hasher = KeyHasher("api-key", "api-secret")
        val api = API(hasher, settings)

        val parameters = with(api) { mapOf("artist[0]" to "Artist").complement("track.scrobble", null) }

        assertEquals("imported-key", parameters["sk"])
        val expectedSignature =
            hasher.hash(
                mapOf(
                    "artist[0]" to "Artist",
                    "method" to "track.scrobble",
                    "api_key" to "api-key",
                    "sk" to "imported-key",
                ),
            )
        assertEquals(expectedSignature, parameters["api_sig"])
    }

    @Test
    fun testClientsInOneProcessDoNotShareSessions() {
        val alice = LastFMClient("api-key", "api-secret", Session(name = "alice", key = "alice-key"))
        val bob = LastFMClient("api-key", "api-secret", Session(name = "bob", key = "bob-key"))

        assertEquals("alice-key", alice.currentSession?.key)
        assertEquals("bob-key", bob.currentSession?.key)

        alice.logout()

        assertFalse(alice.isAuthenticated)
        assertNull(alice.currentSession)
        assertTrue(bob.isAuthenticated)
        assertEquals("bob-key", bob.currentSession?.key)
    }

    @Test
    fun testRestoreSessionReplacesTheStoredSession() {
        val client = LastFMClient("api-key", "api-secret", Session(name = "igor", key = "old-key"))

        client.restoreSession(Session(name = "igor", key = "new-key"))

        assertEquals("new-key", client.currentSession?.key)
    }

    @Test
    fun testCurrentSessionIsNullWhenNotAuthenticated() {
        val client = LastFMClient("api-key", "api-secret", InMemorySettings())

        assertNull(client.currentSession)
        assertFalse(client.isAuthenticated)
    }

    @Test
    fun testImportRejectsBlankSessionKey() {
        assertFailsWith<IllegalArgumentException> {
            LastFMClient("api-key", "api-secret", Session(name = "igor", key = ""))
        }
    }

    @Test
    fun testRestoreSessionRejectsBlankSessionKey() {
        val client = LastFMClient("api-key", "api-secret", Session(name = "igor", key = "valid-key"))

        assertFailsWith<IllegalArgumentException> {
            client.restoreSession(Session(name = "igor", key = ""))
        }
        assertEquals("valid-key", client.currentSession?.key)
    }
}
