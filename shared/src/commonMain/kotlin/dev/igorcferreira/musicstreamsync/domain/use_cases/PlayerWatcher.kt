package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.SystemLogger
import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.seconds

@HiddenFromObjC
internal class PlayerWatcher(
    private val nativePlayer: NativePlayer
) {
    private val coroutineContext = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _isPlaying = MutableStateFlow(nativePlayer.isPlaying)
    private val _playingItem = MutableStateFlow(nativePlayer.currentPlaying)
    private var task: Job? = null

    val isPlaying: StateFlow<Boolean> get() = _isPlaying.asStateFlow()
    val playingItem: StateFlow<MusicEntry?> get() = _playingItem.asStateFlow()

    private fun start() {
        task?.cancel()
        task =  coroutineContext.launch {
            while (isActive) {
                val isPlaying = nativePlayer.isPlaying
                if (isPlaying != _isPlaying.value) {
                    _isPlaying.update { isPlaying }
                }

                val current = nativePlayer.currentPlaying
                if (current?.entryId != _playingItem.value?.entryId) {
                    SystemLogger.info("PlayerWatcher", "Player is playing ${current?.title} - ${current?.artist}")
                    _playingItem.update { current }
                }

                delay(1.seconds)
            }
        }
    }

    private fun destroy() {
        task?.cancel()
        task = null
    }

    companion object {
        private var sharedWatcher: PlayerWatcher? = null

        internal fun getWatcher(nativePlayer: NativePlayer): PlayerWatcher {
            val watcher = sharedWatcher ?: PlayerWatcher(nativePlayer)
            watcher.start()
            return watcher
        }

        internal fun destroyWatcher() {
            sharedWatcher?.destroy()
            sharedWatcher = null
        }
    }
}
