package serialize

import kotlin.math.abs

interface ClassIdProvider {

    fun generateId(clazz: Class<*>): Int

}

class DefaultClassIdProvider: ClassIdProvider {

    override fun generateId(clazz: Class<*>): Int {
        return abs(clazz.name.hashCode()) + 12
    }

}