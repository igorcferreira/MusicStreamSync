package dev.igorcferreira.musicstreamsync.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ServerConfigTests {
    @Test
    fun testAppliesDefaults() {
        val config = ServerConfig.fromEnvironment { key -> if (key == "SYNC_SHARED_SECRET") "secret" else null }

        assertEquals(ServerConfig.DEFAULT_PORT, config.port)
        assertEquals(ServerConfig.DEFAULT_MONGODB_URI, config.mongodbUri)
        assertEquals(ServerConfig.DEFAULT_SYNC_INTERVAL_MINUTES, config.syncIntervalMinutes)
        assertEquals("secret", config.syncSharedSecret)
    }

    @Test
    fun testReadsEnvironmentValues() {
        val environment =
            mapOf(
                "PORT" to "9090",
                "MONGODB_URI" to "mongodb://localhost:27017/other",
                "SYNC_SHARED_SECRET" to "secret",
                "SYNC_INTERVAL_MINUTES" to "15",
            )
        val config = ServerConfig.fromEnvironment(environment::get)

        assertEquals(9090, config.port)
        assertEquals("mongodb://localhost:27017/other", config.mongodbUri)
        assertEquals(15, config.syncIntervalMinutes)
    }

    @Test
    fun testFailsFastWithoutSharedSecret() {
        val failure =
            assertFailsWith<IllegalStateException> {
                ServerConfig.fromEnvironment { null }
            }

        assertTrue(
            failure.message.orEmpty().contains("SYNC_SHARED_SECRET"),
            "Failure message should name the missing variable, was: ${failure.message}",
        )
    }

    @Test
    fun testBlankSharedSecretCountsAsUnset() {
        assertFailsWith<IllegalStateException> {
            ServerConfig.fromEnvironment { key -> if (key == "SYNC_SHARED_SECRET") "  " else null }
        }
    }

    @Test
    fun testMalformedNumbersFallBackToDefaults() {
        val environment =
            mapOf(
                "PORT" to "not-a-port",
                "SYNC_SHARED_SECRET" to "secret",
                "SYNC_INTERVAL_MINUTES" to "soon",
            )
        val config = ServerConfig.fromEnvironment(environment::get)

        assertEquals(ServerConfig.DEFAULT_PORT, config.port)
        assertEquals(ServerConfig.DEFAULT_SYNC_INTERVAL_MINUTES, config.syncIntervalMinutes)
    }
}
