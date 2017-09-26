package com.gh0u1l5.wechatmagician.xposed

import android.content.ContentValues
import kotlin.concurrent.timer

// MessageCache records the recent messages received from friends.
object MessageCache {

    // WechatMessage stores important properties of a single message.
    class WechatMessage(values: ContentValues) {
        val type       = values["type"] as Int
        val talker     = values["talker"] as String
        val imgPath    = values["imgPath"] as String?
        val createTime = values["createTime"] as Long
    }

    // nextMsgId records the next local msgId that should be used for insertion.
    var nextMsgId: Long = -1L
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
            now - it.value.createTime < 120000
        }
    }
}
