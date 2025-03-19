package dev.igorcferreira.musicstreamsync.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.history.History
import dev.igorcferreira.musicstreamsync.playlist.Playlist

@Composable
fun App() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(R.string.history, R.string.playlists)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    text = { Text(stringResource(id = tab)) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            0 -> History()
            1 -> Playlist()
        }
    }

}
