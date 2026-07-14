package dev.igorcferreira.musicstreamsync.server

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.igorcferreira.musicstreamsync.server.health.DatabasePinger
import dev.igorcferreira.musicstreamsync.server.health.MongoDatabasePinger
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.OpenApiPlugin
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.ExampleEncoder
import io.github.smiley4.ktoropenapi.config.OpenApiPluginConfig
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

/** Bearer security scheme name; the token API (TASK_4+) applies it to its token routes. */
internal const val SYNC_SHARED_SECRET_SCHEME = "syncSharedSecret"

/** Tag grouping the health and documentation endpoints. */
private const val META_TAG = "meta"

/** OpenAPI content type for the served document (kept distinct from `text/plain`). */
private val OPENAPI_CONTENT_TYPE = ContentType("application", "yaml")

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
    install(ContentNegotiation) {
        json()
    }
    // Routes carry their own OpenAPI documentation; server/openapi.yaml is generated from
    // them (the code is the single source of truth — see spec/AGENT.md and server/README.md).
    install(OpenApi) {
        info {
            title = "MusicStreamSync Sync Server"
            version = "0.1.0"
            description =
                """
                HTTP API of the MusicStreamSync sync server: the native apps push per-user
                Apple Music and Last.fm credentials to it, and a scheduled loop syncs each
                user's Apple Music play history into Last.fm.

                This document is generated from the server's route definitions; the code is
                the single source of truth (see spec/AGENT.md).
                """.trimIndent()
        }
        server {
            url = "http://localhost:8080"
            description = "Local development (docker compose up)"
        }
        externalDocs {
            url = "https://github.com/igorcferreira/MusicStreamSync/tree/main/spec"
            description = "MusicStreamSync sync-server spec suite"
        }
        tags {
            tag(META_TAG) {
                description = "Health and documentation endpoints."
            }
        }
        // Generate schemas and encode examples with kotlinx-serialization so they match the
        // @Serializable wire types the server actually uses. Body types (e.g. HealthResponse)
        // are emitted as reusable component schemas with simple names and referenced via $ref.
        schemas {
            generator =
                SchemaGenerator.kotlinx {
                    referencePath = RefType.OPENAPI_SIMPLE
                    title = null
                }
        }
        examples {
            encoder(ExampleEncoder.kotlinx())
        }
        // Model the bearer scheme now so TASK_4's `/api/*` routes can apply it. No default
        // scheme is set, so the current (unauthenticated) routes carry no security requirement.
        security {
            securityScheme(SYNC_SHARED_SECRET_SCHEME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                description =
                    """
                    Shared-secret bearer token (the server's SYNC_SHARED_SECRET environment
                    variable). Required by every endpoint from the token API (TASK_4) onwards;
                    /health and /openapi.yaml are unauthenticated.
                    """.trimIndent()
            }
        }
        outputFormat = OutputFormat.YAML
    }
    routing {
        get("/health", {
            operationId = "getHealth"
            summary = "Service health"
            description =
                """
                Liveness/readiness probe. Always returns 200 while the process is up; `mongo`
                reflects a live ping to MongoDB and is `false` when the database is unreachable
                (the endpoint deliberately does not switch to 503 — the process itself is
                healthy and degradation is signaled in the payload).
                """.trimIndent()
            tags = listOf(META_TAG)
            protected = false
            response {
                code(HttpStatusCode.OK) {
                    description = "Service is up."
                    body<HealthResponse> {
                        description = "Process health, with the result of a live MongoDB ping."
                        example("healthy") {
                            summary = "MongoDB reachable"
                            value = HealthResponse(status = "ok", mongo = true)
                        }
                        example("degraded") {
                            summary = "MongoDB unreachable"
                            description = "Process is healthy but the database ping failed."
                            value = HealthResponse(status = "ok", mongo = false)
                        }
                    }
                }
            }
        }) {
            call.respond(HealthResponse(status = "ok", mongo = pinger.ping()))
        }
        get("/openapi.yaml", {
            operationId = "getOpenApiDocument"
            summary = "This document"
            description = "Returns this OpenAPI document, generated from the server's route definitions."
            tags = listOf(META_TAG)
            protected = false
            response {
                code(HttpStatusCode.OK) {
                    description = "The OpenAPI 3.1 document describing this server."
                    body<String> {
                        mediaTypes(OPENAPI_CONTENT_TYPE)
                        description = "The generated OpenAPI document, as YAML."
                        example("document") {
                            summary = "Excerpt (full document elided)"
                            value =
                                """
                                openapi: 3.1.0
                                info:
                                  title: MusicStreamSync Sync Server
                                  version: 0.1.0
                                # paths and components elided
                                """.trimIndent()
                        }
                    }
                }
            }
        }) {
            // getOpenApiSpec reads the document the plugin generated once at startup (a cached
            // lookup, not a per-request rebuild). The drift-guard test exercises this path, so a
            // generation failure is caught in CI rather than only surfacing as a runtime 500.
            call.respondText(
                OpenApiPlugin.getOpenApiSpec(OpenApiPluginConfig.DEFAULT_SPEC_ID),
                OPENAPI_CONTENT_TYPE,
            )
        }
    }
}

@Serializable
internal data class HealthResponse(
    val status: String,
    val mongo: Boolean,
)
