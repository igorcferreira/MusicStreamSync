package dev.igorcferreira.musicstreamsync.sync

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Per-user sync state consumed by [SyncEngine].
 *
 * @property cursor the ordered (newest-first) list of the top-K `entryId`s persisted by the
 *   last successful run, or `null` when the user has **never** been synced (first run).
 *   An empty list means the user is registered but had no play history at the last run.
 * @property lastSyncAt the instant of the last successful run, used to bound the
 *   double-scrobble guard window; `null` when never synced.
 */
@OptIn(ExperimentalTime::class)
data class SyncState(
    val cursor: List<String>?,
    val lastSyncAt: Instant?,
)

/**
 * Minimal persistence surface [SyncEngine] needs. TASK_4's `UserStore` binds to this in
 * TASK_6; defined here so the engine can be built and tested without the server module.
 *
 * Implementations must apply [saveCursor] as a **field-level** update (never touching the
 * user's tokens, session, or sync log) — API handlers and the scheduler race each other.
 */
@OptIn(ExperimentalTime::class)
interface SyncStateRepository {
    /** Loads the current [SyncState] for [userId]. */
    suspend fun loadState(userId: String): SyncState

    /**
     * Persists the new top-K [cursor] and records [syncedAt] as the last successful sync.
     * Called only after a run succeeds (nothing to scrobble, or the scrobble batch
     * completed); a failed run leaves the previous cursor untouched so the next run retries.
     */
    suspend fun saveCursor(
        userId: String,
        cursor: List<String>,
        syncedAt: Instant,
    )
}
