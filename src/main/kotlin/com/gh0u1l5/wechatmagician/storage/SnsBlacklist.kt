package com.gh0u1l5.wechatmagician.storage

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object SnsBlacklist {
    private val snsBlacklistLock = ReentrantReadWriteLock()
    private val snsBlacklist: MutableMap<String, Boolean> = mutableMapOf()

    operator fun contains(id: Any?): Boolean {
        if (id == null) {
            return false
        }
        if (id !is String) {
            return false
        }
        snsBlacklistLock.read {
            return id in snsBlacklist
        }
    }

    operator fun plusAssign(id: String?) {
        if (id == null) {
            return
        }
        snsBlacklistLock.write {
            snsBlacklist[id.padStart(20, '0')] = true
        }
    }
}