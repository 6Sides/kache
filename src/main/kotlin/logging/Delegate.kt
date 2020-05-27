package logging

import mu.KLogger
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject


// Obtain a logger for the given class
fun <R : Any> R.logger(): Lazy<KLogger> {
    return lazy {
        KotlinLogging.logger(unwrapCompanionClass(this::class).qualifiedName ?: this.javaClass.name)
    }
}


// unwrap companion class to enclosing class given a Kotlin Class
private fun <T: Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}

// unwrap companion class to enclosing class given a Java Class
private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}
