package com.gh0u1l5.wechatmagician.spellbook

/**
 * This is an singleton object that records the working status for all the features implemented by
 * Wechat Magician SpellBook. All the supported feature names are defined in [StatusFlag].
 */
object WechatStatus {

    /**
     * The flags that indicates which feature has been initialized.
     */
    enum class StatusFlag {
        STATUS_FLAG_ACTIVITIES,
        STATUS_FLAG_ADAPTERS,
        STATUS_FLAG_BASE_ADAPTER,
        STATUS_FLAG_COMMAND,
        STATUS_FLAG_CONTACT_POPUP,
        STATUS_FLAG_CONVERSATION_POPUP,
        STATUS_FLAG_DATABASE,
        STATUS_FLAG_FILESYSTEM,
        STATUS_FLAG_IMG_STORAGE,
        STATUS_FLAG_MSG_STORAGE,
        STATUS_FLAG_NOTIFICATIONS,
        STATUS_FLAG_RESOURCES,
        STATUS_FLAG_URI_ROUTER,
        STATUS_FLAG_XML_PARSER
    }

    /**
     * A [IntArray] storing the working status of all the features.
     */
    private var status: IntArray = intArrayOf()

    /**
     * Returns the current working status as a [IntArray].
     */
    @Synchronized fun report(): IntArray = status

    /**
     * Updates the working status for the specific feature.
     */
    @Synchronized fun toggle(flag: StatusFlag) { status += flag.ordinal }
}