package dev.igorcferreira.musicstreamsync.model

import dev.igorcferreira.musicstreamsync.domain.JWTTokenSigner
import dev.igorcferreira.musicstreamsync.domain.MusicUserTokenProvider
import kotlin.test.Test
import kotlin.test.assertSame

class ConfigurationTests {
    @Test
    fun testUsesInjectedUserTokenProvider() {
        val provider = MusicUserTokenProvider(token = "user-token")
        val signer = JWTTokenSigner()
        val configuration = Configuration(DeveloperToken(), signer, provider)

        assertSame(provider, configuration.userTokenProvider)
        assertSame(signer, configuration.tokenSigner)
    }
}
