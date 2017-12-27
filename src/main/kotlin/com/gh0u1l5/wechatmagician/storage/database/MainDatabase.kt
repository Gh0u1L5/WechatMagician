package com.gh0u1l5.wechatmagician.storage.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import java.util.concurrent.ConcurrentHashMap

object MainDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var mainDB: Any? = null

    // nicknameCache maps nicknames to corresponding usernames
    private val nicknameCache: MutableMap<String, String> = ConcurrentHashMap()

    fun cleanUnreadCount() {
        val database = mainDB ?: return
        val clean = ContentValues().apply {
            put("unReadCount", 0)
            put("unReadMuteCount", 0)
        }
        callMethod(database, "update", "rconversation", clean, null, null)
    }

    private fun queryUsernames(selection: String, selectionArgs: Array<String>): List<String>? {
        val database = mainDB ?: return null
        var cursor: Any? = null
        try {
            cursor = callMethod(database, "query",
                    "rcontact", arrayOf("username"), selection, selectionArgs,
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount") as Int
            return (0 until count).map {
                callMethod(cursor, "moveToNext")
                callMethod(cursor, "getString", 0) as String
            }
        } catch (e: Throwable) {
            log(e); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }

    fun getUsernamesFromAlias(alias: String): List<String>? {
        if (alias == "") {
            return null
        }
        return queryUsernames("alias=?", arrayOf(alias))
    }

    fun getUsernameFromNickname(nickname: String): String? {
        if (nickname in nicknameCache) {
            return nicknameCache[nickname]
        }
        if (nickname == "") {
            return null
        }
        val result = queryUsernames("nickname=?", arrayOf(nickname))
        if (result != null && result.isNotEmpty()) {
            if (result.size != 1) {
                log("Expected unique nickname for each user!")
            }
            nicknameCache[nickname] = result.first()
        }
        return result?.firstOrNull()
    }
}