package com.gh0u1l5.wechatmagician.storage.database

import android.content.ContentValues
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import java.util.concurrent.ConcurrentHashMap

object MainDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var mainDB: Any? = null

    // nameCache maps nicknames to corresponding usernames
    private val nameCache: MutableMap<String, String> = ConcurrentHashMap()

    fun cleanUnreadCount() {
        val database = mainDB ?: return
        val clean = ContentValues().apply { put("unReadCount", 0) }
        callMethod(database, "update", "rconversation", clean, null, null)
    }

    fun getUsernameFromNickname(nickname: String): String? {
        if (nickname in nameCache) {
            return nameCache[nickname]
        }
        if (nickname == "") {
            return null
        }

        val database = mainDB ?: return null
        var cursor: Any? = null
        try {
            cursor = callMethod(database, "query",
                    "rcontact", arrayOf("username"), "nickname=?", arrayOf(nickname),
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount")
            if (count != 1) {
                return null
            }
            callMethod(cursor, "moveToFirst")
            nameCache[nickname] = callMethod(cursor, "getString", 0) as String
            return nameCache[nickname]
        } catch (e: Throwable) {
            log(e); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }
}