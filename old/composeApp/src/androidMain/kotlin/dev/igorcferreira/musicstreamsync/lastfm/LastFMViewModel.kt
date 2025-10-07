package dev.igorcferreira.musicstreamsync.lastfm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.igorcferreira.musicstreamsync.domain.use_cases.LastFMUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LastFMViewModel(
    private val useCase: LastFMUseCase = LastFMUseCase()
) : ViewModel() {
    val error = MutableStateFlow<String?>(null)
    val isAuthenticated: StateFlow<Boolean>
        get() = useCase.isAuthenticated
    val authenticating: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun logout() = useCase.logout()

    fun authenticate(
        username: String,
        password: String
    ) = viewModelScope.launch {
        error.update { null }
        authenticating.update { true }
        try {
            useCase.authenticate(username, password)
        } catch (exception: Exception) {
            error.update { exception.message }
        } finally {
            authenticating.update { false }
        }
    }
}
