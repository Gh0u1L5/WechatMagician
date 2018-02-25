package com.gh0u1l5.wechatmagician.backend.storage.list

import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class BaseList<T> {
    protected val data: MutableSet<T> = Collections.newSetFromMap(ConcurrentHashMap())

    fun toList() = data.toList()

    fun forEach(action: (T) -> Unit) = data.forEach(action)

    open operator fun contains(value: Any?): Boolean = value in data

    open operator fun plusAssign(value: T) { data += value }

    open operator fun plusAssign(values: Iterable<T>) { data += values }

    open operator fun minusAssign(value: T) { data -= value }

    open operator fun minusAssign(values: Iterable<T>) { data -= values }
}