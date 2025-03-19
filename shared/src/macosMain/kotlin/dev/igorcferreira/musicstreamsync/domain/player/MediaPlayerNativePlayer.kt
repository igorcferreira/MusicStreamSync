package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.MusicEntry
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {
    override val isPlaying: Boolean = false
    override val currentPlaying: MusicEntry?
        get() = queue.firstOrNull()

    private var queue = mutableListOf<MusicEntry>()

    private fun urlFor(entry: MusicEntry): String {
        val id = entry.entryId
        val name = entry.title

        return if (entry.isPlaylist) {
            "https://music.apple.com/playlist/$name/$id?i=$id"
        } else {
            "https://music.apple.com/song/$name/$id?i=$id"
        }
    }

    override fun startPlayback() {
        val entry = currentPlaying ?: return
        val formattedURL = urlFor(entry)
        NSWorkspace.sharedWorkspace.openURL(NSURL(string = formattedURL))
        queue = mutableListOf()
    }

    override fun set(queue: List<MusicEntry>) {
        this.queue = queue.toMutableList()
    }

    override fun stopPlayback() {}
    override fun pausePlayback() {}
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()
