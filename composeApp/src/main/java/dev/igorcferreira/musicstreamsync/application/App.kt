package dev.igorcferreira.musicstreamsync.application

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.history.History
import dev.igorcferreira.musicstreamsync.lastfm.LastFMToolbar
import dev.igorcferreira.musicstreamsync.playlist.Playlist
import dev.igorcferreira.musicstreamsync.scrobble.Scrobble

@Composable
fun App(modifier: Modifier = Modifier) {
    var tabIndex by remember { mutableIntStateOf(0) }
    App(
        modifier = modifier,
        tabIndex = tabIndex,
        onTabChange = { tabIndex = it },
        toolbar = { LastFMToolbar() },
    ) {
        when (tabIndex) {
            0 -> History()
            1 -> Playlist()
            2 -> Scrobble()
        }
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    tabIndex: Int = 0,
    onTabChange: (Int) -> Unit = {},
    toolbar: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val tabs = listOf(R.string.history, R.string.playlists, R.string.scrobble)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier =
                    Modifier
                        .padding(vertical = 16.dp),
                text =
                    when (tabIndex) {
                        0 -> stringResource(R.string.recently_played)
                        1 -> stringResource(R.string.playlists)
                        2 -> stringResource(R.string.manual_scrobble)
                        else -> ""
                    },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            toolbar()
        }

        PrimaryTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    text = { Text(stringResource(id = tab)) },
                    selected = tabIndex == index,
                    onClick = { onTabChange(index) },
                )
            }
        }

        content()
    }
}

@Preview(name = "History Tab — Light", showBackground = true)
@Composable
fun AppHistoryPreview() {
    AppTheme {
        App(
            tabIndex = 0,
            toolbar = {
                LastFMToolbar(
                    authenticated = false,
                    authenticating = false,
                    authenticate = { _, _ -> },
                    logout = {},
                )
            },
        )
    }
}

@Preview(name = "Playlists Tab — Light", showBackground = true)
@Composable
fun AppPlaylistsPreview() {
    AppTheme {
        App(
            tabIndex = 1,
            toolbar = {
                LastFMToolbar(
                    authenticated = true,
                    authenticating = false,
                    authenticate = { _, _ -> },
                    logout = {},
                )
            },
        )
    }
}

@Preview(name = "Scrobble Tab — Light", showBackground = true)
@Composable
fun AppScrobblePreview() {
    AppTheme {
        App(
            tabIndex = 2,
            toolbar = {
                LastFMToolbar(
                    authenticated = true,
                    authenticating = false,
                    authenticate = { _, _ -> },
                    logout = {},
                )
            },
        )
    }
}

@Preview(name = "History Tab — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AppHistoryDarkPreview() {
    AppTheme {
        App(
            tabIndex = 0,
            toolbar = {
                LastFMToolbar(
                    authenticated = false,
                    authenticating = false,
                    authenticate = { _, _ -> },
                    logout = {},
                )
            },
        )
    }
}
