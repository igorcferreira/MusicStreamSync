package dev.igorcferreira.musicstreamsync.model

import dev.igorcferreira.musicstreamsync.domain.JWTTokenSigner
import dev.igorcferreira.musicstreamsync.domain.clearKey
import io.jsonwebtoken.Jwts
import io.ktor.util.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import java.security.KeyFactory
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JWTTokenSignerTests {

    @Test
    fun testSignature() = runTest {
        val signer = JWTTokenSigner()
        val response = signer.sign(
            mockToken(),
            privateKey = MOCK_PRIVATE_KEY
        )

        val spec = X509EncodedKeySpec(
            MOCK_PUBLIC_KEY
                .clearKey(marker = "PUBLIC KEY")
        )
        val keyFactory = KeyFactory.getInstance("EC")
        val key = keyFactory.generatePublic(spec)

        val jwt = Jwts.parser()
            .verifyWith(key)
            .requireIssuer("TEAM_ID")
            .build()
            .parseSignedClaims(response)

        assertEquals("ES256", jwt.header.algorithm)
    }

    @Test(expected = InvalidKeySpecException::class)
    fun testInvalidKey() = runTest {
        val signer = JWTTokenSigner()
        signer.sign(
            mockToken(),
            privateKey = INVALID_KEY
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test(expected = MissingFieldException::class)
    fun testInvalidHeader() = runTest {
        val iat = Clock.System.now().epochSeconds
        val exp = Clock.System.now().plus(1.days).epochSeconds

        val body = "{\"iss\":\"TEAM_ID\",\"iat\":$iat,\"exp\":$exp}".encodeBase64()
        val header = "{\"invalid\": \"header\"}".encodeBase64()
        JWTTokenSigner().sign(
            "$header.$body",
            privateKey = MOCK_PRIVATE_KEY
        )
    }

    private fun mockToken(): String {
        val iat = Clock.System.now().epochSeconds
        val exp = Clock.System.now().plus(1.days).epochSeconds
        val body = "{\"iss\":\"TEAM_ID\",\"iat\":$iat,\"exp\":$exp}".encodeBase64()
        val header = "{\"alg\": \"ES256\", \"kid\": \"KEY_ID\"}".encodeBase64()

        return "$header.$body"
    }

    companion object {
        private const val MOCK_PRIVATE_KEY =
            "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR0hBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJHMHdhd0lCQVFRZ2xCbk8rcW4rUmVjQVEzMVQKakJrbE51K0F3aUZONWVWSEJGYm5qZWNtTXJ5aFJBTkNBQVJHcFZlZjZqN3JNUTZsWVN3YkRrS3dIN0Izek02UApHN1M0QklhbUlZLzdCaDl4elc2Zkl6RnhLMXNQTlNORzQ1dGp3TnFWb0luMzhucFN1UkNSa0cxbgotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t"
        private const val MOCK_PUBLIC_KEY =
            "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFUnFWWG4rbys2ekVPcFdFc0d3NUNzQit3ZDh6TwpqeHUwdUFTR3BpR1Ard1lmY2MxdW55TXhjU3RiRHpValJ1T2JZOERhbGFDSjkvSjZVcmtRa1pCdFp3PT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t"
        private const val INVALID_KEY =
            "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR0hBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJHMHdhd0lCQVFRZ2xCbk8rcW4rUmVjQVEzMVQKakJrbE51K0F3aUZONWVWSEJGYm5qZWNtTXJ5aFJBTkNBQVJHcFZlZjZqN3JNUTZsWVN3YkRrS3dIN0Izek02UAotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t"
    }
}
