package dev.igorcferreira.lastfm.model.responses

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecentTracksResponseTests {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testDecodesNowPlayingTrackWithoutDate() {
        val response = json.decodeFromString<RecenteTracksResponse>(FIXTURE)

        assertEquals(3, response.tracks.size)
        assertNull(response.tracks.first().uts)
        assertNull(response.tracks.first().date)
    }

    @Test
    fun testScrobbledTracksDropTheNowPlayingEntry() {
        val response = json.decodeFromString<RecenteTracksResponse>(FIXTURE)

        assertEquals(listOf("Song B", "Song A"), response.scrobbledTracks.map { it.name })
    }

    companion object {
        // user.getRecentTracks while the user is mid-listen, matching the real wire
        // shape: the first entry is the currently-playing track (`@attr nowplaying`)
        // and carries no `date`; `uts` values arrive as JSON strings, and entries
        // carry extra fields (`mbid`, `url`, `image`, `streamable`, `date.#text`).
        private val FIXTURE =
            """
            {
              "recenttracks": {
                "track": [
                  {
                    "name": "Song C",
                    "artist": { "mbid": "", "#text": "Artist" },
                    "album": { "mbid": "", "#text": "Album" },
                    "mbid": "",
                    "url": "https://www.last.fm/music/Artist/_/Song+C",
                    "image": [{ "size": "small", "#text": "" }],
                    "streamable": "0",
                    "@attr": { "nowplaying": "true" }
                  },
                  {
                    "name": "Song B",
                    "artist": { "mbid": "", "#text": "Artist" },
                    "album": { "mbid": "", "#text": "Album" },
                    "mbid": "",
                    "url": "https://www.last.fm/music/Artist/_/Song+B",
                    "image": [{ "size": "small", "#text": "" }],
                    "streamable": "0",
                    "date": { "uts": "1751980000", "#text": "08 Jul 2026, 13:06" }
                  },
                  {
                    "name": "Song A",
                    "artist": { "mbid": "", "#text": "Artist" },
                    "album": { "mbid": "", "#text": "Album" },
                    "mbid": "",
                    "url": "https://www.last.fm/music/Artist/_/Song+A",
                    "image": [{ "size": "small", "#text": "" }],
                    "streamable": "0",
                    "date": { "uts": "1751970000", "#text": "08 Jul 2026, 10:20" }
                  }
                ]
              }
            }
            """.trimIndent()
    }
}
