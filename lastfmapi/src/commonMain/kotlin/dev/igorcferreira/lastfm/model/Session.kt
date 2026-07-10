package dev.igorcferreira.lastfm.model

import kotlinx.serialization.Serializable

/**
 * An authenticated Last.fm session, as returned by `auth.getMobileSession`.
 *
 * A session can be exported from the device that authenticated
 * ([dev.igorcferreira.lastfm.LastFMClient.currentSession]) and imported by another
 * process (`LastFMClient(apiKey, secret, session)`), so this type doubles as the
 * transport payload. Wire shape (JSON):
 *
 * ```json
 * { "name": "<username>", "key": "<session key>", "subscriber": 0 }
 * ```
 *
 * `name` and `key` are required; `subscriber` is optional and defaults to `0`.
 */
@Serializable
data class Session(
    val name: String,
    val key: String,
    val subscriber: Int = 0,
)
