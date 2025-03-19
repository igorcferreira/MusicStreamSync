package dev.igorcferreira.musicstreamsync.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.player.Player

@Composable
fun EntryList(
    title: String,
    items: List<EntryData>,
    loading: Boolean,
    entry: @Composable (EntryData) -> Unit,
    player: @Composable () -> Unit,
) {
    val context = LocalContext.current

    BoxWithConstraints(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .safeContentPadding()
                .fillMaxWidth()
                .align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .padding(16.dp)
                        .semantics {
                            testTag = "loading"
                            contentDescription = context.getString(R.string.loading)
                        },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = items.isNotEmpty()) {
                Column {
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(16.dp)
                            .semantics { testTag = "home_title" },
                        style = MaterialTheme.typography.headlineLarge
                    )

                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(items) { entry ->
                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                entry(entry)
                            }
                        }
                    }
                }
            }
        }

        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {}
            player()
        }
    }
}

@Preview(name = "Loading")
@Composable
fun LoadingAppPreview() {
    AppTheme {
        EntryList("Empty", listOf(), true, {
            Entry(Modifier, it) {}
        }, {
            Player(Modifier, null, false, {}, {})
        })
    }
}

@Preview(name = "Content")
@Composable
fun LoadedAppPreview(
    list: List<MusicEntry> = listOf(
        MusicEntry(
            id = "1184710148",
            title = "Stardust",
            artist = "Delain",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
            album = "The Human Contradiction"
        )
    )
) {
    AppTheme {
        EntryList("Loaded", list, false, {
            Entry(Modifier, it) {}
        }, {
            Player(Modifier, null, false, {}, {})
        })
    }
}
