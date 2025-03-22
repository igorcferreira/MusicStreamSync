package dev.igorcferreira.lastfm

import dev.igorcferreira.lastfm.model.NowPlaying
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.lastfm.model.Session
import dev.igorcferreira.lastfm.model.responses.NowPlayingResponse
import dev.igorcferreira.lastfm.model.responses.ScrobbleResponse
import dev.igorcferreira.lastfm.model.responses.SessionResponse
import dev.igorcferreira.lastfm.network.API
import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import kotlinx.datetime.Instant

class LastFMClient(
    apiKey: String,
    apiSecret: String,
) {
    private val api = API(KeyHasher(apiKey, apiSecret))

    suspend fun authenticate(username: String, password: String): Session {
        val response: SessionResponse = api.post("auth.getMobileSession", mapOf(
            "username" to username,
            "password" to password
        ))
        return response.session
    }

    suspend fun scrobble(
        artist: String,
        track: String,
        timestamp: Instant,
        album: String?,
        session: String
    ): Scrobble {
        val response: ScrobbleResponse = api.post("track.scrobble", mutableMapOf(
            "artist[0]" to artist,
            "track[0]" to track,
            "timestamp[0]" to timestamp.epochSeconds.toString(),
            "sk" to session,
        ).apply { album?.let { put("album[0]", it) } })
        return response.scrobble
    }

    suspend fun updateNowPlaying(
        artist: String,
        track: String,
        album: String?,
        session: String
    ): NowPlaying {
        val response: NowPlayingResponse = api.post("track.updateNowPlaying", mutableMapOf(
            "artist" to artist,
            "track" to track,
            "sk" to session
        ).apply { album?.let { put("album", it) } })
        return response.nowPlaying
    }
}
