package cache

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Prevents many concurrent calls from independently fetching the same data.
 *
 * If computation is not running, begin and wrap result in a Future for other callers.
 * If computation is running, caller will wait for computation to finish and collect result.
 */
class MemoizedComputer<K, V>(private val computeFunction: (key: K) -> V) {

    private val cache: ConcurrentMap<K, Deferred<V>> = ConcurrentHashMap()

    fun compute(key: K): V {
        var startedComputation = false
        var future = cache[key]

        // If computation not started
        if (future == null) {
            val futureTask = GlobalScope.async {
                computeFunction(key)
            }

            future = cache.putIfAbsent(key, futureTask)

            // Start computation if it hasn't been started in the meantime
            if (future == null) {
                println("Computing")
                future = futureTask
                startedComputation = true
            }
        }

        // Get result if ready, otherwise block and wait
        return runBlocking {
            future.await().also {
                // If this coroutine started the computation
                // remove it from the cache upon completion
                if (startedComputation) {
                    cache -= key
                }
            }
        }
    }
}