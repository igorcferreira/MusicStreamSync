package dev.igorcferreira.musicstreamsync.server

import dev.igorcferreira.musicstreamsync.server.health.DatabasePinger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the checked-in [server/openapi.yaml] against the document generated from the
 * documented routes (the code is the single source of truth — spec/AGENT.md).
 *
 * - Default run (`:server:test`): asserts the served document equals the checked-in snapshot
 *   (the drift guard) and meets the completeness bar.
 * - Regeneration run (`:server:generateOpenApi`, which sets `-Dopenapi.generate=true`): writes
 *   the served document to the snapshot file instead of asserting equality.
 */
class OpenApiDocumentTest {
    private val snapshot: File =
        System.getProperty("openapi.file")?.let(::File) ?: File("openapi.yaml")
    private val shouldRegenerate: Boolean = System.getProperty("openapi.generate") == "true"

    private fun servedDocument(block: (String) -> Unit) =
        testApplication {
            application { module(DatabasePinger { true }) }
            block(client.get("/openapi.yaml").bodyAsText())
        }

    @Test
    fun servedDocumentMatchesCheckedInSnapshot() =
        servedDocument { document ->
            if (shouldRegenerate) {
                snapshot.writeText(document)
                return@servedDocument
            }
            assertTrue(
                snapshot.isFile,
                "Missing ${snapshot.path}; run ./gradlew :server:generateOpenApi",
            )
            assertEquals(
                snapshot.readText(),
                document,
                "server/openapi.yaml is out of sync with the code; run ./gradlew :server:generateOpenApi",
            )
        }

    @Test
    fun generatedDocumentIsValidOpenApi3() =
        servedDocument { document ->
            assertTrue(document.startsWith("openapi: 3.1"), "Should be an OpenAPI 3.1 document")
            assertContainsAll(
                document,
                "title: MusicStreamSync Sync Server",
                "version: 0.1.0",
            )
        }

    @Test
    fun everyPathIsDocumentedWithResponsesExamplesAndSecurityScheme() =
        servedDocument { document ->
            // Both operations, with summaries and operationIds.
            assertContainsAll(
                document,
                "/health:",
                "operationId: getHealth",
                "summary: Service health",
                "/openapi.yaml:",
                "operationId: getOpenApiDocument",
            )
            // Health responses reference the reusable schema (not inlined) and carry both examples.
            assertContainsAll(
                document,
                "\$ref: \"#/components/schemas/HealthResponse\"",
                "healthy:",
                "degraded:",
                "status: ok",
                "mongo: false",
            )
            // The document response advertises the application/yaml media type.
            assertTrue("application/yaml" in document, "openapi.yaml response should be application/yaml")
            // The bearer scheme is modelled for the token API (TASK_4), even though no route uses it yet.
            assertContainsAll(
                document,
                "securitySchemes:",
                "syncSharedSecret:",
                "scheme: bearer",
                "type: http",
            )
        }

    private fun assertContainsAll(
        document: String,
        vararg fragments: String,
    ) = fragments.forEach { fragment ->
        assertTrue(fragment in document, "Generated document should contain: $fragment")
    }
}
