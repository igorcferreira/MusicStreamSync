package dev.igorcferreira.musicstreamsync.domain.player

import dev.igorcferreira.musicstreamsync.model.SongEntry
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MediaPlayerNativePlayer : NativePlayer {
    override val isPlaying: Boolean = false
    override val currentPlaying: SongEntry?
        get() = queue.firstOrNull()

    private var queue = mutableListOf<SongEntry>()

    override fun startPlayback() {
        val id = currentPlaying?.id ?: return
        val name = currentPlaying?.title ?: return
        val formattedURL = "https://music.apple.com/song/$name/$id?i=$id"
        NSWorkspace.sharedWorkspace.openURL(NSURL(string = formattedURL))
        queue = mutableListOf()
    }

    override fun set(queue: List<SongEntry>) {
        this.queue = queue.toMutableList()
    }

    override fun stopPlayback() {}
    override fun pausePlayback() {}
}

internal actual fun buildNativePlayer(): MediaPlayerNativePlayer = MediaPlayerNativePlayer()