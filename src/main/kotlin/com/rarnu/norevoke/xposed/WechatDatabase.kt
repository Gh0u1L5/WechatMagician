package com.rarnu.norevoke.xposed

import android.content.ContentValues
import android.database.Cursor
import de.robv.android.xposed.XposedHelpers
import java.util.*

/**
 * Created by rarnu on 1/11/17.
 */
class WechatDatabase {

    private var _db: Any? = null

    constructor(db: Any?) {
        _db = db
    }

    fun insert(table: String?, selection: String?, values: ContentValues?) = XposedHelpers.callMethod(_db, "insert", table, selection, values)

    fun rawQuery(query: String?): Cursor? = rawQuery(query, null)

    fun rawQuery(query: String?, args: Array<String?>?): Cursor? = XposedHelpers.callMethod(_db, "rawQuery", query, args) as Cursor?

    fun getMessageViaId(id: String?): Cursor? = rawQuery("select * from message where msgsvrid=?", arrayOf(id))

    fun insertSystemMessage(talker: String?, talkerId: Int, msg: String?, createTime: Long) = insertMessage(talker, talkerId, msg, 10000, createTime)

    fun insertMessage(talker: String?, talkerId: Int, msg: String?, type: Int, createTime: Long) {
        val msgSvrId = createTime + (Random().nextInt())
        val msgId = getNextMsgId()
        val v = ContentValues()
        v.put("msgid", msgId)
        v.put("msgSvrid", msgSvrId)
        v.put("type", type)
        v.put("status", 3)
        v.put("createTime", createTime)
        v.put("talker", talker)
        v.put("content", msg)
        if (talkerId != -1) {
            v.put("talkerid", talkerId)
        }
        insert("message", "", v)
    }

    fun getNextMsgId(): Long {
        val cur = rawQuery("SELECT max(msgId) FROM message")
        if (cur == null || !cur.moveToFirst()) {
            return -1
        }
        val id = cur.getLong(0) + 1
        cur.close()
        return id
    }
}