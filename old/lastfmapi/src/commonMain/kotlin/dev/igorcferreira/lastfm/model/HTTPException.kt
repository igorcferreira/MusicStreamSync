package dev.igorcferreira.lastfm.model

import io.ktor.http.*

class HTTPException(val code: HttpStatusCode, message: String) : Exception(message)
