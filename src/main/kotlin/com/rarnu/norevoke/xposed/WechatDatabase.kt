package com.rarnu.norevoke.xposed

import android.content.ContentValues
import android.database.Cursor
import android.database.Cursor.*
import de.robv.android.xposed.XposedHelpers

@Suppress("unused")
class WechatDatabase(db: Any?) {

    private var _db: Any? = null

    init { _db = db }

    fun insert(table: String?, selection: String?, values: ContentValues?): Any = XposedHelpers.callMethod(_db, "insert", table, selection, values)

    fun rawQuery(query: String?): Cursor? = rawQuery(query, null)

    fun rawQuery(query: String?, args: Array<String?>?): Cursor? = XposedHelpers.callMethod(_db, "rawQuery", query, args) as Cursor?

    fun getMessageViaId(id: String?): Cursor? = rawQuery("select * from message where msgId=?", arrayOf(id))

    fun cursorRowToContentValues(cur: Cursor?, values: ContentValues) {
        if (cur == null) return
        for (column in cur.columnNames) {
            val index = cur.getColumnIndex(column)
            when (cur.getType(index)) {
                FIELD_TYPE_INTEGER -> values.put(column, cur.getInt(index))
                FIELD_TYPE_FLOAT   -> values.put(column, cur.getFloat(index))
                FIELD_TYPE_STRING  -> values.put(column, cur.getString(index))
                FIELD_TYPE_BLOB    -> values.put(column, cur.getBlob(index))
            }
        }
    }
}