package dev.igorcferreira.musicstreamsync.domain.use_cases

import dev.igorcferreira.musicstreamsync.domain.SystemLogger
import dev.igorcferreira.musicstreamsync.domain.player.NativePlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.seconds

@HiddenFromObjC
@Suppress("PropertyName")
internal class PlayerWatcher(
    private val nativePlayer: NativePlayer
) {
    private val coroutineContext = CoroutineScope(Dispatchers.Unconfined)
    val _isPlaying = MutableStateFlow(nativePlayer.isPlaying)
    val _playingItem = MutableStateFlow(nativePlayer.currentPlaying)

    @Suppress("unused")
    private val task: Job = coroutineContext.async(start = CoroutineStart.UNDISPATCHED) {
        while (true) {
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

    companion object {
        private lateinit var sharedWatcher: PlayerWatcher

        internal fun getWatcher(nativePlayer: NativePlayer): PlayerWatcher {
            if (!::sharedWatcher.isInitialized) {
                sharedWatcher = PlayerWatcher(nativePlayer)
            }
            return sharedWatcher
        }
    }
}
