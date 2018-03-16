package com.gh0u1l5.wechatmagician.backend.storage.cache

import de.robv.android.xposed.XposedHelpers.getLongField
import kotlin.concurrent.timer

// MessageCache records the recent messages received from friends.
object MessageCache : BaseCache<Long, Any>() {
    init {
        // Clean cache for every 10 minutes
        timer(period = 10 * 60 * 1000) { clear() }
    }

    // clear removes all the messages received more than 2 minutes ago.
    // NOTE: One cannot recall the removed messages because Wechat have
    //       time limit on recalling messages.
    private fun clear() {
        data.forEach { entry ->
            val createTime = getLongField(entry.value, "field_createTime")
            if (System.currentTimeMillis() - createTime > 120000) {
                data.remove(entry.key)
            }
        }
    }
}
