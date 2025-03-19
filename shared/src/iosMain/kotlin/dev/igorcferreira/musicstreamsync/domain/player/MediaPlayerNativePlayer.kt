package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.MusicEntry
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
    override val isPlaying: Boolean
        get() = player.playbackState() == MPMusicPlaybackState.MPMusicPlaybackStatePlaying
    override val currentPlaying: MusicEntry?
        get() = player.nowPlayingItem()?.let { item ->
            return MusicEntry(
                id = item.playbackStoreID,
                entryId = item.playbackStoreID,
                title = item.title ?: "",
                artist = item.artist ?: "",
                artworkUrl = item.getArtwork(),
                duration = item.playbackDuration.toLong(),
                album = item.albumTitle ?: "",
                genres = listOf(item.genre).mapNotNull { it }
            )
        }

    private val player = MPMusicPlayerController.systemMusicPlayer()

    override fun startPlayback() {
        player.play()
    }

    override fun set(queue: List<MusicEntry>) {
        player.setQueueWithStoreIDs(queue.map(MusicEntry::entryId))
    }

    override fun stopPlayback() {
        player.stop()
    }

    override fun pausePlayback() {
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
