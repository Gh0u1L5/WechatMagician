package com.gh0u1l5.wechatmagician.spellbook

import java.util.concurrent.ConcurrentHashMap

/**
 * This is an singleton object that records the working status for all the features implemented by
 * Wechat Magician SpellBook. All the supported feature names are defined in a global singleton.
 * @see com.gh0u1l5.wechatmagician.spellbook.Global
 */
object WechatStatus {
    /**
     * A [ConcurrentHashMap] storing the working status of all the features.
     */
    private val status = ConcurrentHashMap<String, Boolean>()

    /**
     * Returns the current working status as a [ConcurrentHashMap].
     */
    fun report(): ConcurrentHashMap<String, Boolean> = status

    /**
     * Updates the working status for the specific feature.
     */
    fun toggle(key: String, value: Boolean) { status[key] = value }
}