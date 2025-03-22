package dev.igorcferreira.musicstreamsync.lastfm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme
import dev.igorcferreira.musicstreamsync.di.ViewModelFactory

@Composable
fun LastFMToolbar(
    viewModel: LastFMViewModel = viewModel(factory = ViewModelFactory.LastFM),
) {
    val authenticated = viewModel.isAuthenticated.collectAsState(initial = false)
    val authenticating = viewModel.authenticating.collectAsState(initial = false)

    LastFMToolbar(
        authenticated = authenticated.value,
        authenticating = authenticating.value,
        authenticate = { username, password ->
            viewModel.authenticate(username, password)
        }, logout = {
            viewModel.logout()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastFMToolbar(
    authenticated: Boolean,
    authenticating: Boolean,
    authenticate: (username: String, password: String) -> Unit,
    logout: () -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    Box {
        if (authenticated) {
            Button(onClick = { logout() }) {
                Text(text = "Sign Out")
            }
        } else {
            Button(onClick = { showDialog = true }) {
                Text(text = "Authenticate")
            }
        }

        AnimatedVisibility(showDialog) {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    LastFMAuthentication(
                        authenticating = authenticating,
                        authenticate = { username, password ->
                            authenticate(username, password)
                            showDialog = authenticated
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun LastFMToolbarPreview() {
    AppTheme {
        LastFMToolbar(
            authenticated = false,
            authenticating = false,
            authenticate = { _, _ -> },
            logout = { }
        )
    }
}
