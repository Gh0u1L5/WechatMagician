package com.gh0u1l5.wechatmagician.storage.database

import com.gh0u1l5.wechatmagician.util.MessageUtil.longToDecimalString
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers

object SnsDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var snsDB: Any? = null

    // getSnsIdFromRowId searches the database to find the snsId in a specific row
    fun getSnsIdFromRowId(rowId: String?): String? {
        if (rowId == null || rowId == "") return null
        val db = snsDB ?: return null

        try {
            val cursor = XposedHelpers.callMethod(db, "query",
                    "SnsInfo", arrayOf("snsId"), "rowId=?", arrayOf(rowId),
                    null, null, null, null
            )
            val count = XposedHelpers.callMethod(cursor, "getCount")
            if (count != 1) {
                XposedBridge.log("DB => Unexpected count $count for rowId $rowId in table SnsInfo")
                return null
            }
            XposedHelpers.callMethod(cursor, "moveToFirst")
            val snsId = XposedHelpers.callMethod(cursor, "getLong", 0)
            XposedHelpers.callMethod(cursor, "close")
            return longToDecimalString(snsId as Long)
        } catch (e: Throwable) {
            log(e); return null
        }
    }
}
