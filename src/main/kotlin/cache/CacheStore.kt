package cache

/**
 * Represents a local or remote cache location
 */
interface CacheStore {

    fun set(key: String, value: ByteArray): Boolean

    fun setWithExpiry(key: String, value: ByteArray, seconds: Long): Boolean

    fun get(key: String): ByteArray?

    fun del(key: String): Boolean

    fun exists(key: String): Boolean

}