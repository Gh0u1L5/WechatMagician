package com.gh0u1l5.wechatmagician.backend.storage.database

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.MainDatabaseObject
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod

object MainDatabase {

    data class Contact(
            val alias: String,
            val username: String,
            val nickname: String
    )

    data class Conversation(
            val username: String,
            val digest: String,
            val digestUser: String,
            val atCount: Int,
            val unreadCount: Int
    )

    fun cleanUnreadCount() {
        val database = MainDatabaseObject ?: return
        val clean = ContentValues().apply {
            put("unReadCount", 0)
            put("unReadMuteCount", 0)
        }
        callMethod(database, "update", "rconversation", clean, null, null)
    }

    private fun getContacts(selection: String, selectionArgs: Array<String>, ignoreDuplicate: Boolean = false): List<Contact>? {
        val database = MainDatabaseObject ?: return null
        var cursor: Any? = null
        try {
            val columns = arrayOf("alias", "username", "nickname")
            cursor = callMethod(database, "query",
                    "rcontact", columns, selection, selectionArgs,
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount") as Int
            if (count == 0) {
                log("Contact Not Found: selection={$selection}, selectionArgs={$selectionArgs}")
            }
            if (count > 1 && !ignoreDuplicate) {
                log("Duplicate Contact: selection={$selection}, selectionArgs={$selectionArgs}")
            }
            return (0 until count).map {
                callMethod(cursor, "moveToNext")
                val alias    = callMethod(cursor, "getString", 0) as String
                val username = callMethod(cursor, "getString", 1) as String
                val nickname = callMethod(cursor, "getString", 2) as String
                Contact(alias, username, nickname)
            }
        } catch (t: Throwable) {
            log(t); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }

    fun getContactsByAlias(alias: String): List<Contact>? {
        if (alias.isEmpty()) {
            return null
        }
        return getContacts("alias=?", arrayOf(alias), ignoreDuplicate = true)
    }

    fun getContactByNickname(nickname: String): Contact? {
        if (nickname.isEmpty()) {
            return null
        }
        return getContacts("nickname=?", arrayOf(nickname))?.firstOrNull()
    }

    fun getContactByUsername(username: String): Contact? {
        if (username.isEmpty()) {
            return null
        }
        return getContacts("username=?", arrayOf(username))?.firstOrNull()
    }

    private fun getConversations(selection: String, selectionArgs: Array<String>, ignoreDuplicate: Boolean = false): List<Conversation>? {
        val database = MainDatabaseObject ?: return null
        var cursor: Any? = null
        try {
            val columns = arrayOf("username", "digest", "digestUser", "atCount", "unReadCount")
            cursor = callMethod(database, "query",
                    "rconversation", columns, selection, selectionArgs,
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount") as Int
            if (count == 0) {
                log("Conversation Not Found: selection={$selection}, selectionArgs={$selectionArgs}")
            }
            if (count > 1 && !ignoreDuplicate) {
                log("Duplicate Conversation: selection={$selection}, selectionArgs={$selectionArgs}")
            }
            return (0 until count).map {
                callMethod(cursor, "moveToNext")
                val username    = callMethod(cursor, "getString", 0) as String
                val digest      = callMethod(cursor, "getString", 1) as String
                val digestUser  = callMethod(cursor, "getString", 2) as String
                val atCount     = callMethod(cursor, "getInt", 3) as Int
                val unreadCount = callMethod(cursor, "getInt", 4) as Int
                Conversation(username, digest, digestUser, atCount, unreadCount)
            }
        } catch (t: Throwable) {
            log(t); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }

    fun getConversationByUsername(username: String): Conversation? {
        if (username.isEmpty()) {
            return null
        }
        return getConversations("username=?", arrayOf(username))?.firstOrNull()
    }
}