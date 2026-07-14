# PROGRESS — task/5-sync-engine

**Task:** TASK_5 — Sync engine (diff Apple Music history → scrobble delta)
**Branch:** `task/5-sync-engine` (cut from `feature/kotlin-server/base`)
**Spec:** [TASK_5_SPEC.md](TASK_5_SPEC.md)

> Branch-local scratch. MUST be deleted (deletion committed) before the PR is opened
> (the `spec-progress-guard` CI check fails any PR containing this file).

## Status: in progress

## Context / key findings from exploration

- `SyncEngine` lives in `:shared` **jvmMain** (`.../sync/`) because it needs the
  `internal` `AppleMusicAPI` (reached via `Configuration.appleMusicAPI`, an `internal val`
  — visible from jvmMain, same module).
- `AppleMusicAPI.getRecentlyPlayed()` returns `List<MusicEntry>` newest-first, dedup, **no
  timestamps**. Stable id = `MusicEntry.entryId` (`String?`; mapper always sets it, but the
  type is nullable → filter/skip nulls). `MusicEntry.duration` is **seconds** (Long).
- `LastFMClient` (from `:lastfmapi`, `api(project)` in shared commonMain so visible):
  `listLatestTracks(user?)` → `List<Track>` (Track.date is `Instant?`, **null on
  now-playing**); `scrobble(List<Scrobble>)`. `Scrobble(track, artist, timestamp: Instant,
  album?, albumArtist?)`.
- Two different `HTTPException` types: shared `network.model.HTTPException(code: Int,…)`
  thrown by AppleMusicAPI; lastfm `model.HTTPException(code: HttpStatusCode,…)` thrown by
  LastFMClient. Import both with aliases; map to `SyncError.httpStatus` (Int).
- Time: codebase uses `kotlin.time.Clock` / `kotlin.time.Instant` with
  `@OptIn(ExperimentalTime::class)` (see Scrobbler.kt, Track.kt). Mirror that.
- Test pattern (see `ResultUseCaseTests.kt`): `mock<AppleMusicAPI> { everySuspend {
  getRecentlyPlayed() } returns … }` then `Configuration(api)` (internal ctor, ok from
  jvmTest). Mokkery 3.4.2. `runTest`/`kotlin.test` available in jvmTest.

## Diff algorithm decision (top-K prefix cursor), K = 10

New plays = the head of the fetched list before the point where **`fetched[i:]` is an
order-preserving subsequence of the stored cursor** (smallest such `i`). This is the
reading of the spec's "remainder of the fetched list aligns with the stored cursor
sequence" that satisfies **every** Req 8 case:

- canonical A,B→[A,B]; +C→[C]; +A→[A]; unchanged→[]
- re-listen (cursor `[A,B,D]`, play C then A → fetched `[A,C,B,D]`): `[B,D]` is a subseq of
  cursor → i=2 → new plays `[A,C]` (not B/D). Contiguous-substring matching over-counts
  when a middle track is pulled to the head (`[A,B,C,D]`, re-listen B → `[B,A,C,D]`, tail
  `[A,C,D]` is a subseq but not contiguous), so **subsequence, not substring**.
- page overflow (no plays in cursor) → only empty suffix aligns → all new.

`cursor == null` ⇒ first run (init cursor to top-K, no scrobble). `cursor == emptyList`
(registered, no history) ⇒ everything fetched is new.

## Plan / checklist

- [ ] `sync/SyncStateRepository.kt` — `SyncState(cursor: List<String>?, lastSyncAt: Instant?)`
      + interface (`loadState`, `saveCursor`). TASK_6 binds to UserStore.
- [ ] `sync/SyncResult.kt` — counts + new cursor + `SyncError(message, httpStatus?)`.
- [ ] `sync/SyncEngine.kt` — `sync(userId)`: fetch → filter null entryId → first-run init →
      subsequence diff → guard (listLatestTracks, window = since lastSyncAt; null date and
      null lastSyncAt always included) → synth timestamps backwards (newest=now, older =
      newer − duration, fallback 180s) → batch scrobble chronological → saveCursor only on
      success. Errors surfaced in SyncResult (cursor untouched); CancellationException
      rethrown. KDoc the known re-listen limitation.
- [ ] Tests `jvmTest/.../sync/SyncEngineTest.kt`: canonical scenario, first-run, page
      overflow, cursor-track re-listen, guard drop, scrobble-failure-leaves-cursor,
      timestamp ordering, now-playing-in-history fixture.
- [ ] Validation: `ktlintCheck`, `:shared:jvmTest`, `:composeApp:assembleDebug`.
- [ ] Pre-PR senior-Kotlin review sub-agent, apply findings.
- [ ] Update README task table + session log; delete this file; open PR.

## Notes / decisions when resuming

- Also correct the stale README row: TASK_10 (PR #86) is **merged** → should be `done`
  (currently `in_review`). Fold that fix into this branch's README bookkeeping commit.
