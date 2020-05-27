package serialize

import cache.CacheableResult
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.google.common.io.ByteStreams
import logging.logger
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

class KryoSerializer: Serializer {

    companion object {
        private val LOG by logger()

        private val kryoPool = KryoPool.pool

        init {
            KryoPool.registerClass(CacheableResult::class.java)
            KryoPool.registerClass(LocalDateTime::class.java)
        }
    }

    override fun readObject(data: ByteArray): Any? {
        return try {
            val kryo = kryoPool.obtain()
            val result = kryo?.readClassAndObject(Input(ByteArrayInputStream(data)))

            kryoPool.free(kryo)
            result
        } catch (ex: Exception) {
            LOG.error { ex.message }
            null
        }
    }

    override fun writeObject(data: Any?): ByteArray {
        val kryo = kryoPool.obtain()

        Output(1024, -1).use { output ->
            kryo?.writeClassAndObject(output, data)
            kryoPool.free(kryo)

            return Input(output.buffer, 0, output.position()).use { input ->
                ByteStreams.toByteArray(input)
            }
        }
    }
}