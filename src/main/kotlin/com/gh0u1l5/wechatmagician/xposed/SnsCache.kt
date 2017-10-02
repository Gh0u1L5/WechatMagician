package com.gh0u1l5.wechatmagician.xposed

import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import java.math.BigInteger


object SnsCache {

    data class SnsMedia(
            val url: String?,
            val type: String?,
            val idx: String?,
            val key: String?,
            val token: String?
    )

    class SnsInfo(raw: MutableMap<String, String?>) {
        private val mediaListKey = ".TimelineObject.ContentObject.mediaList"

        val content = raw[".TimelineObject.contentDesc"]
        val medias = parseMedias(raw)

        private fun parseMedia(key: String, raw: MutableMap<String, String?>): SnsMedia? {
            if (key == "media0") {
                return parseMedia("media", raw)
            }
            if (!raw.containsKey("$mediaListKey.$key")) {
                return null
            }
            return SnsMedia(
                    url   = raw["$mediaListKey.$key.url"],
                    type  = raw["$mediaListKey.$key.url.\$type"],
                    idx   = raw["$mediaListKey.$key.url.\$enc_idx"],
                    key   = raw["$mediaListKey.$key.url.\$key"],
                    token = raw["$mediaListKey.$key.url.\$token"]
            )
        }

        private fun parseMedias(raw: MutableMap<String, String?>): List<SnsMedia> {
            if (!raw.containsKey(mediaListKey)) {
                return listOf()
            }
            val result = mutableListOf<SnsMedia>()
            for (i in 0 until 1000) {
                val media = parseMedia("media$i", raw) ?: break
                result += media
            }
            return result.toList()
        }
    }

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
        val cursor = callMethod(db, "query",
                "SnsInfo", arrayOf("snsId"), "rowId=$rowId", null,
                null, null, null, null
        )

        val count = callMethod(cursor, "getCount")
        if (count != 1) {
            log("DB => Unexpected count $count for rowId $rowId in table SnsInfo")
            return null
        }
        callMethod(cursor, "moveToNext")
        val snsId = callMethod(cursor, "getLong", 0)
        callMethod(cursor, "close")
        return toDecimalString(snsId as Long)
    }

    // snsTable maps snsId to SNS object.
    private var snsTable: Map<String, SnsInfo> = mapOf()

    @Synchronized operator fun get(snsId: String?): SnsInfo? {
        return snsTable[snsId]
    }

    @Synchronized operator fun set(snsId: String, record: SnsInfo) {
        snsTable += Pair(snsId, record)
    }

    @Synchronized operator fun contains(snsId: String): Boolean {
        return snsId in snsTable
    }
}