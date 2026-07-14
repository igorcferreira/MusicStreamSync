package dev.igorcferreira.musicstreamsync.sync

import dev.igorcferreira.lastfm.model.CorrectableText
import dev.igorcferreira.lastfm.model.Scrobble
import dev.igorcferreira.lastfm.model.Track
import dev.igorcferreira.musicstreamsync.model.Configuration
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.network.AppleMusicAPI
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import dev.igorcferreira.lastfm.model.HTTPException as LastFmHTTPException
import dev.igorcferreira.musicstreamsync.network.model.HTTPException as AppleHTTPException

@OptIn(ExperimentalTime::class)
class SyncEngineTest {
    private val userId = "user-1"
    private val base = Instant.fromEpochSeconds(1_700_000_000)

    // --- Diff algorithm (top-K subsequence) --------------------------------------------

    @Test
    fun firstAlignedIndex_reListenOfAMiddleTrack_onlyCountsTheMovedTrack() {
        // cursor [A,B,C,D]; user re-listens B → fetched [B,A,C,D]. Only B is new.
        val split =
            SyncEngine.firstAlignedIndex(
                fetchedIds = listOf("B", "A", "C", "D"),
                cursor = listOf("A", "B", "C", "D"),
            )
        assertEquals(1, split)
    }

    @Test
    fun firstAlignedIndex_noOverlap_treatsWholePageAsNew() {
        val split =
            SyncEngine.firstAlignedIndex(
                fetchedIds = listOf("X", "Y", "Z"),
                cursor = listOf("A", "B"),
            )
        assertEquals(3, split)
    }

    // --- Canonical scenario (README) ---------------------------------------------------

    @Test
    fun canonicalScenario_scrobblesEachDeltaThenNothing() =
        runTest {
            val a = entry("A")
            val b = entry("B")
            val c = entry("C")

            val api = mock<AppleMusicAPI>()
            val scrobbler = FakeLastFmScrobbler()
            // Registered user with an initialised, empty cursor (first run already happened).
            val repo = FakeSyncStateRepository(SyncState(cursor = emptyList(), lastSyncAt = null))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            suspend fun step(vararg apple: MusicEntry): SyncResult {
                everySuspend { api.getRecentlyPlayed() } returns apple.toList()
                return engine.sync(userId)
            }

            // A then B → Apple recency [B, A]; scrobbles chronological [A, B].
            assertEquals(2, step(b, a).scrobbled)
            // + C → [C, B, A]; scrobbles [C].
            assertEquals(1, step(c, b, a).scrobbled)
            // + A again → [A, C, B]; scrobbles [A].
            assertEquals(1, step(a, c, b).scrobbled)
            // unchanged → nothing.
            assertEquals(0, step(a, c, b).scrobbled)

            assertEquals(
                listOf(listOf("A", "B"), listOf("C"), listOf("A")),
                scrobbler.batches.map { batch -> batch.map { it.track.text } },
            )
        }

    // --- Edge cases --------------------------------------------------------------------

    @Test
    fun firstRun_initialisesCursorWithoutScrobbling() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns listOf(entry("B"), entry("A"))
                }
            val scrobbler = FakeLastFmScrobbler()
            val repo = FakeSyncStateRepository(SyncState(cursor = null, lastSyncAt = null))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertTrue(result.firstRun)
            assertEquals(0, result.scrobbled)
            assertEquals(listOf("B", "A"), repo.state.cursor)
            assertEquals(1, repo.saves.size)
            assertTrue(scrobbler.batches.isEmpty())
        }

    @Test
    fun pageOverflow_noPrefixMatch_scrobblesEntirePage() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns listOf(entry("X"), entry("Y"), entry("Z"))
                }
            val scrobbler = FakeLastFmScrobbler()
            // Old cursor tracks all rolled off the page.
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("A", "B"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertEquals(3, result.scrobbled)
            assertEquals(listOf("Z", "Y", "X"), scrobbler.batches.single().map { it.track.text })
        }

    @Test
    fun reListenOfCursorTrack_scrobblesBothTheNewAndTheReListenedTrack() =
        runTest {
            // cursor [A, B, D]; play C then A → fetched [A, C, B, D]. New plays: C and A.
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns
                        listOf(entry("A"), entry("C"), entry("B"), entry("D"))
                }
            val scrobbler = FakeLastFmScrobbler()
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("A", "B", "D"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertEquals(2, result.candidates)
            assertEquals(2, result.scrobbled)
            assertEquals(listOf("C", "A"), scrobbler.batches.single().map { it.track.text })
        }

    @Test
    fun guardDropsTracksAlreadyInLastFmHistory() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns listOf(entry("C"), entry("B"), entry("A"))
                }
            // The app already scrobbled C in real time (matched case-insensitively).
            val scrobbler =
                FakeLastFmScrobbler(
                    history = listOf(track(title = "c", artist = "artist c", date = base + 10.seconds)),
                )
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("B", "A"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base + 60.seconds))

            val result = engine.sync(userId)

            assertEquals(1, result.candidates)
            assertEquals(1, result.droppedByGuard)
            assertEquals(0, result.scrobbled)
            assertTrue(scrobbler.batches.isEmpty())
        }

    @Test
    fun guardTreatsNowPlayingEntryAsRecent() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns listOf(entry("C"), entry("B"), entry("A"))
                }
            // Now-playing entry has a null date but must still be treated as recent.
            val scrobbler =
                FakeLastFmScrobbler(history = listOf(track(title = "C", artist = "Artist C", date = null)))
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("B", "A"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base + 60.seconds))

            val result = engine.sync(userId)

            assertEquals(1, result.droppedByGuard)
            assertEquals(0, result.scrobbled)
        }

    @Test
    fun scrobbleFailure_leavesCursorUntouchedAndSurfacesStatus() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns listOf(entry("C"), entry("B"), entry("A"))
                }
            val scrobbler =
                FakeLastFmScrobbler(
                    scrobbleError = LastFmHTTPException(HttpStatusCode.Unauthorized, "bad session"),
                )
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("B", "A"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertEquals(401, result.error?.httpStatus)
            assertEquals(listOf("B", "A"), result.cursor)
            assertTrue(repo.saves.isEmpty()) // cursor untouched → next run retries
        }

    @Test
    fun appleFetchFailure_isSurfacedInResult() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } throws AppleHTTPException(403, "forbidden")
                }
            val scrobbler = FakeLastFmScrobbler()
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("A"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertEquals(403, result.error?.httpStatus)
            assertTrue(repo.saves.isEmpty())
        }

    @Test
    fun timestampsAreSynthesisedBackwardsAndStrictlyIncreasing() =
        runTest {
            // newest-first with distinct durations; B has 0 duration → fallback spacing.
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns
                        listOf(
                            entry("A", duration = 100),
                            entry("B", duration = 0),
                            entry("C", duration = 200),
                        )
                }
            val scrobbler = FakeLastFmScrobbler()
            val now = base + 1000.seconds
            val repo = FakeSyncStateRepository(SyncState(cursor = emptyList(), lastSyncAt = null))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(now))

            engine.sync(userId)

            val batch = scrobbler.batches.single()
            // Chronological (oldest-first): C, B, A.
            assertEquals(listOf("C", "B", "A"), batch.map { it.track.text })
            val ts = batch.map { it.timestamp }
            assertTrue(ts.zipWithNext().all { (older, newer) -> older < newer }) // strictly increasing
            // newest A = now (never future); each older = newer's ts minus its own duration:
            // B = A.ts - B.duration(0→180); C = B.ts - C.duration(200).
            assertEquals(now.epochSeconds, ts[2])
            assertEquals(now.epochSeconds - 180, ts[1])
            assertEquals(now.epochSeconds - 180 - 200, ts[0])
        }

    @Test
    fun emptyAppleList_isANoOpThatPreservesTheCursor() =
        runTest {
            val api =
                mock<AppleMusicAPI> {
                    everySuspend { getRecentlyPlayed() } returns emptyList()
                }
            val scrobbler = FakeLastFmScrobbler()
            val repo = FakeSyncStateRepository(SyncState(cursor = listOf("A"), lastSyncAt = base))
            val engine = SyncEngine(Configuration(api), scrobbler, repo, FixedClock(base))

            val result = engine.sync(userId)

            assertNull(result.error)
            assertEquals(0, result.scrobbled)
            assertEquals(0, result.candidates)
            assertEquals(listOf("A"), repo.state.cursor) // transient empty page must not wipe the cursor
        }

    // --- Fixtures ----------------------------------------------------------------------

    private fun entry(
        id: String,
        title: String = id,
        artist: String = "Artist $id",
        duration: Long = 200,
    ) = MusicEntry(
        id = id,
        entryId = id,
        title = title,
        artist = artist,
        artworkUrl = "",
        duration = duration,
        album = "Album $id",
        albumArtist = null,
        genres = emptyList(),
    )

    private fun track(
        title: String,
        artist: String,
        date: Instant?,
    ) = Track(
        name = title,
        artist = CorrectableText(artist),
        uts = date?.let { Track.TrackDate(it.epochSeconds) },
        album = null,
    )

    private class FakeLastFmScrobbler(
        private val history: List<Track> = emptyList(),
        private val scrobbleError: Throwable? = null,
    ) : LastFmScrobbler {
        val batches = mutableListOf<List<Scrobble>>()

        override suspend fun listLatestTracks(): List<Track> = history

        override suspend fun scrobble(items: List<Scrobble>) {
            scrobbleError?.let { throw it }
            batches.add(items)
        }
    }

    private class FakeSyncStateRepository(
        initial: SyncState,
    ) : SyncStateRepository {
        var state: SyncState = initial
            private set
        val saves = mutableListOf<Pair<List<String>, Instant>>()

        override suspend fun loadState(userId: String): SyncState = state

        override suspend fun saveCursor(
            userId: String,
            cursor: List<String>,
            syncedAt: Instant,
        ) {
            state = SyncState(cursor, syncedAt)
            saves.add(cursor to syncedAt)
        }
    }

    private class FixedClock(
        private val instant: Instant,
    ) : Clock {
        override fun now(): Instant = instant
    }
}
