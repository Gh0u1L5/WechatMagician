package com.gh0u1l5.wechatmagician.spellbook.hookers.base

import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryAsynchronously
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import java.util.concurrent.ConcurrentHashMap

abstract class EventCenter {

    abstract val interfaces: List<Class<*>>

    private val registries: MutableMap<String, List<Any>> = ConcurrentHashMap()

    private fun Any.hasEvent(event: String) =
            this::class.java.declaredMethods.any { method -> method.name == event }

    private fun register(event: String, observer: Any) {
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
        if (event == "") {
            throw IllegalArgumentException("event cannot be empty!")
        }
        registries[event]?.forEach {
            tryVerbosely { action(it) }
        }
    }

    fun notifyParallel(event: String, action: (Any) -> Unit) {
        if (event == "") {
            throw IllegalArgumentException("event cannot be empty!")
        }
        registries[event]?.map { observer ->
            tryAsynchronously { action(observer) }
        }?.forEach(Thread::join)
    }

    fun <T: Any>notifyForResult(event: String, action: (Any) -> T?): List<T> {
        if (event == "") {
            throw IllegalArgumentException("event cannot be empty!")
        }
        return registries[event]?.mapNotNull {
            tryVerbosely { action(it) }
        } ?: emptyList()
    }
}