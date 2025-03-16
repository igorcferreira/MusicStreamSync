package dev.igorcferreira.musicstreamsync.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory
import dev.igorcferreira.musicstreamsync.player.Player

@Preview
@Composable
fun App(
    viewModel: RecentlyPlayedViewModel = viewModel(factory = ViewModelFactory.RecentlyPlayed)
) {
    MaterialTheme {
        val history = viewModel.history.collectAsState(initial = listOf())
        val loading = viewModel.loading.collectAsState(initial = false)

        LaunchedEffect(viewModel) {
            viewModel.updateHistory()
        }

        BoxWithConstraints {
            Column(
                Modifier
                    .safeContentPadding()
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(loading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(16.dp)
                            .semantics {
                                testTag = "loading"
                                contentDescription = "loading"
                            },
                        color = MaterialTheme.colors.primary
                    )
                }

                AnimatedVisibility(visible = history.value.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Recently Played",
                            modifier = Modifier
                                .padding(16.dp)
                                .semantics { testTag = "home_title" },
                            style = MaterialTheme.typography.h4
                        )

                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(history.value) { entry ->
                                Row { Entry(entry) }
                            }
                        }
                    }
                }
            }

            Column {
                Row(Modifier
                    .fillMaxWidth()
                    .weight(1f)) {}
                Player()
            }
        }
    }
}
