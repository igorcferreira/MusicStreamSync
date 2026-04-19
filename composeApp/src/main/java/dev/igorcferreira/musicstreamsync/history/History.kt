package dev.igorcferreira.musicstreamsync.history

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.list.Entry
import dev.igorcferreira.musicstreamsync.list.EntryList
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.player.Player

@Composable
fun History(viewModel: RecentlyPlayedViewModel = viewModel(factory = ViewModelFactory.RecentlyPlayed)) {
    val history = viewModel.history.collectAsState(initial = listOf())
    val loading = viewModel.loading.collectAsState(initial = false)

    LaunchedEffect(viewModel) {
        viewModel.updateHistory()
    }

    EntryList(
        items = history.value,
        loading = loading.value,
        entry = { Entry(it) },
        player = { Player() },
        onRefresh = { viewModel.updateHistory() },
    )
}

@Preview(name = "Loading — Light", showBackground = true)
@Composable
fun HistoryLoadingPreview() {
    AppTheme {
        EntryList(
            items = listOf(),
            loading = true,
            entry = { Entry(Modifier, it) {} },
            player = { Player(Modifier, null, false, {}, {}) },
        )
    }
}

@Preview(name = "Content — Light", showBackground = true)
@Composable
fun HistoryContentPreview() {
    AppTheme {
        EntryList(
            items =
                listOf(
                    MusicEntry(
                        id = "1184710148",
                        title = "Stardust",
                        artist = "Delain",
                        artworkUrl = "",
                        album = "The Human Contradiction",
                    ),
                    MusicEntry(
                        id = "1184710149",
                        title = "Here Come the Vultures",
                        artist = "Delain",
                        artworkUrl = "",
                        album = "The Human Contradiction",
                    ),
                ),
            loading = false,
            entry = { Entry(Modifier, it) {} },
            player = { Player(Modifier, null, false, {}, {}) },
        )
    }
}

@Preview(name = "Content — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HistoryContentDarkPreview() {
    AppTheme {
        EntryList(
            items =
                listOf(
                    MusicEntry(
                        id = "1184710148",
                        title = "Stardust",
                        artist = "Delain",
                        artworkUrl = "",
                        album = "The Human Contradiction",
                    ),
                ),
            loading = false,
            entry = { Entry(Modifier, it) {} },
            player = { Player(Modifier, null, false, {}, {}) },
        )
    }
}
