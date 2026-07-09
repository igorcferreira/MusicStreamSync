# TASK_5 — Sync engine (diff Apple Music history → scrobble delta)

Branch: `task/5-sync-engine` · Depends on: TASK_1, TASK_2 · Protocol: [AGENT.md](AGENT.md)

## Goal

The core logic: for **one user**, fetch the Apple Music recently-played list, determine
which plays are new since the last run, and scrobble them to Last.fm — exactly
reproducing the canonical scenario in [README.md](README.md) (Songs A/B → A+B scrobbled;
C → C; A again → A; nothing new → nothing).

## Context

- `AppleMusicAPI.getRecentlyPlayed()` (`shared/src/commonMain/.../network/AppleMusicAPI.kt`)
  calls `GET /v1/me/recent/played/tracks` and maps to `List<MusicEntry>`.
  **Critical Apple behavior:** the endpoint returns a *recency-ordered, de-duplicated*
  list of tracks with **no play timestamps**. A re-listen moves the track to the head.
  Diffing must therefore be **order-based against a persisted cursor**, not
  timestamp-based.
- **Visibility constraint:** `AppleMusicAPI` and `URLSession` are `internal` to
  `:shared` — a separate `:server` module cannot reference them. That is why
  `SyncEngine` lives in `:shared` **jvmMain** (see Req 1), where `internal` symbols are
  visible; `:server` (TASK_6) consumes only `SyncEngine`'s public surface. Per-user
  construction goes through the public `Configuration(developerToken, tokenSigner,
  userTokenProvider)` path added by TASK_1.
- `MusicEntry.id` is index-prefixed by the mapper (`"${index}_$id"`); the stable track
  identifier is **`MusicEntry.entryId`** — cursors must use `entryId`. Note `entryId`
  is declared `String?` (always non-null from the recently-played mapper, but the diff
  code must handle the type — e.g. skip/log entries with null `entryId`).
- `MusicEntry.duration` is in **seconds** (mapped from Apple's `durationInMillis`).
- `LastFMClient.listLatestTracks(user)` returns `List<Track>` (name, artist, album,
  `date: Instant`). `LastFMClient.scrobble(items: List<Scrobble>)` batch-scrobbles;
  `Scrobble` carries track/artist/timestamp/album/albumArtist.
- Per-user state comes from TASK_4's `UserStore` (`syncCursor` = the top-K prefix
  cursor below, sync-run log). Define a minimal `SyncStateRepository` interface in this
  task (TASK_4 need not be merged) and let TASK_6 bind it to `UserStore`.
- Existing mobile scrobbling (`shared/.../domain/Scrobbler.kt`) scrobbles at
  `Clock.System.now()` — the server may double-report plays the app already scrobbled;
  hence the Last.fm cross-check below.

## Requirements

1. **`SyncEngine`** — a pure, constructor-injected **public** class in `:shared`
   **jvmMain** (it needs visibility of the `internal` `AppleMusicAPI`; see Context):
   `SyncEngine(configuration, lastFMClient, stateRepository, clock)` with a single
   entry point `suspend fun sync(userId: String): SyncResult`. Only public types in its
   signatures, so `:server` (TASK_6) can drive it.
2. **Diff algorithm (top-K prefix cursor):**
   - Fetch the Apple recent list (newest first).
   - The persisted cursor is the **ordered list of the top K entryIds** from the last
     successful run (K = 10, constant).
   - New plays = entries from the head until the **longest prefix match**: the first
     position where the remainder of the fetched list aligns with the stored cursor
     sequence. A single-id cursor is wrong here: with cursor `[A]` and plays C then A,
     Apple returns `[A, C, …]` and cutting at `A` (index 0) would silently drop both
     plays; matching against the K-sequence recovers such reorderings.
   - A re-listen of an already-synced song appears before the matched prefix and is
     therefore detected (satisfies the "Song A again" step of the scenario).
3. **Double-scrobble guard:** before scrobbling, fetch `listLatestTracks()` and drop any
   candidate whose (artist, title) — case-insensitive — already appears in Last.fm
   history within a recent window (since the last successful sync time; when no
   last-sync time is recorded, fall back to the returned page — note
   `listLatestTracks()` has **no limit parameter**, it returns Last.fm's default page
   size, so a heavy listener can outrun the window). This protects against plays the
   mobile apps already scrobbled in real time.
4. **Scrobbling:** submit the remaining candidates in **chronological order** (reverse
   of Apple's list) via one batch `scrobble()` call. Apple provides no timestamps, so
   synthesize them backwards from `clock.now()`: the newest candidate gets `now`, each
   previous one gets the next one's timestamp minus that track's duration (fallback 3
   minutes when duration is 0). Timestamps must be strictly increasing in the submitted
   batch and never in the future.
5. **Cursor update:** persist the new top-K cursor **only after** the scrobble batch
   succeeds (or when there was nothing to scrobble). A failed scrobble leaves the cursor
   untouched so the next run retries.
6. **Edge cases (all spec'd behavior, all tested):**
   - *First run (no cursor):* initialize the cursor to the current top-K **without
     scrobbling** (history before registration is not replayed).
   - *No prefix match in the fetched page* (more plays than the page size since last
     run): treat the entire page as new plays.
   - *Empty Apple list* or *empty diff:* no-op, still records a sync-run log entry.
   - *Known limitation (document in KDoc + in this spec):* an immediate repeat of the
     current head track between two runs does not change the list order and is
     undetectable — that play is lost. More generally, any sequence of re-listens that
     reproduces a previous list prefix is ambiguous; the top-K prefix match reduces the
     loss to these genuinely ambiguous cases. Accepted.
7. **`SyncResult`** value type: counts (candidates, dropped-by-guard, scrobbled), new
   cursor, error info — consumed by TASK_6 for the sync-run log and `/api/users/status`.
8. **Tests** (Mokkery mocks or hand-rolled fakes for `AppleMusicAPI`, `LastFMClient`,
   repository; virtual clock):
   - The **full canonical scenario** from README as a sequence of `sync()` calls:
     A,B → scrobbles [A,B]; +C → [C]; +A → [A]; unchanged → [].
   - First-run initialization; no-prefix-match page overflow; **cursor-track re-listen**
     (cursor `[A, …]`, plays C then A → both C and A scrobbled); guard drops an
     app-scrobbled track; scrobble failure leaves cursor untouched; timestamp synthesis
     ordering; now-playing entry present in Last.fm history (TASK_2 fixture).

## Non-goals

- No scheduling/looping over users (TASK_6).
- No HTTP surface changes (no `openapi.yaml` update needed).
- No token refresh handling beyond surfacing errors in `SyncResult` (TASK_6 maps 401/403
  to `tokenStale`).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :shared:jvmTest
./gradlew :composeApp:assembleDebug
```

Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
