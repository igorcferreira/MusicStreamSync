package dev.igorcferreira.lastfm

import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import dev.igorcferreira.lastfm.model.HTTPException
import dev.igorcferreira.lastfm.model.NowPlaying
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.lastfm.model.Session
import dev.igorcferreira.lastfm.model.Track
import dev.igorcferreira.lastfm.model.responses.NowPlayingResponse
import dev.igorcferreira.lastfm.model.responses.RecenteTracksResponse
import dev.igorcferreira.lastfm.model.responses.ScrobbleResponse
import dev.igorcferreira.lastfm.model.responses.SessionResponse
import dev.igorcferreira.lastfm.network.API
import dev.igorcferreira.lastfm.network.authentication.KeyHasher
import dev.igorcferreira.lastfm.storage.InMemorySettings
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private fun Session.requireUsable(): Session {
    require(key.isNotBlank()) { "Session key must not be blank" }
    return this
}

internal var Settings.session: Session?
    get() = decodeValueOrNull(Session.serializer(), API.SESSION_KEY)
    set(value) =
        if (value == null) {
            remove(API.SESSION_KEY)
        } else {
            encodeValue(Session.serializer(), API.SESSION_KEY, value)
        }

/**
 * Client for the Last.fm API.
 *
 * Sessions are portable: a client that ran [authenticate] on-device can export its
 * session via [currentSession]; another process restores it with
 * `LastFMClient(apiKey, secret, session)` (or [restoreSession]) and is authenticated
 * without ever seeing the user's password.
 *
 * Sessions imported through the **session constructor** are **instance-scoped**: the
 * client is backed by its own in-memory storage, so a multi-user server can hold one
 * client per user in one process without the sessions colliding. The two-argument
 * constructor keeps the existing mobile behavior (process-global [Settings]
 * persistence) — a multi-user server must therefore always use the session
 * constructor; [restoreSession] alone does not re-scope a client (see its docs).
 */
@Suppress("unused")
class LastFMClient internal constructor(
    apiKey: String,
    apiSecret: String,
    private val settings: Settings,
) {
    private val api = API(KeyHasher(apiKey, apiSecret), settings)
    val isAuthenticated: Boolean
        get() {
            val session: Session = settings.session ?: return false
            return session.key.isNotBlank()
        }

    /** The stored [Session], or `null` when not authenticated. */
    val currentSession: Session?
        get() = settings.session?.takeIf { it.key.isNotBlank() }

    constructor(apiKey: String, secret: String) : this(apiKey, secret, Settings())

    /**
     * Builds a client already authenticated with a [session] exported elsewhere
     * (see [currentSession]). The session is instance-scoped: it is held in in-memory
     * storage owned by this client and is not persisted.
     *
     * @throws IllegalArgumentException when [session] has a blank key.
     */
    constructor(apiKey: String, secret: String, session: Session) : this(
        apiKey,
        secret,
        InMemorySettings().apply { this.session = session.requireUsable() },
    )

    /**
     * Replaces the stored session with a [session] exported elsewhere. Subsequent calls
     * sign with the imported key exactly as if [authenticate] had run locally.
     *
     * The session lands in **whatever storage backs this client**: instance-scoped
     * in-memory storage for clients built with the session constructor, the
     * process-global persistent [Settings] for clients built with the two-argument
     * constructor. Never use this to multiplex users over two-argument clients — those
     * all share one storage and the sessions would overwrite each other; a multi-user
     * server must build one client per user via the session constructor.
     *
     * @throws IllegalArgumentException when [session] has a blank key.
     */
    fun restoreSession(session: Session) {
        settings.session = session.requireUsable()
    }

    fun logout() = settings.clear()

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun authenticate(
        username: String,
        password: String,
    ): Session {
        val response: SessionResponse =
            api.post(
                "auth.getMobileSession",
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            )

        if (response.session.key.isBlank()) {
            throw HTTPException(HttpStatusCode.Unauthorized, "Invalid authentication")
        }

        settings.session = response.session

        return response.session
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun listLatestTracks(user: String? = null): List<Track> {
        val response: RecenteTracksResponse =
            api.get(
                "user.getRecentTracks",
                mapOf(
                    "user" to (
                        user ?: settings.session?.name
                            ?: throw HTTPException(HttpStatusCode.Unauthorized, "Failed to get current user data")
                    ),
                ),
            )
        return response.scrobbledTracks
    }

    @Throws(HTTPException::class, CancellationException::class)
    suspend fun scrobble(items: List<Scrobble>): Scrobble {
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
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: HTTPException) {
            throw ex
        } catch (ex: Exception) {
            throw HTTPException(HttpStatusCode.InternalServerError, ex.message ?: "")
        }
    }

    @OptIn(ExperimentalTime::class)
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
        val response: NowPlayingResponse =
            api.post(
                "track.updateNowPlaying",
                mutableMapOf(
                    "artist" to artist,
                    "track" to track,
                ).apply {
                    album?.let { put("album", it) }
                    albumArtist?.let { put("albumArtist", it) }
                },
            )
        return response.nowPlaying
    }
}
