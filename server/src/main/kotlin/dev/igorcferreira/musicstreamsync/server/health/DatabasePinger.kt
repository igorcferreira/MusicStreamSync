package dev.igorcferreira.musicstreamsync.server.health

/** Live reachability check for the persistence layer, injected into the routes. */
fun interface DatabasePinger {
    suspend fun ping(): Boolean
}
