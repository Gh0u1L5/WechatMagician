package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedHelpers.getLongField
import kotlin.concurrent.timer

// MessageCache records the recent messages received from friends.
object MessageCache {

    // msgTable maps msgId to message object.
    private var msgTable: MutableMap<Long, Any> = mutableMapOf()

    // Clean cache for every 30 minutes
    init {
        timer(period = 30 * 60 * 1000, action = {
            clear()
        })
    }

    @Synchronized operator fun get(msgId: Long): Any? {
        return msgTable[msgId]
    }

    @Synchronized operator fun set(msgId: Long, msg: Any) {
        msgTable[msgId] = msg
    }

    @Synchronized operator fun contains(msgId: Long): Boolean {
        return msgId in msgTable
    }

    // clear removes all the messages received more than 2 minutes ago.
    // NOTE: One cannot recall the removed messages because Wechat have
    //       time limit on recalling messages.
    @Synchronized private fun clear() {
        val now = System.currentTimeMillis()
        msgTable = msgTable.filter {
            now - getLongField(it.value, "field_createTime") < 120000
        }.toMutableMap()
    }
}
