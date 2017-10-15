package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedHelpers.getLongField
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.timer
import kotlin.concurrent.write

// MessageCache records the recent messages received from friends.
object MessageCache {

    // msgTable maps msgId to message object.
    private val msgTableLock = ReentrantReadWriteLock()
    private var msgTable: MutableMap<Long, Any> = mutableMapOf()

    // Clean cache for every 10 minutes
    init {
        timer(period = 10 * 60 * 1000, action = {
            clear()
        })
    }

    operator fun get(msgId: Long): Any? {
        msgTableLock.read {
            return msgTable[msgId]
        }
    }

    operator fun set(msgId: Long, msg: Any) {
        msgTableLock.write {
            msgTable[msgId] = msg
        }
    }

    operator fun contains(msgId: Long): Boolean {
        msgTableLock.read {
            return msgId in msgTable
        }
    }

    // clear removes all the messages received more than 2 minutes ago.
    // NOTE: One cannot recall the removed messages because Wechat have
    //       time limit on recalling messages.
    private fun clear() {
        msgTableLock.write {
            val now = System.currentTimeMillis()
            msgTable = msgTable.filter {
                now - getLongField(it.value, "field_createTime") < 120000
            }.toMutableMap()
        }
    }
}
