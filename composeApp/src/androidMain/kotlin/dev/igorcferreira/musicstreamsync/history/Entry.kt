package dev.igorcferreira.musicstreamsync.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.model.SongEntry
import dev.igorcferreira.musicstreamsync.player.PlayerViewModel

@Composable
fun Entry(
    entry: SongEntry,
    viewModel: PlayerViewModel = viewModel(factory = ViewModelFactory.Player),
    modifier: Modifier = Modifier
        .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
) {
    key(entry.id) {
        Row(
            modifier
                .semantics(mergeDescendants = true) {
                    testTag = "entry_${entry.id}"
                    contentDescription = "${entry.title} by ${entry.artist}"
                }
                .clickable { viewModel.play(entry) }, verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = entry.album ?: entry.title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Column(Modifier.padding(start = 8.dp)) {
                Text(
                    entry.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    style = MaterialTheme.typography.h6
                )
                entry.album?.let { album ->
                    Text(
                        album,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
                Text(
                    entry.artist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Preview
@Composable
fun EntryAndroidPreview() {
    Entry(
        SongEntry(
            id = "1184710148",
            title = "Stardust",
            artist = "Delain",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
            album = "The Human Contradiction"
        )
    )
}