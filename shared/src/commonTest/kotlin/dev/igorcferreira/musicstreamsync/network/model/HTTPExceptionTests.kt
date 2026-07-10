package dev.igorcferreira.musicstreamsync.network.model

import kotlin.test.Test
import kotlin.test.assertEquals

class HTTPExceptionTests {
    @Test
    fun testExposesStatusCodeAndMessage() {
        val exception = HTTPException(401, "Unauthorized")

        assertEquals(401, exception.code)
        assertEquals("Unauthorized", exception.message)
    }
}
