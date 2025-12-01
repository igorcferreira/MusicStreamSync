package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer.PlayerState
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.model.entryId
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.Foundation.base64Encoding
import platform.MediaPlayer.MPMediaItem
import platform.MediaPlayer.MPMusicPlaybackState
import platform.MediaPlayer.MPMusicPlayerController
import platform.UIKit.UIImageJPEGRepresentation

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {
    actual override val playerState: PlayerState
        get() = if (isPlaying) PlayerState.PLAYING else PlayerState.NOT_PLAYING
    actual override val isPlaying: Boolean
        get() = player.playbackState() == MPMusicPlaybackState.MPMusicPlaybackStatePlaying
    actual override val currentPlaying: MusicEntry?
        get() = player.nowPlayingItem()?.let { item ->
            return MusicEntry(
                id = item.playbackStoreID,
                entryId = item.playbackStoreID,
                title = item.title ?: "",
                artist = item.artist ?: "",
                artworkUrl = item.getArtwork(),
                duration = item.playbackDuration.toLong(),
                album = item.albumTitle ?: "",
                albumArtist = item.albumArtist ?: "",
                genres = listOf(item.genre).mapNotNull { it }
            )
        }
    actual override val elapsedTime: Double
        get() = player.currentPlaybackTime

    private val player = MPMusicPlayerController.systemMusicPlayer()

    actual override fun startPlayback() {
        player.play()
    }

    actual override fun set(queue: List<EntryData>) {
        player.setQueueWithStoreIDs(queue.map { it.entryId })
    }

    actual override fun stopPlayback() {
        player.stop()
    }

    actual override fun pausePlayback() {
        player.pause()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun MPMediaItem.getArtwork(): String {
        val itemArtwork = artwork ?: return ""
        val size = itemArtwork.bounds.useContents { size }
            .readValue()
        val image = itemArtwork.imageWithSize(size) ?: return ""
        val data = UIImageJPEGRepresentation(image, compressionQuality = 1.0) ?: return ""
        return "data:image/jpeg;base64,${data.base64Encoding()}"
    }
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()
