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

    fun cacheNicknameUsernamePair(nickname: String?, username: String?) {
        if (nickname == null || username == null) {
            return
        }
        nameCache[nickname] = username
    }

    fun cleanUnreadCount() {
        val clean = ContentValues().apply { put("unReadCount", 0) }
        callMethod(mainDB, "update", "rconversation", clean, null, null)
    }

    fun getUsernameFromNickname(nickname: String): String? {
        if (nickname in nameCache) {
            return nameCache[nickname]
        }

        if (nickname == "") return null
        val db = mainDB ?: return null

        var cursor: Any? = null
        try {
            cursor = callMethod(db, "query",
                    "rcontact", arrayOf("username"), "nickname=?", arrayOf(nickname),
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount")
            if (count != 1) {
                return null
            }
            callMethod(cursor, "moveToFirst")
            val username = callMethod(cursor, "getString", 0) as String
            nameCache[nickname] = username
            return username
        } catch (e: Throwable) {
            log(e); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }
}