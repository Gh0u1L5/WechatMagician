package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.math.BigInteger

object SnsDatabase {
    // snsDB is the database that stores SNS information.
    @Volatile var snsDB: Any? = null

    // TWO_64 is a constant used by toDecimalString()
    private val TWO_64 = BigInteger.ONE.shiftLeft(64)

    // toDecimalString convert a signed Long to unsigned decimal String
    private fun toDecimalString(l: Long): String {
        var b = BigInteger.valueOf(l)
        if (b.signum() < 0) {
            b = b.add(TWO_64)
        }
        return b.toString()
    }

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
        return toDecimalString(snsId as Long)
    }
}
