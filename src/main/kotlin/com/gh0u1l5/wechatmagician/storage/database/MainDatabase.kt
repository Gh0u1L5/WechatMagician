package com.gh0u1l5.wechatmagician.storage.database

import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers

object MainDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var mainDB: Any? = null

    fun getUsernameFromNickname(nickname: String): String? {
        if (nickname == "") return null
        val db = mainDB ?: return null

        try {
            val cursor = XposedHelpers.callMethod(db, "query",
                    "rcontact", arrayOf("username"), "nickname=?", arrayOf(nickname),
                    null, null, null, null
            )
            val count = XposedHelpers.callMethod(cursor, "getCount")
            if (count != 1) {
                return null
            }
            XposedHelpers.callMethod(cursor, "moveToFirst")
            val username = XposedHelpers.callMethod(cursor, "getString", 0)
            XposedHelpers.callMethod(cursor, "close")
            return username as String
        } catch (e: Throwable) {
            log(e); return null
        }
    }
}