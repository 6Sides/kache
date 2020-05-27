package cache

import com.google.inject.Inject
import serialize.Serializer


object CacheBuilder {
    @Inject
    private lateinit var cache: CacheStore

    @Inject
    private lateinit var serializer: Serializer

    fun <K, V> build(strategy: Strategy, block: (key: K) -> CacheableResult<V>): CachedFetcher<K, V> {
        return when (strategy) {
            Strategy.READ_THROUGH -> {
                object: ReadThroughCachedFetcher<K, V>(cache, serializer) {

                    override fun calculateResult(key: K): CacheableResult<V> {
                        return block.invoke(key)
                    }
                }
            }

            Strategy.REFRESH_AHEAD -> {
                object: RefreshAheadCachedFetcher<K, V>(cache, serializer) {

                    override fun calculateResult(key: K): CacheableResult<V> {
                        return block.invoke(key)
                    }
                }
            }
        }
    }
}