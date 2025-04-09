package dev.igorcferreira.lastfm.network

import com.russhwolf.settings.Settings
import dev.igorcferreira.lastfm.model.APIErrorResponse
import dev.igorcferreira.lastfm.model.HTTPException
import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import dev.igorcferreira.lastfm.session
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmOverloads

internal class API(
    private val keyHasher: KeyHasher,
    private val settings: Settings,
    private val endpoint: String = "https://ws.audioscrobbler.com/2.0",
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    internal suspend inline fun <reified T> get(
        method: String,
        parameters: Map<String, String> = emptyMap(),
        complement: Boolean = true
    ): T {
        val client = buildClient()
        try {
            val query = if (complement) parameters.complement(method, null) else {
                parameters
            }
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
        parameters: Map<String, String>,
        complement: Boolean = true
    ): T {
        val client = buildClient()
        try {
            val response = client.post(endpoint) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    if (complement) parameters.complement(method, null) else {
                        parameters
                    }.forEach { (key, value) ->
                        append(key, value)
                    }
                }))
            }

            if (!response.status.isSuccess()) {
                val message = try {
                    json.decodeFromString<APIErrorResponse>(response.bodyAsText()).message
                } catch (ignored: Exception) {
                    ""
                }
                throw HTTPException(response.status, message)
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
        userSession: String?
    ): Map<String, String> {
        val allParameters = toMutableMap().apply {
            put("method", method)
            put("api_key", keyHasher.apiKey)
        }

        settings.session?.let { allParameters["sk"] = it.key }

        allParameters["api_sig"] = keyHasher.hash(allParameters)
        allParameters["format"] = "json"
        userSession?.let { allParameters["sk"] = it }

        if (PlatformUtils.IS_DEVELOPMENT_MODE) {
            Logger.DEFAULT.log("Body: $allParameters")
        }

        return allParameters
    }

    private fun buildClient(): HttpClient = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = if (PlatformUtils.IS_DEVELOPMENT_MODE) LogLevel.HEADERS else LogLevel.INFO
            format = LoggingFormat.OkHttp
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
    }

    companion object {
        const val SESSION_KEY = "LastFMClient.API.SESSION_KEY"
    }
}
