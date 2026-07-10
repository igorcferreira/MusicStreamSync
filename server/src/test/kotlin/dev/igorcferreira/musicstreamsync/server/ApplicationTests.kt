package dev.igorcferreira.musicstreamsync.server

import dev.igorcferreira.musicstreamsync.server.health.DatabasePinger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTests {
    @Test
    fun testHealthReportsMongoUp() =
        testApplication {
            application { module(DatabasePinger { true }) }

            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("""{"status":"ok","mongo":true}""", response.bodyAsText())
        }

    @Test
    fun testHealthStays200WhenMongoIsUnreachable() =
        testApplication {
            application { module(DatabasePinger { false }) }

            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("""{"status":"ok","mongo":false}""", response.bodyAsText())
        }

    @Test
    fun testServesTheOpenApiDocument() =
        testApplication {
            application { module(DatabasePinger { true }) }

            val response = client.get("/openapi.yaml")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType("application", "yaml"), response.contentType()?.withoutParameters())
            val document = response.bodyAsText()
            assertTrue(document.startsWith("openapi:"), "Should serve the OpenAPI document")
            assertTrue("/health" in document, "Document should describe /health")
            assertTrue("syncSharedSecret" in document, "Document should declare the security scheme")
        }
}
