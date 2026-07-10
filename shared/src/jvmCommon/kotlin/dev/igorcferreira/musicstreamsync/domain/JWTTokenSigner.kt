package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.musicstreamsync.model.jwt.JWTHeader
import io.jsonwebtoken.Jwts
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.io.encoding.Base64

class JWTTokenSigner : TokenSigner {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sign(
        jwtToken: String,
        privateKey: String,
    ): String {
        val components = jwtToken.split(".")
        val header = json.decodeFromString<JWTHeader>(Base64.decode(components[0]).decodeToString())

        val spec = PKCS8EncodedKeySpec(privateKey.clearKey())
        val keyFactory = KeyFactory.getInstance("EC")
        val key = keyFactory.generatePrivate(spec)

        val builder =
            Jwts
                .builder()
                .header()
                .keyId(header.kid)
                .and()
                .content(Base64.decode(components[1]).decodeToString())

        return builder
            .signWith(key, Jwts.SIG.ES256)
            .compact()
    }
}

internal fun String.clearKey(marker: String = "PRIVATE KEY"): ByteArray {
    // android.util.Base64 tolerated missing padding; keep that behavior on JVM.
    val base64 = Base64.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
    val content = base64.decode(this)
    val stringContent =
        String(content, Charsets.UTF_8)
            .removePrefix("-----BEGIN $marker-----\n")
            .removeSuffix("\n-----END $marker-----")
            .replace("\n", "")
    return base64.decode(stringContent)
}
