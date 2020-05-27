package cache

import com.google.inject.AbstractModule
import serialize.KryoSerializer
import serialize.Serializer

class SimpleCacheModule: AbstractModule() {

    override fun configure() {
        bind(CacheStore::class.java).to(InMemoryCache::class.java)
        bind(Serializer::class.java).to(KryoSerializer::class.java)
    }
}