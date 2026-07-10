package dev.igorcferreira.lastfm.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTests {
    @Test
    fun testDecodesMinimalWirePayload() {
        val session = Json.decodeFromString<Session>("""{"name":"igor","key":"session-key"}""")

        assertEquals("igor", session.name)
        assertEquals("session-key", session.key)
        assertEquals(0, session.subscriber)
    }

    @Test
    fun testDecodesFullWirePayload() {
        val session = Json.decodeFromString<Session>("""{"name":"igor","key":"session-key","subscriber":1}""")

        assertEquals(1, session.subscriber)
    }

    @Test
    fun testEncodeDecodeRoundTrip() {
        val session = Session(name = "igor", key = "session-key", subscriber = 1)
        val decoded = Json.decodeFromString<Session>(Json.encodeToString(Session.serializer(), session))

        assertEquals(session, decoded)
    }
}
