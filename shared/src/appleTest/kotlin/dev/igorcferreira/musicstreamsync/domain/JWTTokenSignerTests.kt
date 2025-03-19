package dev.igorcferreira.musicstreamsync.domain

import io.ktor.util.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

@OptIn(ExperimentalForeignApi::class)
class JWTTokenSignerTests {
    @Test
    fun testJWTSigner() = runTest {
        val signer = JWTTokenSigner()

        val signed = signer.sign(UNSIGNED, MOCKED_KEY.decodeBase64String())

        assertNotEquals(UNSIGNED, signed)

        val components = signed.split(".")
            .toMutableList()

        assertEquals(3, components.size)

        components.removeLast()
        val header = components.joinToString(separator = ".")
        assertEquals(UNSIGNED, header)
    }

    @Test
    fun testInvalidKey() = runTest {
        val signer = JWTTokenSigner()
        try {
            signer.sign(UNSIGNED, INVALID_KEY.decodeBase64String())
            fail("JWTTokenSigner did not throw for invalid key")
        } catch (ignored: JWTTokenSigner.SignatureException) {
        }
    }

    companion object {
        private const val INVALID_KEY =
            "klWQVRFIEtFWS0tLS0tCk1JR0hBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJHMHdhd0lCQVFRZ2xCbk8rcW4rUmVjQVEzMVQKakJrbE51K0F3aUZONWVWSEJGYm5qZWNtTXJ5aFJBTkNBQVJHcFZlZjZqN3JNUTZsWVN3YkRrS3dIN0Izek02UApHN1M0QklhbUlZLzdCaDl4elc2Zkl6RnhLMXNQTlNORzQ1dGp3TnFWb0luMzhucFN1UkNSa0cxbgotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t"
        private const val MOCKED_KEY =
            "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR0hBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJHMHdhd0lCQVFRZ2xCbk8rcW4rUmVjQVEzMVQKakJrbE51K0F3aUZONWVWSEJGYm5qZWNtTXJ5aFJBTkNBQVJHcFZlZjZqN3JNUTZsWVN3YkRrS3dIN0Izek02UApHN1M0QklhbUlZLzdCaDl4elc2Zkl6RnhLMXNQTlNORzQ1dGp3TnFWb0luMzhucFN1UkNSa0cxbgotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t"
        private const val UNSIGNED =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTc0MjA4NzM0NiwiZXhwIjoxNzQyMDkwOTQ2fQ"
    }
}