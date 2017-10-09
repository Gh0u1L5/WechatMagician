package com.gh0u1l5.wechatmagician.storage

// HookStatus records the status of all the hooks.
object HookStatus {
    private val status: MutableMap<String, Boolean> = mutableMapOf()

    @Synchronized operator fun get(func: String?): Boolean {
        return status[func] ?: false
    }

    @Synchronized operator fun plusAssign(func: String) {
        status[func] = true
    }

    @Synchronized operator fun minusAssign(func: String) {
        status[func] = false
    }
}