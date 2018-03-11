package com.gh0u1l5.wechatmagician.spellbook

import java.util.concurrent.ConcurrentHashMap

/**
 * This is an singleton object that records the working status for all the features implemented by
 * Wechat Magician SpellBook. All the supported feature names are defined in a global singleton.
 * @see com.gh0u1l5.wechatmagician.spellbook.StatusFlags
 */
object WechatStatus {

    /**
     * The flags that indicates which feature has been initialized.
     */
    enum class StatusFlag {
        STATUS_FLAG_MSG_STORAGE,
        STATUS_FLAG_IMG_STORAGE,
        STATUS_FLAG_RESOURCES,
        STATUS_FLAG_DATABASE,
        STATUS_FLAG_XML_PARSER,
        STATUS_FLAG_URI_ROUTER,
        STATUS_FLAG_COMMAND
    }

    /**
     * A [ConcurrentHashMap] storing the working status of all the features.
     */
    private val status = ConcurrentHashMap<StatusFlag, Boolean>()

    /**
     * Returns the current working status as a [ConcurrentHashMap].
     */
    fun report(): ConcurrentHashMap<StatusFlag, Boolean> = status

    /**
     * Updates the working status for the specific feature.
     */
    fun toggle(key: StatusFlag, value: Boolean) { status[key] = value }
}