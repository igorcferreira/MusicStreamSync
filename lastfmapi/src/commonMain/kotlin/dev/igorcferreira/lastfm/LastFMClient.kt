package dev.igorcferreira.lastfm

import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import dev.igorcferreira.lastfm.model.*
import dev.igorcferreira.lastfm.model.responses.NowPlayingResponse
import dev.igorcferreira.lastfm.model.responses.RecenteTracksResponse
import dev.igorcferreira.lastfm.model.responses.ScrobbleResponse
import dev.igorcferreira.lastfm.model.responses.SessionResponse
import dev.igorcferreira.lastfm.network.API
import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlin.coroutines.cancellation.CancellationException

internal var Settings.session: Session?
    get() = decodeValueOrNull(Session.serializer(), API.SESSION_KEY)
    set(value) = if (value == null) {
        remove(API.SESSION_KEY)
    } else {
        encodeValue(Session.serializer(), API.SESSION_KEY, value)
    }

@Suppress("unused")
class LastFMClient internal constructor(
    apiKey: String,
    apiSecret: String,
    private val settings: Settings
) {
    private val api = API(KeyHasher(apiKey, apiSecret), settings)
    val isAuthenticated: Boolean
        get() {
            val session: Session = settings.session ?: return false
            return session.key.isNotBlank()
        }

    constructor(apiKey: String, secret: String) : this(apiKey, secret, Settings())

    fun logout() = settings.clear()

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun authenticate(username: String, password: String): Session {
        val response: SessionResponse = api.post(
            "auth.getMobileSession", mapOf(
                "username" to username,
                "password" to password
            )
        )

        if (response.session.key.isBlank()) {
            throw HTTPException(HttpStatusCode.Unauthorized, "Invalid authentication")
        }

        settings.session = response.session

        return response.session
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun listLatestTracks(
        user: String? = null
    ): List<Track> {
        val response: RecenteTracksResponse = api.get(
            "user.getRecentTracks", mapOf(
                "user" to (user ?: settings.session?.name
                    ?: throw HTTPException(HttpStatusCode.Unauthorized, "Failed to get current user data"))
            )
        )
        return response.tracks
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun scrobble(
        items: List<Scrobble>
    ): Scrobble {
        try {
            val parameters = mutableMapOf<String, String>()

            items.forEachIndexed { index, scrobble ->
                parameters.apply {
                    put("artist[$index]", scrobble.artist.text)
                    put("track[$index]", scrobble.track.text)
                    put("timestamp[$index]", scrobble.timestamp.toString())
                    scrobble.album?.text?.let { put("album[$index]", it) }
                    scrobble.albumArtist?.text?.let { put("albumArtist[$index]", it) }
                }
            }

            val response: ScrobbleResponse = api.post("track.scrobble", parameters)
            return response.scrobble
        } catch (ex: Exception) {
            throw HTTPException(HttpStatusCode.InternalServerError, ex.message ?: "")
        }
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun scrobble(
        artist: String,
        track: String,
        timestamp: Instant,
        album: String? = null,
        albumArtist: String? = null,
    ): Scrobble = scrobble(items = listOf(Scrobble(track, artist, timestamp, album, albumArtist)))

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun updateNowPlaying(
        artist: String,
        track: String,
        album: String? = null,
        albumArtist: String? = null,
    ): NowPlaying {
        val response: NowPlayingResponse = api.post(
            "track.updateNowPlaying", mutableMapOf(
                "artist" to artist,
                "track" to track
            ).apply {
                album?.let { put("album", it) }
                albumArtist?.let { put("albumArtist", it) }
            })
        return response.nowPlaying
    }
}
