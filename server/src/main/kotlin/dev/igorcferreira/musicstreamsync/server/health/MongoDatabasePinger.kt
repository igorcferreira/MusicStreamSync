package dev.igorcferreira.musicstreamsync.server.health

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.withTimeoutOrNull
import org.bson.BsonDocument
import org.bson.BsonInt64
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class MongoDatabasePinger(
    private val database: MongoDatabase,
) : DatabasePinger {
    override suspend fun ping(): Boolean =
        try {
            withTimeoutOrNull(PING_TIMEOUT) {
                database.runCommand(BsonDocument("ping", BsonInt64(1)))
            } != null
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            logger.warn("MongoDB ping failed", ex)
            false
        }

    private companion object {
        private val PING_TIMEOUT = 2.seconds
        private val logger = LoggerFactory.getLogger(MongoDatabasePinger::class.java)
    }
}
