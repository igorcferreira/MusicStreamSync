package dev.igorcferreira.musicstreamsync.network.model

class HTTPException(
    val code: Int,
    message: String,
) : Exception(message)
