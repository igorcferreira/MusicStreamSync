package dev.igorcferreira.musicstreamsync.history

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
fun History(
    viewModel: RecentlyPlayedViewModel = viewModel(factory = ViewModelFactory.RecentlyPlayed)
) {
    val history = viewModel.history.collectAsState(initial = listOf())
    val loading = viewModel.loading.collectAsState(initial = false)

    LaunchedEffect(viewModel) {
        viewModel.updateHistory()
    }

    EntryList(stringResource(R.string.recently_played), history.value, loading.value, { Entry(it) }, { Player() })
}

