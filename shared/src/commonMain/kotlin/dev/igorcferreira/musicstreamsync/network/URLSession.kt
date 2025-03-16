package dev.igorcferreira.musicstreamsync.network

import dev.igorcferreira.musicstreamsync.network.model.HTTPException
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
internal class URLSession(
    private val decoder: Json = Json { ignoreUnknownKeys = true },
    private val logger: Logger = Logger.DEFAULT
) {
    @Throws(HTTPException::class, CancellationException::class)
    suspend inline fun <reified T> perform(
        path: String,
        method: HttpMethod,
        headers: Map<String, String> = mapOf()
    ): T {
        val client = buildClient()

        try {
            val response = client.request(path) {
                headers {
                    headers.forEach { (key, value) -> header(key, value) }
                    header(HttpHeaders.Accept, ContentType.Application.Json)
                }
                this.method = method
            }

            if (!response.status.isSuccess()) {
                throw HTTPException(response.status.value, response.status.description)
            }

            return decoder.decodeFromString(response.bodyAsText())
        } catch (ex: SerializationException) {
            logger.log("Error: ${ex.message}")
            throw HTTPException(HttpStatusCode.UnprocessableEntity.value, ex.message ?: "")
        } catch (ex: IllegalArgumentException) {
            logger.log("Error: ${ex.message}")
            throw HTTPException(HttpStatusCode.UnprocessableEntity.value, ex.message ?: "")
        } catch (ex: Exception) {
            logger.log("Error: ${ex.message}")
            throw HTTPException(505, ex.message ?: "")
        } finally {
            client.close()
        }
    }

    private fun buildClient(): HttpClient = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
            format = LoggingFormat.OkHttp
            sanitizeHeader { it == HttpHeaders.Authorization }
            sanitizeHeader { it == "Music-User-Token" }
        }
    }
}