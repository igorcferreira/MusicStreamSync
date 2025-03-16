package dev.igorcferreira.musicstreamsync.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.model.SongEntry

@Composable
fun Player(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = ViewModelFactory.Player)
) {
    val playing = viewModel.isPlaying.collectAsState(initial = false)
    val playingItem = viewModel.playingItem.collectAsState(initial = null)

    AnimatedVisibility(playingItem.value != null) {
        PlayerItem(
            playingItem.value!!,
            playing.value,
            play = { viewModel.play() },
            pause = { viewModel.pause() },
            modifier
        )
    }
}

@Composable
fun PlayerItem(
    entry: SongEntry,
    isPlaying: Boolean = false,
    play: (SongEntry) -> Unit = {},
    pause: (SongEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(modifier
        .fillMaxWidth()
        .background(MaterialTheme.colors.secondary)) {
        Row(Modifier
            .padding(all = 8.dp)
            .padding(bottom = 20.dp)
            .clickable {
                if (isPlaying) pause(entry) else play(entry)
            }, verticalAlignment = Alignment.CenterVertically) {
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
            Column(Modifier
                .padding(start = 8.dp)
                .weight(1f)) {
                Text(entry.title, color = MaterialTheme.colors.onSurface, style = MaterialTheme.typography.body1)
                Text(entry.artist, color = MaterialTheme.colors.onSurface, style = MaterialTheme.typography.subtitle2)
            }

            if (isPlaying) {
                Icon(
                    painterResource(R.drawable.ic_filled_pause),
                    contentDescription = "Pause",
                    tint = MaterialTheme.colors.onSecondary
                )
            } else {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colors.onSecondary
                )
            }
        }
    }
}

@Composable
@Preview(name = "Playing", showBackground = true)
fun PlayerItem_Playing_Preview() {
    PlayerItem(
        SongEntry(
            id = "1184710148",
            title = "Stardust",
            artist = "Delain",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
            album = "The Human Contradiction"
        ), true
    )
}

@Composable
@Preview(name = "Paused", showBackground = true)
fun PlayerItem_Paused_Preview() {
    PlayerItem(
        SongEntry(
            id = "1184710148",
            title = "Stardust",
            artist = "Delain",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
            album = "The Human Contradiction"
        ), false
    )
}