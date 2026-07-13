package dev.igorcferreira.musicstreamsync.server

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.igorcferreira.musicstreamsync.server.health.DatabasePinger
import dev.igorcferreira.musicstreamsync.server.health.MongoDatabasePinger
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun main() {
    val config = ServerConfig.fromEnvironment()
    val connection = ConnectionString(config.mongodbUri)
    val client = MongoClient.create(connection)
    val database = client.getDatabase(connection.database ?: ServerConfig.DEFAULT_DATABASE_NAME)

    // Release the driver's connection pool on SIGTERM (docker stop) / JVM exit.
    Runtime.getRuntime().addShutdownHook(Thread(client::close))

    embeddedServer(Netty, port = config.port) {
        module(MongoDatabasePinger(database))
    }.start(wait = true)
}

internal fun Application.module(pinger: DatabasePinger) {
    // Fail fast at startup when the API document was not packaged.
    val openApiDocument =
        checkNotNull(environment::class.java.classLoader.getResource("openapi.yaml")) {
            "openapi.yaml is missing from the server resources"
        }.readText()

    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok", mongo = pinger.ping()))
        }
        get("/openapi.yaml") {
            call.respondText(openApiDocument, ContentType("application", "yaml"))
        }
    }
}

@Serializable
internal data class HealthResponse(
    val status: String,
    val mongo: Boolean,
)
