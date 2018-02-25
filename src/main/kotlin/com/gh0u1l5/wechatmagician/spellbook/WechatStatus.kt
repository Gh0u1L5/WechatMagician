package com.gh0u1l5.wechatmagician.spellbook

import java.util.concurrent.ConcurrentHashMap

object WechatStatus {
    // status stores the working status of all the features.
    private val status = ConcurrentHashMap<String, Boolean>()

    // report returns the current status as a hash map.
    fun report(): ConcurrentHashMap<String, Boolean> = status

    // toggle updates the current status of the specific Wechat SpellBook features.
    fun toggle(key: String, value: Boolean) { status[key] = value }
}