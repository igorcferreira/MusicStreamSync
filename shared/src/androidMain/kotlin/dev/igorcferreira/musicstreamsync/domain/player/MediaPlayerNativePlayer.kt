package dev.igorcferreira.musicstreamsync.domain.player

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.apple.android.music.playback.controller.MediaPlayerController
import com.apple.android.music.playback.controller.MediaPlayerControllerFactory
import com.apple.android.music.playback.model.*
import com.apple.android.music.playback.model.PlaybackState.*
import com.apple.android.music.playback.queue.CatalogPlaybackQueueItemProvider
import com.apple.android.music.playback.queue.PlaybackQueueInsertionType
import com.apple.android.sdk.authentication.TokenProvider
import dev.igorcferreira.musicstreamsync.domain.TokenSigner
import dev.igorcferreira.musicstreamsync.domain.UserTokenProvider
import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer.PlayerState
import dev.igorcferreira.musicstreamsync.domain.signWith
import dev.igorcferreira.musicstreamsync.model.*
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {

    private data class PlayerConfiguration(
        val context: WeakReference<Context>,
        val developerToken: DeveloperToken,
        val tokenSigner: TokenSigner,
        val userTokenProvider: UserTokenProvider
    )

    private var playerConfiguration: PlayerConfiguration? = null
    private var player: MediaPlayerController? = null
    override val playerState: PlayerState
        get() = if (isPlaying) PlayerState.PLAYING else PlayerState.NOT_PLAYING
    override val isPlaying: Boolean
        get() = player?.playbackState == PLAYING
    override val currentPlaying: MusicEntry?
        get() = player?.currentItem?.item?.let { item ->
            val id = item.subscriptionStoreId ?: return@let null
            return@let MusicEntry(
                id = id,
                entryId = id,
                title = item.title ?: "",
                artist = item.artistName ?: "",
                artworkUrl = item.artworkUrl ?: "",
                duration = item.duration,
                album = item.albumTitle ?: "",
                albumArtist = item.albumArtistName ?: "",
                genres = listOf(item.genreName).mapNotNull { it }
            )
        }
    override val elapsedTime: Double
        get() = player?.currentPosition?.toDouble() ?: 0.0

    fun preparePlayer(
        context: Context,
        developerToken: DeveloperToken,
        tokenSigner: TokenSigner,
        userTokenProvider: UserTokenProvider
    ) {
        playerConfiguration = PlayerConfiguration(
            context = WeakReference(context),
            developerToken = developerToken,
            tokenSigner = tokenSigner,
            userTokenProvider = userTokenProvider
        )

        player?.release()
        player = null
    }

    private val currentPlayer: MediaPlayerController?
        get() {
            if (player != null) { return player }
            val configuration = playerConfiguration ?: return null
            val context = configuration.context.get() ?: return null

            val audioManager = getSystemService(context, AudioManager::class.java)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0)
            player = MediaPlayerControllerFactory.createLocalController(
                context.applicationContext,
                Handler(Looper.getMainLooper()),
                object : TokenProvider {
                    override fun getDeveloperToken(): String = runBlocking {
                        configuration.developerToken.signWith(configuration.tokenSigner)
                    }

                    override fun getUserToken(): String = runBlocking {
                        configuration.userTokenProvider.getUserToken(getDeveloperToken())
                    }
                }
            )
            player?.addListener(object : MediaPlayerController.Listener {
                override fun onPlaybackError(p0: MediaPlayerController, p1: MediaPlayerException) {}
                override fun onPlayerStateRestored(p0: MediaPlayerController) {}
                override fun onPlaybackStateChanged(p0: MediaPlayerController, p1: Int, p2: Int) {
                    if (p1 == STOPPED && p2 == PAUSED) {
                        p0.play()
                    }
                }

                override fun onPlaybackStateUpdated(p0: MediaPlayerController) {}
                override fun onBufferingStateChanged(p0: MediaPlayerController, p1: Boolean) {}
                override fun onCurrentItemChanged(p0: MediaPlayerController, p1: PlayerQueueItem?, p2: PlayerQueueItem?) {}
                override fun onItemEnded(p0: MediaPlayerController, p1: PlayerQueueItem, p2: Long) {}
                override fun onMetadataUpdated(p0: MediaPlayerController, p1: PlayerQueueItem) {}
                override fun onPlaybackQueueChanged(p0: MediaPlayerController, p1: MutableList<PlayerQueueItem>) {}
                override fun onPlaybackQueueItemsAdded(p0: MediaPlayerController, p1: Int, p2: Int, p3: Int) {}
                override fun onPlaybackRepeatModeChanged(p0: MediaPlayerController, p1: Int) {}
                override fun onPlaybackShuffleModeChanged(p0: MediaPlayerController, p1: Int) {}
            })
            return player
        }

    override fun startPlayback() {
        currentPlayer?.play()
    }

    override fun set(queue: List<EntryData>) {
        if (isPlaying) {
            currentPlayer?.stop()
        }

        currentPlayer?.queueItems?.forEach {
            currentPlayer?.removeQueueItemWithId(it.playbackQueueId)
        }

        val mutable = queue.toMutableList()
        val first = mutable.removeFirstOrNull() ?: return
        val catalog = first.buildCatalog()
        currentPlayer?.repeatMode = PlaybackRepeatMode.REPEAT_MODE_OFF
        currentPlayer?.shuffleMode = PlaybackShuffleMode.SHUFFLE_MODE_OFF
        currentPlayer?.prepare(catalog)

        mutable
            .filter { it.entryId != first.entryId }
            .forEach { entry ->
                val item = entry.buildCatalog()
                currentPlayer?.addQueueItems(
                    item,
                    PlaybackQueueInsertionType.INSERTION_TYPE_AT_END
                )
            }
    }

    private fun EntryData.buildCatalog() = CatalogPlaybackQueueItemProvider
        .Builder()
        .let { builder ->
            when (this) {
                is PlaylistEntry -> builder
                    .containers(MediaContainerType.PLAYLIST, entryId)

                is MusicEntry -> builder
                    .items(MediaItemType.SONG, entryId)

                else -> builder
            }
        }
        .build()

    override fun stopPlayback() {
        currentPlayer?.stop()
    }

    override fun pausePlayback() {
        currentPlayer?.pause()
    }

    companion object {
        init {
            try {
                // Adding these two lines will prevent the OOM false alarm
                System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0")
                System.setProperty("org.bytedeco.javacpp.maxbytes", "0")

                System.loadLibrary("c++_shared")
                System.loadLibrary("appleMusicSDK")
            } catch (e: Exception) {
                Log.e("MediaPlayerNativePlayer", "Could not load library due to: " + Log.getStackTraceString(e))
                throw e
            }
        }
    }
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()
