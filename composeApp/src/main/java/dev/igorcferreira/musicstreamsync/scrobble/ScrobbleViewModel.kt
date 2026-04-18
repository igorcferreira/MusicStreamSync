package dev.igorcferreira.musicstreamsync.scrobble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.igorcferreira.musicstreamsync.domain.use_cases.LastFMUseCase
import dev.igorcferreira.musicstreamsync.domain.use_cases.RecentlyPlayedUseCase
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScrobbleViewModel(
    private val lastFMUseCase: LastFMUseCase,
    private val recentlyPlayedUseCase: RecentlyPlayedUseCase,
) : ViewModel() {
    val history: StateFlow<List<MusicEntry>>
        get() = recentlyPlayedUseCase.result
    val loading: StateFlow<Boolean>
        get() = recentlyPlayedUseCase.isPerforming
    val selection = MutableStateFlow<Set<String>>(emptySet())
    val isScrobbling = MutableStateFlow(false)

    fun updateHistory() =
        viewModelScope.launch {
            recentlyPlayedUseCase.perform()
        }

    fun toggleSelection(entry: EntryData) {
        selection.update { current ->
            if (entry.id in current) current - entry.id else current + entry.id
        }
    }

    fun isSelected(entry: EntryData): Boolean = entry.id in selection.value

    fun scrobble() =
        viewModelScope.launch {
            isScrobbling.update { true }
            try {
                val items = history.value.filter { it.id in selection.value }
                lastFMUseCase.scrobble(items)
                selection.update { emptySet() }
            } finally {
                isScrobbling.update { false }
            }
        }
}
