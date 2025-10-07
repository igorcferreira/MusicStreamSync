package dev.igorcferreira.musicstreamsync.application

import android.app.Application
import dev.igorcferreira.musicstreamsync.domain.MusicUserTokenProvider

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MusicUserTokenProvider.shared.init(this)
    }
}