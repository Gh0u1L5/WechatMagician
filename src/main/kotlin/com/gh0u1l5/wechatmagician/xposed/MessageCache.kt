package com.gh0u1l5.wechatmagician.xposed

import kotlin.concurrent.timer

// MessageCache records the recent messages received from friends.
object MessageCache {

    // WechatMessage stores important properties of a single message.
    data class WechatMessage (
            val type: Int,
            val talker: String,
            val content: String?,
            val imgPath: String?
    )

    // timeTable maps insert time to msgId.
    private var timeTable: Map<Long, Long> = mapOf()
    // msgTable maps msgId to message object.
    private var msgTable: Map<Long, WechatMessage> = mapOf()

    // Clean cache for every 30 minutes
    init {
        timer(period = 30 * 60 * 1000, action = {
            MessageCache.clear()
        })
    }

    @Synchronized operator fun get(msgId: Long): WechatMessage? {
        return msgTable[msgId]
    }

    @Synchronized operator fun set(msgId: Long, msg: WechatMessage) {
        msgTable += Pair(msgId, msg)
        timeTable += Pair(System.currentTimeMillis(), msgId)
    }

    @Synchronized operator fun contains(msgId: Long): Boolean {
        return msgId in msgTable
    }

    // clear removes all the messages received more than 2 minutes ago.
    // NOTE: One cannot recall these messages because Wechat have time
    //       limit on recalling messages.
    @Synchronized private fun clear() {
        val now = System.currentTimeMillis()
        timeTable = timeTable.filter { now - it.key < 120000 }
        msgTable = msgTable.filter { it.key in timeTable }
    }
}
