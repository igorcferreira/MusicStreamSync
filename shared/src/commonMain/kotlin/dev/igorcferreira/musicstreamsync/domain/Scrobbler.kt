package dev.igorcferreira.musicstreamsync.domain

import com.arkanakeys.ArkanaKeys
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import dev.igorcferreira.lastfm.LastFMClient
import dev.igorcferreira.lastfm.model.HTTPException
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer
import dev.igorcferreira.musicstreamsync.domain.use_cases.PlayerUseCase
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Scrobbler(
    apiKey: String = ArkanaKeys.Global.lastFMAPIKey,
    apiKeySecret: String = ArkanaKeys.Global.lastFMAPISecret,
    private val playerUserCase: PlayerUseCase = PlayerUseCase()
) {
    private val coroutineContext = CoroutineScope(Dispatchers.Unconfined)
    private var client: LastFMClient? = null
    private var job: Job? = null
    internal val isAuthenticated: Boolean
        get() = client?.isAuthenticated ?: false

    init {
        start(apiKey, apiKeySecret)
    }

    fun logout() = client?.logout()

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun authenticate(username: String, password: String) {
        val currentClient = client ?: return
        currentClient.authenticate(username, password)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun scrobble(items: List<MusicEntry>) {
        val currentClient = client ?: return
        try {
            currentClient.scrobble(items.map {
                Scrobble(
                    track = it.title,
                    artist = it.artist,
                    timestamp = Clock.System.now(),
                    album = it.album,
                    albumArtist = it.albumArtist,
                )
            })
        } catch (e: Exception) {
            SystemLogger.error("LAST_FM_SCROBBLER", "Unable to scrobble: ${e.message ?: ""}", e)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun start(apiKey: String, apiKeySecret: String) {
        job?.cancel()
        client = LastFMClient(apiKey, apiKeySecret)
        job = coroutineContext.launch {
            playerUserCase.playingItem
                .shareIn(this, SharingStarted.Lazily)
                .onEach {
                    val currentClient = client ?: return@onEach
                    val item = it ?: return@onEach
                    if (!currentClient.isAuthenticated) {
                        return@onEach
                    }
                    if (item.entryId == lastItemScrobbled) {
                        return@onEach
                    }
                    if (playerUserCase.playerState == NativePlayer.PlayerState.NOT_PLAYING) {
                        return@onEach
                    }
                    lastItemScrobbled = item.entryId

                    SystemLogger.info("Scrobbler", "Scrobbling ${item.title} - ${item.artist}")

                    currentClient.scrobble(
                        artist = item.artist,
                        track = item.title,
                        timestamp = Clock.System.now(),
                        album = item.album,
                        albumArtist = item.albumArtist,
                    )

                    currentClient.updateNowPlaying(
                        artist = item.artist,
                        track = item.title,
                        album = item.album,
                        albumArtist = item.albumArtist
                    )
                }.launchIn(this)
        }
    }

    companion object {
        private val settings = Settings()
        private const val LAST_ITEM_KEY = "LAST_ITEM_KEY"
        private var lastItemScrobbled: String?
            get() = settings.getStringOrNull(LAST_ITEM_KEY)
            set(value) = settings.set(LAST_ITEM_KEY, value)
    }
}
