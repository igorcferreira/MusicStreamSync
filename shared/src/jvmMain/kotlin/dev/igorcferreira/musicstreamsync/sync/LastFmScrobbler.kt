package dev.igorcferreira.musicstreamsync.sync

import dev.igorcferreira.lastfm.LastFMClient
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.lastfm.model.Track

/**
 * The slice of Last.fm the [SyncEngine] needs: read the user's recent history (for the
 * double-scrobble guard) and submit a batch of scrobbles.
 *
 * [SyncEngine] depends on this interface rather than the concrete [LastFMClient] so it can
 * be exercised with a fake — the project's Mokkery configuration refuses to mock final
 * classes, and `LastFMClient` is final. [LastFmClientScrobbler] is the production adapter;
 * the [SyncEngine] convenience constructor wraps a raw client in it automatically.
 */
interface LastFmScrobbler {
    /** The user's recent Last.fm tracks (uses the client's stored session name). */
    suspend fun listLatestTracks(): List<Track>

    /** Submits [items] as a single scrobble batch. */
    suspend fun scrobble(items: List<Scrobble>)
}

/** Production [LastFmScrobbler] backed by a per-user [LastFMClient]. */
class LastFmClientScrobbler(
    private val client: LastFMClient,
) : LastFmScrobbler {
    override suspend fun listLatestTracks(): List<Track> = client.listLatestTracks()

    override suspend fun scrobble(items: List<Scrobble>) {
        client.scrobble(items)
    }
}
