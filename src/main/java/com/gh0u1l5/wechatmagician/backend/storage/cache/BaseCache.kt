package com.gh0u1l5.wechatmagician.backend.storage.cache

import java.util.concurrent.ConcurrentHashMap

open class BaseCache<K, V> {
    protected val data: MutableMap<K, V> = ConcurrentHashMap()

    open operator fun contains(key: K): Boolean = key in data

    open operator fun get(key: K): V? = data[key]

    open operator fun set(key: K, value: V) { data[key] = value }
}