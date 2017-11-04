package com.gh0u1l5.wechatmagician.storage

import com.gh0u1l5.wechatmagician.util.MessageUtil.longToDecimalString
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object SnsDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var snsDB: Any? = null

    // getSnsId searches the database to find the snsId in a specific row
    fun getSnsId(rowId: String?): String? {
        val db = snsDB ?: return null
        val cursor = XposedHelpers.callMethod(db, "query",
                "SnsInfo", arrayOf("snsId"), "rowId=$rowId", null,
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
    }
}
