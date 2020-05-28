package cache

import com.google.inject.Inject
import logging.logger
import serialize.Serializer

/**
 * Basic read-through cache implementation. Attempts to read a value from the
 * cache and if it's absent the result is recomputed and cached.
 */
abstract class ReadThroughCachedFetcher<K, V> @Inject protected constructor(
    cache: CacheStore,
    serializer: Serializer
) : CachedFetcher<K, V>(cache, serializer) {

    companion object {
        private val LOG by logger()
    }

    private val cacheFunc: (K, CacheableResult<V>?) -> Unit = { key: K, value: CacheableResult<V>? ->
        value?.let {
            cacheResult(key, value)
        }
    }

    override fun fetchResult(key: K): CacheableResult<V>? {
        var result = super.getValueFromCache(key)

        // Refetch result and cache it if it was not found
        if (result == null) {
            LOG.debug { "Cache miss for key $key. Contacting memoizer" }
            memo.compute(key, cacheFunc)?.let {
                result = it
            }
        } else {
            LOG.debug { "Cache hit for key $key" }
        }

        return result
    }
}