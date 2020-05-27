package serialize

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.util.Pool
import logging.logger
import java.util.*
import kotlin.math.abs

/**
 * Based on https://github.com/EsotericSoftware/kryo#pooling
 */
object KryoPool {

    private val LOG by logger()

    // Stores classes Kryo needs to register on creation
    private val registeredClasses: MutableMap<Class<*>, Serializer<*>?> = HashMap()

    val pool: Pool<Kryo> = object : Pool<Kryo>(true, false, 4) {
        override fun create(): Kryo {
            val kryo = Kryo()
            synchronized(registeredClasses) {
                registeredClasses.forEach { (clazz: Class<*>, serializer: Serializer<*>?) ->
                    LOG.debug { "Registering class ${clazz.simpleName} with id ${getClassId(clazz)}" }
                    if (serializer == null) {
                        kryo.register(clazz, getClassId(clazz))
                    } else {
                        kryo.register(clazz, serializer, getClassId(clazz))
                    }
                }
            }
            return kryo
        }
    }

    private fun getClassId(clazz: Class<*>): Int {
        return abs(clazz.name.hashCode()) + 12
    }

    /**
     * Registers a class with an id based on its hashcode. 12 is added to ensure
     * there is no version clash with Kryo's default registered classes.
     */
    fun <T> registerClass(clazz: Class<T>) {
        synchronized(registeredClasses) { registeredClasses.put(clazz, null) }
    }

    fun <T> registerClass(clazz: Class<T>, serializer: Serializer<T>?) {
        synchronized(registeredClasses) { registeredClasses.put(clazz, serializer) }
    }
}