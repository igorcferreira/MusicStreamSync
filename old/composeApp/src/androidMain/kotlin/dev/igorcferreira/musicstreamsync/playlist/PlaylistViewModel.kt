package dev.igorcferreira.musicstreamsync.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.igorcferreira.musicstreamsync.domain.use_cases.PlaylistsUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val useCase: PlaylistsUseCase
) : ViewModel() {

    constructor(
        configuration: Configuration
    ) : this(PlaylistsUseCase(configuration))

    val loading: StateFlow<Boolean>
        get() = useCase.isPerforming
    val items: StateFlow<List<PlaylistEntry>>
        get() = useCase.result

    fun load() = viewModelScope.launch { useCase.perform() }
}
