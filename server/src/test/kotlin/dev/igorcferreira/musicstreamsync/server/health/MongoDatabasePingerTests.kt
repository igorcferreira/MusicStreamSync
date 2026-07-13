package dev.igorcferreira.musicstreamsync.server.health

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class MongoDatabasePingerTests {
    @Test
    fun testReturnsFalseWhenDatabaseIsUnreachable() =
        runBlocking {
            // Port 1 is never a MongoDB; a short server-selection timeout keeps the
            // driver from waiting out its 30s default before the ping gives up.
            val connection =
                ConnectionString(
                    "mongodb://127.0.0.1:1/musicstreamsync?serverSelectionTimeoutMS=200&connectTimeoutMS=200",
                )
            val client = MongoClient.create(connection)
            try {
                val pinger = MongoDatabasePinger(client.getDatabase("musicstreamsync"))

                var result = true
                val elapsed = measureTime { result = pinger.ping() }

                assertFalse(result, "An unreachable database must ping false, not throw")
                assertFalse(elapsed > 5.seconds, "Ping should give up quickly, took $elapsed")
            } finally {
                client.close()
            }
        }
}
