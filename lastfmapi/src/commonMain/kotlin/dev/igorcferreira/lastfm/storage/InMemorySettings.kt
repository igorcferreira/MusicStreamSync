package dev.igorcferreira.lastfm.storage

import com.russhwolf.settings.Settings

/**
 * Trivial map-backed [Settings], used to keep an imported [dev.igorcferreira.lastfm.model.Session]
 * scoped to a single [dev.igorcferreira.lastfm.LastFMClient] instance instead of the
 * process-global no-arg [Settings]. Not persisted and not thread-safe beyond the
 * import-once/read-many pattern the client uses.
 */
internal class InMemorySettings : Settings {
    private val storage = mutableMapOf<String, Any>()

    override val keys: Set<String>
        get() = storage.keys.toSet()
    override val size: Int
        get() = storage.size

    override fun clear() = storage.clear()

    override fun remove(key: String) {
        storage.remove(key)
    }

    override fun hasKey(key: String): Boolean = storage.containsKey(key)

    override fun putInt(
        key: String,
        value: Int,
    ) {
        storage[key] = value
    }

    override fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = getIntOrNull(key) ?: defaultValue

    override fun getIntOrNull(key: String): Int? = storage[key] as? Int

    override fun putLong(
        key: String,
        value: Long,
    ) {
        storage[key] = value
    }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = getLongOrNull(key) ?: defaultValue

    override fun getLongOrNull(key: String): Long? = storage[key] as? Long

    override fun putString(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = getStringOrNull(key) ?: defaultValue

    override fun getStringOrNull(key: String): String? = storage[key] as? String

    override fun putFloat(
        key: String,
        value: Float,
    ) {
        storage[key] = value
    }

    override fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float = getFloatOrNull(key) ?: defaultValue

    override fun getFloatOrNull(key: String): Float? = storage[key] as? Float

    override fun putDouble(
        key: String,
        value: Double,
    ) {
        storage[key] = value
    }

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = getDoubleOrNull(key) ?: defaultValue

    override fun getDoubleOrNull(key: String): Double? = storage[key] as? Double

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        storage[key] = value
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = getBooleanOrNull(key) ?: defaultValue

    override fun getBooleanOrNull(key: String): Boolean? = storage[key] as? Boolean
}
