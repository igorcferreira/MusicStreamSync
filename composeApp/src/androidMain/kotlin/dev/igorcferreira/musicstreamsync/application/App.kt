package dev.igorcferreira.musicstreamsync.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.history.History
import dev.igorcferreira.musicstreamsync.lastfm.LastFMToolbar
import dev.igorcferreira.musicstreamsync.playlist.Playlist

@Composable
fun App() {
    val context = LocalContext.current
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(R.string.history, R.string.playlists)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 16.dp),
                text = when (tabIndex) {
                    0 -> context.getString(R.string.recently_played)
                    1 -> context.getString(R.string.playlists)
                    else -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            LastFMToolbar()
        }

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
