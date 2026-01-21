package dev.igorcferreira.musicstreamsync.lastfm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.igorcferreira.musicstreamsync.R
import dev.igorcferreira.musicstreamsync.application.theme.AppTheme

@Composable
fun LastFMAuthentication(
    authenticating: Boolean,
    authenticate: (String, String) -> Unit
) {
    val context = LocalContext.current
    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        usernameFocus.requestFocus()
    }

    Box(
        Modifier.padding(16.dp)
    ) {
        AnimatedVisibility(authenticating, Modifier.align(Alignment.Center)) {
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
        AnimatedVisibility(!authenticating, Modifier.align(Alignment.TopCenter)) {
            Column {
                Text(
                    text = stringResource(R.string.authenticate_to_last_fm),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.authentication_label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                TextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .focusRequester(usernameFocus),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        passwordFocus.requestFocus()
                    })
                )
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .focusRequester(passwordFocus),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        authenticate(username, password)
                    })
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    onClick = { authenticate(username, password) }
                ) {
                    Text(stringResource(R.string.authenticate))
                }
            }
        }
    }
}

@Composable
@Preview(name = "Inputs", showBackground = true)
fun LastFMAuthenticationPreview() {
    AppTheme {
        LastFMAuthentication(
            authenticating = false,
        ) { _, _ -> }
    }
}

@Composable
@Preview(name = "Loading", showBackground = true)
fun LastFMAuthenticatingPreview() {
    AppTheme {
        LastFMAuthentication(
            authenticating = true,
        ) { _, _ -> }
    }
}
