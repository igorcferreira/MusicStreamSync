package dev.igorcferreira.musicstreamsync.sync

/**
 * Outcome of a single [SyncEngine.sync] run for one user. Consumed by TASK_6 for the
 * sync-run log and `GET /api/users/status`.
 *
 * @property userId the user this run was for.
 * @property fetched number of entries returned by Apple Music (before filtering).
 * @property candidates new plays detected by the diff (before the double-scrobble guard).
 * @property droppedByGuard candidates dropped because they were already in Last.fm history.
 * @property scrobbled number of tracks submitted to Last.fm.
 * @property cursor the top-K cursor as it stands after this run (new one on success, the
 *   untouched previous one on error).
 * @property firstRun `true` when this run only initialised the cursor (no history replay).
 * @property error non-null when the run failed; the cursor was left untouched for retry.
 */
data class SyncResult(
    val userId: String,
    val fetched: Int,
    val candidates: Int,
    val droppedByGuard: Int,
    val scrobbled: Int,
    val cursor: List<String>,
    val firstRun: Boolean = false,
    val error: SyncError? = null,
) {
    val isSuccess: Boolean
        get() = error == null
}

/**
 * Error surfaced by [SyncEngine.sync] instead of throwing, so a scheduler loop can move on
 * to the next user. [httpStatus] carries the upstream HTTP code when the failure was an
 * Apple Music / Last.fm HTTP error, so TASK_6 can map 401/403 to `tokenStale`.
 */
data class SyncError(
    val message: String,
    val httpStatus: Int? = null,
)
