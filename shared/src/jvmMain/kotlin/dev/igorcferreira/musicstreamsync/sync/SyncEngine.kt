package dev.igorcferreira.musicstreamsync.sync

import dev.igorcferreira.lastfm.LastFMClient
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.lastfm.model.Track
import dev.igorcferreira.musicstreamsync.domain.SystemLogger
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import dev.igorcferreira.lastfm.model.HTTPException as LastFmHTTPException
import dev.igorcferreira.musicstreamsync.network.model.HTTPException as AppleHTTPException

/**
 * Syncs one user's Apple Music recently-played history into Last.fm.
 *
 * ### Why this lives in `:shared` jvmMain
 * It drives [Configuration.appleMusicAPI] — an `internal` symbol only visible inside
 * `:shared`. The public surface ([sync] + [SyncResult]) is what the server (TASK_6) binds.
 *
 * ### Diff: top-K prefix cursor
 * `getRecentlyPlayed()` returns a **recency-ordered, de-duplicated** list with **no
 * timestamps** — a re-listen just moves a track to the head. Diffing is therefore
 * order-based against a persisted cursor (the top [CURSOR_SIZE] `entryId`s of the last
 * successful run), never timestamp-based. New plays are the head of the fetched list up to
 * the first position `i` where `fetched[i:]` is an **order-preserving subsequence** of the
 * cursor. A single-id cursor is unsafe (playing C then re-playing A yields `[A, C, …]`, and
 * cutting at `A` would drop both plays); matching the whole K-sequence recovers such
 * reorderings, and a subsequence (not a contiguous run) is required because pulling a middle
 * track to the head leaves a gap in the tail.
 *
 * ### Known limitation (accepted)
 * An immediate repeat of the current head track between two runs does not change the list
 * order and is undetectable — that play is lost. More generally, any sequence of re-listens
 * that reproduces a previous list prefix is ambiguous. The top-K match reduces the loss to
 * these genuinely ambiguous cases.
 */
@OptIn(ExperimentalTime::class)
class SyncEngine(
    private val configuration: Configuration,
    private val scrobbler: LastFmScrobbler,
    private val stateRepository: SyncStateRepository,
    private val clock: Clock = Clock.System,
) {
    /**
     * Convenience constructor wrapping a raw per-user [LastFMClient] in the production
     * [LastFmClientScrobbler]. TASK_6 builds the engine this way; tests inject a fake
     * [LastFmScrobbler] through the primary constructor.
     */
    constructor(
        configuration: Configuration,
        lastFMClient: LastFMClient,
        stateRepository: SyncStateRepository,
        clock: Clock = Clock.System,
    ) : this(configuration, LastFmClientScrobbler(lastFMClient), stateRepository, clock)

    private val appleMusicAPI get() = configuration.appleMusicAPI

    /**
     * Runs one sync for [userId]. Never throws for expected upstream failures — those are
     * surfaced in [SyncResult.error] with the cursor left untouched so the next run retries.
     * Coroutine cancellation still propagates.
     */
    suspend fun sync(userId: String): SyncResult {
        val state = stateRepository.loadState(userId)

        val fetched =
            try {
                appleMusicAPI.getRecentlyPlayed()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppleHTTPException) {
                return errorResult(userId, state, e.message, e.code)
            } catch (e: Exception) {
                return errorResult(userId, state, e.message, null)
            }

        // entryId drives the diff; the mapper always sets it, but the type is nullable.
        val usable = fetched.mapNotNull { entry -> entry.entryId?.let { id -> entry to id } }
        val skipped = fetched.size - usable.size
        if (skipped > 0) {
            SystemLogger.debug(TAG, "Skipped $skipped entries with a null entryId for $userId")
        }

        val fetchedIds = usable.map { it.second }
        // Preserve a good cursor when Apple returns an empty page (possibly transient):
        // overwriting it with `[]` would make the next non-empty run treat everything as new.
        val newCursor =
            if (fetchedIds.isEmpty() && state.cursor != null) state.cursor else fetchedIds.take(CURSOR_SIZE)

        // First run: seed the cursor, never replay history recorded before registration.
        if (state.cursor == null) {
            stateRepository.saveCursor(userId, newCursor, clock.now())
            return SyncResult(userId, fetched.size, 0, 0, 0, newCursor, firstRun = true)
        }

        val split = firstAlignedIndex(fetchedIds, state.cursor)
        val candidates = usable.take(split).map { it.first } // newest-first

        if (candidates.isEmpty()) {
            stateRepository.saveCursor(userId, newCursor, clock.now())
            return SyncResult(userId, fetched.size, 0, 0, 0, newCursor)
        }

        // Double-scrobble guard: drop plays the mobile apps already scrobbled in real time.
        val history =
            try {
                scrobbler.listLatestTracks()
            } catch (e: CancellationException) {
                throw e
            } catch (e: LastFmHTTPException) {
                return errorResult(userId, state, e.message, e.code.value)
            } catch (e: Exception) {
                return errorResult(userId, state, e.message, null)
            }

        val recentlyScrobbled = historyKeys(history, state.lastSyncAt)
        val toScrobble = candidates.filterNot { recentlyScrobbled.contains(entryKey(it.artist, it.title)) }
        val dropped = candidates.size - toScrobble.size

        if (toScrobble.isEmpty()) {
            stateRepository.saveCursor(userId, newCursor, clock.now())
            return SyncResult(userId, fetched.size, candidates.size, dropped, 0, newCursor)
        }

        try {
            scrobbler.scrobble(scrobblesFor(toScrobble))
        } catch (e: CancellationException) {
            throw e
        } catch (e: LastFmHTTPException) {
            return errorResult(userId, state, e.message, e.code.value)
        } catch (e: Exception) {
            return errorResult(userId, state, e.message, null)
        }

        stateRepository.saveCursor(userId, newCursor, clock.now())
        return SyncResult(userId, fetched.size, candidates.size, dropped, toScrobble.size, newCursor)
    }

    /**
     * Builds the scrobble batch for [newestFirst] candidates. Apple gives no timestamps, so
     * they are synthesised backwards from now: the newest gets `now`, each older one gets the
     * next-newer timestamp minus its own duration (fallback [FALLBACK_DURATION_SECONDS] when
     * duration is 0). The batch is returned in chronological (oldest-first) order with
     * strictly-increasing timestamps, none in the future.
     */
    private fun scrobblesFor(newestFirst: List<MusicEntry>): List<Scrobble> {
        val timestamps = arrayOfNulls<Instant>(newestFirst.size)
        var timestamp = clock.now()
        for (index in newestFirst.indices) {
            if (index > 0) {
                val duration = newestFirst[index].duration.takeIf { it > 0 } ?: FALLBACK_DURATION_SECONDS
                timestamp -= duration.seconds
            }
            timestamps[index] = timestamp
        }
        return newestFirst.indices.reversed().map { index ->
            val entry = newestFirst[index]
            Scrobble(
                track = entry.title,
                artist = entry.artist,
                timestamp = requireNotNull(timestamps[index]),
                album = entry.album,
                albumArtist = entry.albumArtist,
            )
        }
    }

    /**
     * Case-insensitive `(artist, title)` keys of Last.fm plays within the guard window: on or
     * after [since], or — when [since] is null (no recorded last sync) — the whole returned
     * page. A now-playing entry (null date) is always treated as recent.
     */
    private fun historyKeys(
        history: List<Track>,
        since: Instant?,
    ): Set<String> =
        history
            .asSequence()
            .filter { track ->
                val date = track.date
                since == null || date == null || date >= since
            }.map { entryKey(it.artist.text, it.name) }
            .toSet()

    private fun errorResult(
        userId: String,
        state: SyncState,
        message: String?,
        httpStatus: Int?,
    ) = SyncResult(
        userId = userId,
        fetched = 0,
        candidates = 0,
        droppedByGuard = 0,
        scrobbled = 0,
        cursor = state.cursor ?: emptyList(),
        error = SyncError(message ?: "Unknown error", httpStatus),
    )

    companion object {
        /** Number of head entryIds kept as the diff cursor. */
        const val CURSOR_SIZE = 10

        /** Timestamp spacing used when an entry reports a 0-second duration. */
        const val FALLBACK_DURATION_SECONDS = 180L

        private const val TAG = "SyncEngine"

        private fun entryKey(
            artist: String,
            title: String,
        ) = "${artist.lowercase()} ${title.lowercase()}"

        /**
         * Smallest index `i` such that `fetchedIds[i:]` is an order-preserving subsequence of
         * [cursor]; everything before `i` is a new play. Returns `fetchedIds.size` when nothing
         * aligns (every fetched entry is new).
         */
        internal fun firstAlignedIndex(
            fetchedIds: List<String>,
            cursor: List<String>,
        ): Int {
            for (index in fetchedIds.indices) {
                if (isSubsequence(fetchedIds, index, cursor)) return index
            }
            return fetchedIds.size
        }

        /** Whether `fetchedIds[from:]` appears in [cursor] in order (gaps allowed). */
        private fun isSubsequence(
            fetchedIds: List<String>,
            from: Int,
            cursor: List<String>,
        ): Boolean {
            var next = from
            for (id in cursor) {
                if (next >= fetchedIds.size) break
                if (fetchedIds[next] == id) next++
            }
            return next == fetchedIds.size
        }
    }
}
