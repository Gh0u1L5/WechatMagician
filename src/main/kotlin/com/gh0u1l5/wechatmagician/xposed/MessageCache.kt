package com.gh0u1l5.wechatmagician.xposed

import kotlin.concurrent.timer

object MessageCache {
    data class WechatMessage(val type: Int, val talker: String, val content: String?, val imgPath: String?)

    // timeTable maps insert time to msgId
    private var timeTable: Map<Long, Long> = mapOf()
    // msgTable maps msgId to message object
    private var msgTable: Map<Long, WechatMessage> = mapOf()

    init {
        timer(period = 240000, action = {
            MessageCache.clear()
        })
    }

    @Synchronized
    operator fun get(msgId: Long): WechatMessage? {
        return msgTable[msgId]
    }

    @Synchronized
    operator fun set(msgId: Long, msg: WechatMessage) {
        msgTable += Pair(msgId, msg)
        timeTable += Pair(System.currentTimeMillis(), msgId)
    }

    @Synchronized
    operator fun contains(msgId: Long): Boolean {
        return msgId in msgTable
    }

    @Synchronized
    fun clear() {
        val now = System.currentTimeMillis()
        timeTable = timeTable.filter { now - it.key < 120000 }
        msgTable = msgTable.filter { it.key in timeTable }
    }
}
