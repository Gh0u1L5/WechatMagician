package com.gh0u1l5.wechatmagician.backend.foundation.base

import com.gh0u1l5.wechatmagician.Global.tryWithThread
import java.util.concurrent.ConcurrentHashMap

abstract class EventCenter {

    private val registries: MutableMap<String, List<Any>> = ConcurrentHashMap()

    private fun Any.hasEvent(event: String) =
            this.javaClass.declaredMethods.any { method -> method.name == event }

    fun register(event: String, observer: Any) {
        if (observer.hasEvent(event)) {
            val added = registries[event] ?: emptyList()
            registries[event] = added + observer
        }
    }

    fun register(`interface`: Class<*>, observer: Any) {
        `interface`.methods.forEach { method ->
            register(method.name, observer)
        }
    }

    fun notify(event: String, action: (Any) -> Unit) {
        registries[event]?.forEach(action)
    }

    fun notifyParallel(event: String, action: (Any) -> Unit) {
        registries[event]?.map { observer ->
            tryWithThread { action(observer) }
        }?.forEach(Thread::join)
    }

    fun <T: Any>notifyForResult(event: String, action: (Any) -> T?): List<T> {
        return registries[event]?.mapNotNull(action) ?: emptyList()
    }
}