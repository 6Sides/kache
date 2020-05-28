package cache

import kotlinx.coroutines.*
import logging.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Prevents many concurrent calls from independently fetching the same data.
 *
 * If computation is not running, begin and wrap result in a Future for other callers.
 * If computation is running, caller will wait for computation to finish and collect result.
 */
class MemoizedComputer<K, V>(private val computeFunction: (key: K) -> V) {

    companion object {
        private val LOG by logger()
    }

    private val cache: ConcurrentMap<K, Deferred<V>> = ConcurrentHashMap()

    fun compute(key: K, onComplete: (key: K, value: V) -> Unit): V = runBlocking {
        var startedComputation = false
        var future = cache[key]

        // If computation not started
        if (future == null) {
            val futureTask = async(start = CoroutineStart.LAZY) {
                computeFunction(key)
            }

            future = synchronized(this) {
                cache.putIfAbsent(key, futureTask)
            }

            // Start computation if it hasn't been started in the meantime
            if (future == null) {
                future = futureTask
                future.start()
                LOG.debug { "Recomputing value" }
                startedComputation = true
            } else {
                LOG.debug { "Debounced... Waiting for result" }
            }
        }

        // Get result if ready, otherwise block and wait
        future.await().also {
            // If this coroutine started the computation
            // remove it from the cache upon completion
            if (startedComputation) {
                cache -= key
                launch {
                    onComplete(key, it)
                }
            }
        }
    }
}