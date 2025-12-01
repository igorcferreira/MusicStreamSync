package dev.igorcferreira.musicstreamsync.network.model

class HTTPException(
    code: Int,
    message: String
) : Exception(message)