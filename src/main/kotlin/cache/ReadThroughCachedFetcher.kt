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

    override fun fetchResult(input: K): CacheableResult<V>? {
        var result = super.getValueFromCache(input)

        // Refetch result and cache it if it was not found
        if (result == null) {
            LOG.debug { "Cache miss for key $input. Recomputing value..." }
            result = calculateResult(input)
            cacheResult(input, result)
        } else {
            LOG.debug { "Cache hit for key $input" }
        }

        return result
    }
}