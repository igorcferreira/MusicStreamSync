package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.musicstreamsync.model.PrivateKey
import dev.igorcferreira.musicstreamsync.model.jwt.JWTHeader
import io.jsonwebtoken.Jwts
import io.ktor.util.*
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class JWTTokenSigner : TokenSigner {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sign(jwtToken: String, privateKey: PrivateKey): String {
        val components = jwtToken.split(".")
        val header = json.decodeFromString<JWTHeader>(components[0].decodeBase64String())

        val spec = PKCS8EncodedKeySpec(privateKey.clearKey())
        val keyFactory = KeyFactory.getInstance("EC")
        val key = keyFactory.generatePrivate(spec)

        val builder = Jwts.builder()
            .header()
            .keyId(header.kid)
            .and()
            .content(components[1].decodeBase64String())

        return builder
            .signWith(key, Jwts.SIG.ES256)
            .compact()
    }
}

internal fun PrivateKey.clearKey(marker: String = "PRIVATE KEY"): ByteArray {
    val content = android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
    val stringContent = String(content, kotlin.text.Charsets.UTF_8)
        .removePrefix("-----BEGIN $marker-----\n")
        .removeSuffix("\n-----END $marker-----")
        .replace("\n", "")
    return android.util.Base64.decode(stringContent, android.util.Base64.NO_WRAP)
}
