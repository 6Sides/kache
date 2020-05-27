package cache

import cache.CacheStore
import cache.ConcurrentExpiryHashMap


class InMemoryCache: CacheStore {

    private val cache = ConcurrentExpiryHashMap<String, ByteArray>()

    override fun set(key: String, value: ByteArray): Boolean {
        cache.put(key, value)
        return true
    }

    override fun setWithExpiry(key: String, value: ByteArray, seconds: Long): Boolean {
        cache.put(key, value, seconds)
        return true
    }

    override fun get(key: String): ByteArray? {
        return cache[key]
    }

    override fun del(key: String): Boolean {
        cache.remove(key)
        return true
    }

    override fun exists(key: String): Boolean {
        return cache.containsKey(key)
    }
}