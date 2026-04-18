package dev.igorcferreira.musicstreamsync.playlist

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
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry
import dev.igorcferreira.musicstreamsync.player.Player

@Composable
fun Playlist(viewModel: PlaylistViewModel = viewModel(factory = ViewModelFactory.Playlist)) {
    val items = viewModel.items.collectAsState(initial = listOf())
    val loading = viewModel.loading.collectAsState(initial = false)

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    EntryList(
        items = items.value,
        loading = loading.value,
        entry = { Entry(it) },
        player = { Player() },
        onRefresh = { viewModel.load() },
    )
}

@Preview(name = "Loading — Light", showBackground = true)
@Composable
fun PlaylistLoadingPreview() {
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
fun PlaylistContentPreview() {
    AppTheme {
        EntryList(
            items =
                listOf(
                    PlaylistEntry(
                        id = "p.abc123",
                        entryId = null,
                        name = "Heavy Rotation",
                        canEdit = true,
                        hasCatalog = false,
                        isPublic = false,
                        entryDescription = "My most-played tracks",
                    ),
                    PlaylistEntry(
                        id = "p.def456",
                        entryId = null,
                        name = "Workout Mix",
                        canEdit = true,
                        hasCatalog = false,
                        isPublic = true,
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
fun PlaylistContentDarkPreview() {
    AppTheme {
        EntryList(
            items =
                listOf(
                    PlaylistEntry(
                        id = "p.abc123",
                        entryId = null,
                        name = "Heavy Rotation",
                        canEdit = true,
                        hasCatalog = false,
                        isPublic = false,
                        entryDescription = "My most-played tracks",
                    ),
                ),
            loading = false,
            entry = { Entry(Modifier, it) {} },
            player = { Player(Modifier, null, false, {}, {}) },
        )
    }
}
