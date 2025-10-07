package dev.igorcferreira.lastfm.model.responses

import dev.igorcferreira.lastfm.model.Session
import kotlinx.serialization.Serializable

@Serializable
internal data class SessionResponse(
    val session: Session,
)
