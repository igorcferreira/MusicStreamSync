package dev.igorcferreira.musicstreamsync.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import dev.igorcferreira.musicstreamsync.domain.JWTTokenSigner
import dev.igorcferreira.musicstreamsync.domain.MusicUserTokenProvider
import dev.igorcferreira.musicstreamsync.domain.player.MediaPlayerNativePlayer
import dev.igorcferreira.musicstreamsync.domain.use_cases.PlayerUseCase
import dev.igorcferreira.musicstreamsync.history.RecentlyPlayedViewModel
import dev.igorcferreira.musicstreamsync.model.BuildConfigDeveloperToken
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.player.PlayerViewModel
import dev.igorcferreira.musicstreamsync.playlist.PlaylistViewModel

class ViewModelFactory {
    companion object {
        private lateinit var playerInstance: MediaPlayerNativePlayer

        private val configuration = Configuration(BuildConfigDeveloperToken())

        private fun providePlayer(
            context: Context
        ): MediaPlayerNativePlayer {
            if (!::playerInstance.isInitialized) {
                playerInstance = MediaPlayerNativePlayer().apply {
                    preparePlayer(
                        context = context,
                        developerToken = BuildConfigDeveloperToken(),
                        tokenSigner = JWTTokenSigner(),
                        userTokenProvider = MusicUserTokenProvider.shared
                    )
                }
            }
            return playerInstance
        }

        val Playlist: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return PlaylistViewModel(configuration) as T
            }
        }

        val Player: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val player = providePlayer(application)
                val playerUseCase = PlayerUseCase(player)
                return PlayerViewModel(playerUseCase) as T
            }
        }

        val RecentlyPlayed: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return RecentlyPlayedViewModel(configuration) as T
            }
        }
    }
}
