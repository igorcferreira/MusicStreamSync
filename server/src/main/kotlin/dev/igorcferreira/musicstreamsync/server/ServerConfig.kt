package dev.igorcferreira.musicstreamsync.server

/**
 * Server configuration, sourced from environment variables:
 *
 * - `PORT` — HTTP port (default [DEFAULT_PORT]).
 * - `MONGODB_URI` — MongoDB connection string (default [DEFAULT_MONGODB_URI]).
 * - `SYNC_SHARED_SECRET` — bearer secret the apps use to call the token API
 *   (TASK_4 onwards). **No default**: startup fails fast when unset.
 * - `SYNC_INTERVAL_MINUTES` — sync loop interval (default
 *   [DEFAULT_SYNC_INTERVAL_MINUTES]; consumed by TASK_6).
 */
data class ServerConfig(
    val port: Int,
    val mongodbUri: String,
    val syncSharedSecret: String,
    val syncIntervalMinutes: Int,
) {
    companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_MONGODB_URI = "mongodb://mongodb:27017/musicstreamsync"
        const val DEFAULT_SYNC_INTERVAL_MINUTES = 5
        const val DEFAULT_DATABASE_NAME = "musicstreamsync"

        fun fromEnvironment(env: (String) -> String? = System::getenv): ServerConfig {
            val secret =
                env("SYNC_SHARED_SECRET")?.takeIf { it.isNotBlank() }
                    ?: error(
                        "SYNC_SHARED_SECRET environment variable must be set: it is the shared " +
                            "secret the mobile apps use to authenticate against the token API.",
                    )
            return ServerConfig(
                port = env("PORT")?.toIntOrNull() ?: DEFAULT_PORT,
                mongodbUri = env("MONGODB_URI")?.takeIf { it.isNotBlank() } ?: DEFAULT_MONGODB_URI,
                syncSharedSecret = secret,
                syncIntervalMinutes = env("SYNC_INTERVAL_MINUTES")?.toIntOrNull() ?: DEFAULT_SYNC_INTERVAL_MINUTES,
            )
        }
    }
}
