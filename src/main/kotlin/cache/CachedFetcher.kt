package cache

import com.google.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import logging.logger
import serialize.Serializer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Handles caching of results. Should be used for computationally expensive queries.
 *
 * This class automatically handles object serialization/deserialization at the expense
 * of slightly larger serialization data.
 *
 * @param <K> The data type required to make the query (The `key`)
 * @param <V> The result type (The `value`)
 */
abstract class CachedFetcher<K, V> @Inject protected constructor(
    private val cache: CacheStore,
    private val serializer: Serializer
) {

    companion object {
        private val LOG by logger()
    }

    private val collisionCache: ConcurrentMap<K, CacheableResult<V>> = ConcurrentHashMap()
    val memo: MemoizedComputer<K, CacheableResult<V>?> = MemoizedComputer { this.calculateResult(it) }

    protected abstract fun calculateResult(key: K): CacheableResult<V>

    /**
     * Fetches and returns the result based on the input
     *
     * @return the result of the computation, or null if it failed
     */
    protected abstract fun fetchResult(key: K): CacheableResult<V>?

    /**
     * Fetches data based on the specified key.
     *
     * @param key The data being used to make the query
     * @return The value associated with the specified key (can be null)
     */
    operator fun get(key: K): CacheableResult<V>? {
        return fetchResult(key)
    }

    /**
     * Attempts to get a value from the cache based on the input.
     *
     * @return A CacheableResult containing the computed result, or null if none is found.
     */
    protected fun getValueFromCache(key: K): CacheableResult<V>? {
        val objectHash = generateHash(key).toString()
        val blob = cache.get(objectHash)

        // Key exists, attempt to deserialize and return it's associated value.
        return blob?.let {
            return serializer.readObject(blob) as? CacheableResult<V>
        } ?: collisionCache[key]
    }

    /**
     * Serializes and caches a fetched result. Runs asynchronously.
     *
     * @param key The key to store the result at.
     * @param result The object to serialize and store.
     */
    protected fun cacheResult(key: K, result: CacheableResult<V>) {
        LOG.debug { "Caching $key -> ${result.result} (Expires in ${result.ttl} seconds)" }

        val res = collisionCache.putIfAbsent(key, result)

        // If cache doesn't contain value begin process of caching it
        if (res == null) {
            GlobalScope.launch {
                val resultBytes = serializer.writeObject(result)
                cache.setWithExpiry(generateHash(key).toString(), resultBytes, result.ttl.toLong())
                collisionCache -= key
            }
        }
    }

    /**
     * Generates the key to be used in the key,value pair inserted in the cache.
     * @param key The key to hash.
     *
     * @return A unique hash of the key
     */
    protected open fun generateHash(key: K): Int {
        return key.hashCode()
    }
}