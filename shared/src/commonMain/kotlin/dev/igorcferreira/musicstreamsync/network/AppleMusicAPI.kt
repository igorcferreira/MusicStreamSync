package dev.igorcferreira.musicstreamsync.network

import dev.igorcferreira.musicstreamsync.domain.TokenSigner
import dev.igorcferreira.musicstreamsync.domain.UserTokenProvider
import dev.igorcferreira.musicstreamsync.domain.signWith
import dev.igorcferreira.musicstreamsync.model.DeveloperToken
import dev.igorcferreira.musicstreamsync.model.MusicEntry
import dev.igorcferreira.musicstreamsync.model.PlaylistEntry
import dev.igorcferreira.musicstreamsync.network.model.HTTPException
import dev.igorcferreira.musicstreamsync.network.model.apple_music.Response
import dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist.LibraryPlaylists
import dev.igorcferreira.musicstreamsync.network.model.apple_music.playlist.LibrarySongs
import dev.igorcferreira.musicstreamsync.network.model.apple_music.song.SongAttributes
import dev.igorcferreira.musicstreamsync.network.model.apple_music.song.SongResource
import io.ktor.http.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.milliseconds

@HiddenFromObjC
internal open class AppleMusicAPI(
    private val host: String = "https://api.music.apple.com/v1",
    internal val tokenSigner: TokenSigner,
    internal val userTokenProvider: UserTokenProvider,
    internal val developerToken: DeveloperToken,
    private val urlSession: URLSession
) {
    @HiddenFromObjC
    @Throws(HTTPException::class, CancellationException::class)
    internal open suspend fun getRecentlyPlayed(): List<MusicEntry> = get<Response<SongResource>>(
        "/me/recent/played/tracks?extend=artistUrl&l=en"
    ).map()

    @HiddenFromObjC
    @Throws(HTTPException::class, CancellationException::class)
    internal open suspend fun getUserPlaylists() = get<Response<LibraryPlaylists>>(
        "/me/library/playlists?extend=dateAdded&l=en"
    ).data.filter { it.attributes.playParams.globalId != null }.map { it.map() }

    @HiddenFromObjC
    @Throws(HTTPException::class, CancellationException::class)
    internal open suspend fun getPlaylist(id: String) = get<Response<LibraryPlaylists>>(
        "/me/library/playlists/$id?extend=dateAdded&include=tracks&l=en"
    ).data.map { it.map() }.firstOrNull()

    private suspend inline fun <reified T> get(
        path: String
    ): T = urlSession.perform("$host$path", HttpMethod.Get, buildHeaders())

    private suspend fun buildHeaders(): Map<String, String> {
        val developerToken = buildDeveloperToken()
        val userToken = userTokenProvider.getUserToken(developerToken)
        return mapOf(
            HttpHeaders.Authorization to "Bearer $developerToken",
            "Music-User-Token" to userToken
        )
    }

    private fun LibraryPlaylists.map() = PlaylistEntry(
        id = id,
        entryId = attributes.playParams.globalId
            ?: attributes.playParams.id,
        name = attributes.name,
        canEdit = attributes.canEdit,
        hasCatalog = attributes.hasCatalog,
        isPublic = attributes.isPublic,
        dateAdded = attributes.dateAdded,
        lastModifiedDate = attributes.lastModifiedDate,
        entryDescription = attributes.description?.standard,
        artworkUrl = attributes.artwork?.mappedUrl,
        songs = relationships?.data?.map { it.map() }
    )

    private fun LibrarySongs.map() = attributes.map(
        id = id
    )

    private fun Response<SongResource>.map() = data
        .mapIndexed { index, songResource -> songResource.map("${index}_") }

    private fun SongAttributes.map(
        idPrefix: String = "",
        id: String,
    ) = MusicEntry(
        id = "$idPrefix$id",
        entryId = id,
        title = name,
        artist = artistName,
        artworkUrl = artwork.url
            .replace("{w}", artwork.width.toString())
            .replace("{h}", artwork.height.toString()),
        duration = durationInMillis.milliseconds.inWholeSeconds,
        album = albumName
    )

    private fun SongResource.map(idPrefix: String = "") = attributes.map(
        idPrefix = idPrefix,
        id = id
    )

    private suspend fun buildDeveloperToken(): String = developerToken
        .signWith(tokenSigner)
}
