package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.lastfm.model.HTTPException
import dev.igorcferreira.musicstreamsync.domain.Scrobbler
import dev.igorcferreira.musicstreamsync.domain.UseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.cancellation.CancellationException

@Suppress("unused", "MemberVisibilityCanBePrivate")
class LastFMUseCase(
    private val scrobbler: Scrobbler
) : UseCase() {
    private val _isAuthenticated = MutableStateFlow(scrobbler.isAuthenticated)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    constructor() : this(Scrobbler())

    fun logout() {
        scrobbler.logout()
        _isAuthenticated.update { false }
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun authenticate(username: String, password: String) {
        try {
            scrobbler
                .authenticate(username, password)
        } finally {
            _isAuthenticated.update { scrobbler.isAuthenticated }
        }
    }
}
