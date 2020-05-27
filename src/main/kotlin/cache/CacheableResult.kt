package cache

import java.time.LocalDateTime


/**
 * Used by clients to return fetched results.
 * Sets default values so serialization library has default constructor to work with.
 *
 * TODO: Remove no arg constructor if/when kryo fixes this
 * THE CLASS MUST HAVE A NO ARGS CONSTRUCTOR OR KRYO WONT BE ABLE TO DESERIALIZE!!!
 *
 * @param <V> The type of the result being returned.
 */
data class CacheableResult<V> constructor(
        val result: V? = null,
        val ttl: Int = 0,
        val lastUpdated: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        private const val defaultCacheTtl = 900

        /**
         * @param result The result of the query.
         * @param cacheTTL The ttl in seconds.
         */
        fun <V> of(result: V, cacheTTL: Int = defaultCacheTtl): CacheableResult<V> {
            return CacheableResult(result, cacheTTL)
        }
    }
}