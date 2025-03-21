package dev.igorcferreira.lastfm.network

import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.json.Json

internal class API(
    private val keyHasher: KeyHasher,
    private val endpoint: String = "https://ws.audioscrobbler.com/2.0",
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    class HTTPException(val code: HttpStatusCode, message: String): Exception(message)

    internal suspend inline fun <reified T> get(
        method: String,
        parameters: Map<String, String> = emptyMap()
    ): T {
        val client = buildClient()
        try {
            val query = parameters.complement(method)
                .map { (key, value) -> "$key=$value" }
                .joinToString("&")

            val response = client.get("$endpoint?$query")
            if (!response.status.isSuccess()) {
                throw HTTPException(response.status, response.bodyAsText())
            }

            if (T::class == String::class) {
                return response.bodyAsText() as T
            }

            return json.decodeFromString(response.bodyAsText())
        } finally {
            client.close()
        }
    }

    internal suspend inline fun <reified T> post(
        method: String,
        parameters: Map<String, String>
    ): T {
        val client = buildClient()
        try {
            val response = client.post(endpoint) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    parameters.complement(method).forEach { (key, value) ->
                        append(key, value)
                    }
                }))
            }

            if (!response.status.isSuccess()) {
                throw HTTPException(response.status, response.bodyAsText())
            }

            if (T::class == String::class) {
                return response.bodyAsText() as T
            }

            return json.decodeFromString(response.bodyAsText())
        } finally {
            client.close()
        }
    }

    private fun Map<String, String>.complement(
        method: String,
        userSession: String? = null
    ): Map<String, String> {
        val allParameters = toMutableMap().apply {
            put("method", method)
            put("api_key", keyHasher.apiKey)
        }
        allParameters["api_sig"] = keyHasher.hash(allParameters)
        allParameters["format"] = "json"
        userSession?.let { allParameters["sk"] = it }
        return allParameters
    }

    private fun buildClient(): HttpClient = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = if (PlatformUtils.IS_DEVELOPMENT_MODE) LogLevel.ALL else LogLevel.INFO
            format = LoggingFormat.OkHttp
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
    }
}
