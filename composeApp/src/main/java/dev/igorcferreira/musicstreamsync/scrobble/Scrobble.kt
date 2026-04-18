package dev.igorcferreira.musicstreamsync.scrobble

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.list.Entry
import dev.igorcferreira.musicstreamsync.list.EntryList
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry

@Composable
fun Scrobble(viewModel: ScrobbleViewModel = viewModel(factory = ViewModelFactory.Scrobble)) {
    val history = viewModel.history.collectAsState()
    val loading = viewModel.loading.collectAsState()
    val selection = viewModel.selection.collectAsState()

    LaunchedEffect(Unit) { viewModel.updateHistory() }

    Scrobble(
        items = history.value,
        loading = loading.value,
        selection = selection.value,
        onRefresh = { viewModel.updateHistory() },
        onToggle = { viewModel.toggleSelection(it) },
        isSelected = { viewModel.isSelected(it) },
        onScrobble = { viewModel.scrobble() },
    )
}

@Composable
fun Scrobble(
    items: List<EntryData>,
    loading: Boolean,
    selection: Set<String>,
    onRefresh: () -> Unit,
    onToggle: (EntryData) -> Unit,
    isSelected: (EntryData) -> Boolean,
    onScrobble: () -> Unit,
) {
    EntryList(
        items = items,
        loading = loading,
        onRefresh = onRefresh,
        header = {
            AnimatedVisibility(!loading) {
                Text(
                    text = stringResource(R.string.scrobble_instructions),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            AnimatedVisibility(selection.isNotEmpty()) {
                Button(
                    onClick = onScrobble,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Text(text = stringResource(R.string.scrobble))
                }
            }
        },
        entry = { entryData ->
            Box {
                Entry(
                    modifier = Modifier,
                    entry = entryData,
                    onClick = { onToggle(entryData) },
                )
                if (isSelected(entryData)) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.33f))
                            .clickable { onToggle(entryData) },
                    )
                }
            }
        },
        player = {},
    )
}

private val previewEntries: List<MusicEntry> =
    listOf(
        MusicEntry(
            id = "1184710148",
            title = "Stardust",
            artist = "Delain",
            artworkUrl = "",
            album = "The Human Contradiction",
        ),
        MusicEntry(
            id = "1184710149",
            title = "Here Come the Vultures",
            artist = "Delain",
            artworkUrl = "",
            album = "The Human Contradiction",
        ),
    )

@Preview(name = "Loading — Light", showBackground = true)
@Composable
fun ScrobbleLoadingPreview() {
    AppTheme {
        Scrobble(
            items = listOf(),
            loading = true,
            selection = emptySet(),
            onRefresh = {},
            onToggle = {},
            isSelected = { false },
            onScrobble = {},
        )
    }
}

@Preview(name = "Content — No Selection", showBackground = true)
@Composable
fun ScrobbleNoSelectionPreview() {
    AppTheme {
        Scrobble(
            items = previewEntries,
            loading = false,
            selection = emptySet(),
            onRefresh = {},
            onToggle = {},
            isSelected = { false },
            onScrobble = {},
        )
    }
}

@Preview(name = "Content — With Selection", showBackground = true)
@Composable
fun ScrobbleWithSelectionPreview() {
    AppTheme {
        Scrobble(
            items = previewEntries,
            loading = false,
            selection = setOf("1184710148"),
            onRefresh = {},
            onToggle = {},
            isSelected = { it.id == "1184710148" },
            onScrobble = {},
        )
    }
}

@Preview(name = "Content — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ScrobbleDarkPreview() {
    AppTheme {
        Scrobble(
            items = previewEntries,
            loading = false,
            selection = setOf("1184710148"),
            onRefresh = {},
            onToggle = {},
            isSelected = { it.id == "1184710148" },
            onScrobble = {},
        )
    }
}
