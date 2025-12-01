package dev.igorcferreira.musicstreamsync.player

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.model.MusicEntry

@Composable
fun Player(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = ViewModelFactory.Player)
) {
    val playing = viewModel.isPlaying.collectAsState(initial = false)
    val playingItem = viewModel.playingItem.collectAsState(initial = null)

    Player(
        modifier = modifier,
        playingItem = playingItem.value,
        playing = playing.value,
        play = { viewModel.play() },
        pause = { viewModel.pause() }
    )
}

@Composable
fun Player(
    modifier: Modifier = Modifier,
    playingItem: MusicEntry?,
    playing: Boolean,
    play: (MusicEntry) -> Unit,
    pause: (MusicEntry) -> Unit,
) {
    AnimatedVisibility(
        visible = playingItem != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Player(
            playingItem!!,
            playing,
            play = play,
            pause = pause,
            modifier
        )
    }
}

@Composable
fun Player(
    entry: MusicEntry,
    isPlaying: Boolean = false,
    play: (MusicEntry) -> Unit = {},
    pause: (MusicEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            Modifier
                .padding(all = 8.dp)
                .padding(bottom = 20.dp)
                .clickable {
                    if (isPlaying) pause(entry) else play(entry)
                }, verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = entry.album ?: entry.title,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Column(
                Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    entry.title,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    entry.artist,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isPlaying) {
                Icon(
                    painterResource(R.drawable.ic_filled_pause),
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Playing",
    showBackground = true
)
fun PlayerItem_Playing_Preview() {
    AppTheme {
        Player(
            MusicEntry(
                id = "1184710148",
                title = "Stardust",
                artist = "Delain",
                artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
                album = "The Human Contradiction"
            ), true
        )
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Paused", showBackground = true
)
fun PlayerItem_Paused_Preview() {
    AppTheme {
        Player(
            MusicEntry(
                id = "1184710148",
                title = "Stardust",
                artist = "Delain",
                artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
                album = "The Human Contradiction"
            ), false
        )
    }
}
