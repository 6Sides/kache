package cache

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class ConcurrentExpiryHashMap<K, V>: Map<K, V> {

    private val map = ConcurrentHashMap<K, V>()
    private val expirationTimes = ConcurrentHashMap<K, Instant>()


    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = map.entries
    override val keys: MutableSet<K> = map.keys
    override val size: Int = map.size
    override val values: MutableCollection<V> = map.values

    override fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return map.containsValue(value)
    }

    override operator fun get(key: K): V? {
        return expirationTimes[key]?.let {
            if (it <= Instant.now()) {
                expirationTimes -= key
                map -= key
            }
            map[key]
        }
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun clear() {
        expirationTimes.clear()
        map.clear()
    }

    fun put(key: K, value: V, expirationTimeInSeconds: Long? = null): V? {
        expirationTimeInSeconds?.let {
            expirationTimes[key] = Instant.now().plusSeconds(expirationTimeInSeconds)
        }

        return map.put(key, value)
    }

    fun remove(key: K): V? {
        expirationTimes -= key
        return map.remove(key)
    }
}