package dev.igorcferreira.lastfm.network.authentication

import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import org.kotlincrypto.hash.md.MD5

internal class KeyHasher(
    val apiKey: String,
    private val apiSecret: String
) {
    fun hash(parameters: Map<String, String>): String {
        val components = mutableListOf<String>()
        parameters.keys.sorted().forEach { key ->
            val value = parameters[key] ?: return@forEach
            components.add("$key$value")
        }
        components.add(apiSecret)
        val base = components.joinToString("")
        return base.hashMD5()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hashMD5(): String {
        val hasher = MD5()
        val hash = hasher.digest(this.toByteArray(Charsets.UTF_8))
        return hash.toHexString()
    }
}
