package com.gh0u1l5.wechatmagician.backend.storage.database

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.SnsDatabaseObject
import com.gh0u1l5.wechatmagician.util.MessageUtil.longToDecimalString
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callMethod

object SnsDatabase {
    // getSnsIdFromRowId searches the database to find the snsId in a specific row
    fun getSnsIdFromRowId(rowId: String?): String? {
        if (rowId.isNullOrEmpty()) {
            return null
        }

        val database = SnsDatabaseObject ?: return null
        var cursor: Any? = null
        try {
            cursor = callMethod(database, "query",
                    "SnsInfo", arrayOf("snsId"), "rowId=?", arrayOf(rowId),
                    null, null, null, null
            )
            val count = callMethod(cursor, "getCount")
            if (count != 1) {
                XposedBridge.log("DB => Unexpected count $count for rowId $rowId in table SnsInfo")
                return null
            }
            callMethod(cursor, "moveToFirst")
            val snsId = XposedHelpers.callMethod(cursor, "getLong", 0)
            return longToDecimalString(snsId as Long)
        } catch (t: Throwable) {
            log(t); return null
        } finally {
            if (cursor != null) {
                callMethod(cursor, "close")
            }
        }
    }
}
