package dev.igorcferreira.musicstreamsync.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.list.Entry
import dev.igorcferreira.musicstreamsync.list.EntryList
import dev.igorcferreira.musicstreamsync.player.Player

@Composable
fun Playlist(
    viewModel: PlaylistViewModel = viewModel(factory = ViewModelFactory.Playlist)
) {
    val items = viewModel.items.collectAsState(initial = listOf())
    val loading = viewModel.loading.collectAsState(initial = false)

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    EntryList(stringResource(R.string.playlists), items.value, loading.value, { Entry(it) }, { Player() })
}
