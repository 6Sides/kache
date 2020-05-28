package cache

import com.google.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import logging.logger
import serialize.Serializer


/**
 * Implementation of the refresh-ahead caching strategy.
 *
 * refreshAheadFactor should be in (0, 1). The larger the value, the longer the value waits to be refreshed.
 */
abstract class RefreshAheadCachedFetcher<K, V> @Inject protected constructor(
    private val cache: CacheStore,
    serializer: Serializer,
    private val refreshAheadFactor: Float = 0.5f
): CachedFetcher<K, V>(cache, serializer) {

    companion object {
        private val LOG by logger()
    }

    init {
        require (refreshAheadFactor in 0f..1f) { "RefreshAheadFactor must be between 0 and 1 inclusive" }
    }

    override fun fetchResult(key: K): CacheableResult<V>? {
        val needsRefresh: Boolean = !cache.exists(generateHash(key).toString() + "rac")

        if (needsRefresh) {
            // Refetch result and cache it asynchronously
            GlobalScope.launch {
                val result = calculateResult(key)
                cacheResult(key, result)
                cache.setWithExpiry(generateHash(key).toString() + "rac", byteArrayOf(), (result.ttl * refreshAheadFactor).toLong())
            }
        }

        var result = super.getValueFromCache(key)

        // Refetch result and cache it
        if (result == null) {
            LOG.debug { "Cache miss for key $key. Recomputing value..." }
            result = calculateResult(key)
            cacheResult(key, result)
        } else {
            LOG.debug { "Cache hit for key $key" }
        }

        return result
    }
}