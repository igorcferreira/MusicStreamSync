package dev.igorcferreira.musicstreamsync.network

import dev.igorcferreira.musicstreamsync.network.model.HTTPException
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.native.HiddenFromObjC

interface IURLSession {
    suspend fun <T> perform(
        deserializer: DeserializationStrategy<T>,
        path: String,
        method: HttpMethod,
        headers: Map<String, String> = mapOf(),
        decoder: Json = json,
    ): T

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}

suspend inline fun <reified T> IURLSession.perform(
    path: String,
    method: HttpMethod,
    headers: Map<String, String> = mapOf(),
    decoder: Json = IURLSession.json
): T = perform(
    deserializer = decoder.serializersModule.serializer(),
    path = path,
    method = method,
    headers = headers,
    decoder = decoder
)

@HiddenFromObjC
internal class URLSession(
    private val logger: Logger = Logger.DEFAULT
): IURLSession {
    override suspend fun <T> perform(
        deserializer: DeserializationStrategy<T>,
        path: String,
        method: HttpMethod,
        headers: Map<String, String>,
        decoder: Json,
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

            response.request.headers.forEach { string, strings ->
                println("-H '$string: ${strings.joinToString(", ")}'")
            }

            val body = response.bodyAsText()
            print("Response: $body")
            if (!response.status.isSuccess()) {
                throw HTTPException(response.status.value, response.status.description)
            }

            return decoder.decodeFromString(deserializer, body)
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
