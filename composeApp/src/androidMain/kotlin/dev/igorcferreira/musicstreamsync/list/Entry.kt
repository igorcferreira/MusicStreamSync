package dev.igorcferreira.musicstreamsync.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.player.PlayerViewModel

@Composable
fun Entry(
    entry: EntryData,
    modifier: Modifier = Modifier
        .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)),
    viewModel: PlayerViewModel = viewModel(factory = ViewModelFactory.Player)
) {
    Entry(modifier, entry) { viewModel.play(entry) }
}

@Composable
fun Entry(
    modifier: Modifier = Modifier,
    entry: EntryData,
    onClick: (EntryData) -> Unit
) {
    key(entry.id) {
        Surface(modifier = modifier, tonalElevation = 4.dp, shadowElevation = 4.dp) {
            Row(
                Modifier
                    .padding(8.dp)
                    .semantics(mergeDescendants = true) {
                        testTag = "entry_${entry.id}"
                        contentDescription = "${entry.title} - ${entry.body ?: ""}"
                    }
                    .clickable { onClick(entry) }, verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.artworkUrl.isNullOrBlank()) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = entry.body ?: entry.title,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier
                            .size(60.dp, 60.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(entry.artworkUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = entry.body ?: entry.title,
                        modifier = Modifier
                            .size(60.dp, 60.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
                Column(Modifier.padding(start = 8.dp)) {
                    Text(
                        entry.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    entry.body?.let { body ->
                        Text(
                            body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    entry.footer?.let { footer ->
                        Text(
                            footer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode", showBackground = true
)
@Composable
fun EntryAndroidPreviewDark() {
    AppTheme {
        Entry(
            Modifier,
            MusicEntry(
                id = "1184710148",
                title = "Stardust",
                artist = "Delain",
                artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
                album = "The Human Contradiction"
            ),
        ) {}
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode", showBackground = true
)
@Composable
fun EntryAndroidPreviewLight() {
    AppTheme {
        Entry(
            Modifier,
            MusicEntry(
                id = "1184710148",
                title = "Stardust",
                artist = "Delain",
                artworkUrl = "",
                album = "The Human Contradiction"
            )
        ) {}
    }
}
