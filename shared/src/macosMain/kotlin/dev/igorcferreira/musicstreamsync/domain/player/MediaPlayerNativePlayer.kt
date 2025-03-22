package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer.PlayerState
import dev.igorcferreira.musicstreamsync.model.EntryData
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.model.entryId
import dev.igorcferreira.musicstreamsync.model.isPlaylist
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL
import platform.Foundation.base64Encoding
import platform.MediaRemote.MSCatalogItem
import platform.MediaRemote.MSMediaRemote
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {
    override val playerState: PlayerState
        get() = PlayerState.UNKNOWN
    override val isPlaying: Boolean
        get() = false
    override val currentPlaying: MusicEntry?
        get() = fetchCurrentItem()?.map()
    override val elapsedTime: Double
        get() = fetchCurrentItem()?.elapsedTime ?: 0.0

    private fun MSCatalogItem.map() = MusicEntry(
        id = catalogId,
        entryId = catalogId,
        title = title,
        artist = artist,
        artworkUrl = artworkUrl ?: "",
        duration = duration.toLong(),
        album = album,
        genres = listOf()
    )

    private val MSCatalogItem.artworkUrl: String?
        get() = artworkData?.let { "data:image/jpeg;base64,${it.base64Encoding()}" }

    private var queue = mutableListOf<EntryData>()
    private val mediaRemote = MSMediaRemote()

    private fun fetchCurrentItem(): MSCatalogItem? = runBlocking {
        suspendCoroutine { continuation ->
            mediaRemote.getCurrentItem { continuation.resume(it) }
        }
    }

    private fun urlFor(entry: EntryData): String {
        val id = entry.entryId
        val name = entry.title

        return if (entry.isPlaylist) {
            "https://music.apple.com/playlist/$name/$id?i=$id"
        } else {
            "https://music.apple.com/song/$name/$id?i=$id"
        }
    }

    override fun set(queue: List<EntryData>) {
        this.queue = queue.toMutableList()
    }

    override fun startPlayback() = openCurrentEntry()
    override fun stopPlayback() = openCurrentEntry()
    override fun pausePlayback() = openCurrentEntry()

    private fun openCurrentEntry() {
        val entry = queue.removeFirstOrNull() ?: currentPlaying ?: return
        val formattedURL = urlFor(entry)
        NSWorkspace.sharedWorkspace.openURL(NSURL(string = formattedURL))
        queue = mutableListOf()
    }
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()
