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
        // user.getRecentTracks while the user is mid-listen: the first entry is the
        // currently-playing track (`@attr nowplaying`) and carries no `date`.
        private val FIXTURE =
            """
            {
              "recenttracks": {
                "track": [
                  {
                    "name": "Song C",
                    "artist": { "#text": "Artist" },
                    "album": { "#text": "Album" },
                    "@attr": { "nowplaying": "true" }
                  },
                  {
                    "name": "Song B",
                    "artist": { "#text": "Artist" },
                    "album": { "#text": "Album" },
                    "date": { "uts": 1751980000 }
                  },
                  {
                    "name": "Song A",
                    "artist": { "#text": "Artist" },
                    "album": { "#text": "Album" },
                    "date": { "uts": 1751970000 }
                  }
                ]
              }
            }
            """.trimIndent()
    }
}
