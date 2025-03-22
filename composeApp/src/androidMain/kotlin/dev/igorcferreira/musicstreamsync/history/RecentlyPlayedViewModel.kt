package dev.igorcferreira.musicstreamsync.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.igorcferreira.musicstreamsync.domain.use_cases.RecentlyPlayedUseCase
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentlyPlayedViewModel(
    private val recentlyPlayedUseCase: RecentlyPlayedUseCase
) : ViewModel() {

    constructor(
        configuration: Configuration
    ) : this(RecentlyPlayedUseCase(configuration))

    val history: StateFlow<List<MusicEntry>>
        get() = recentlyPlayedUseCase.result
    val loading: StateFlow<Boolean>
        get() = recentlyPlayedUseCase.isPerforming

    fun updateHistory() = viewModelScope.launch {
        recentlyPlayedUseCase.perform()
    }
}
