package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.MessageCache
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil
import de.robv.android.xposed.XposedHelpers

class Database(private val loader: ClassLoader, private val preferences: Preferences) {

    private val str = Strings
    private val pkg = WechatPackage

    fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == null){
            return
        }

        val dbPkg = pkg.SQLiteDatabasePkg
        val typeSQLiteCipherSpec = de.robv.android.xposed.XposedHelpers.findClass("$dbPkg.database.SQLiteCipherSpec", loader)
        val typeCursorFactory = de.robv.android.xposed.XposedHelpers.findClass("$dbPkg.database.SQLiteDatabase.CursorFactory", loader)
        val typeDatabaseErrorHandler = de.robv.android.xposed.XposedHelpers.findClass("$dbPkg.DatabaseErrorHandler", loader)
        val typeCancellationSignal = de.robv.android.xposed.XposedHelpers.findClass("$dbPkg.support.CancellationSignal", loader)

        // Hook SQLiteDatabase.openDatabase to capture the database instance for SNS.
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "openDatabase", com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.ByteArray, typeSQLiteCipherSpec, typeCursorFactory, com.gh0u1l5.wechatmagician.C.Int, typeDatabaseErrorHandler, com.gh0u1l5.wechatmagician.C.Int, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                val path = param.args[0] as String?
                if (path?.endsWith("SnsMicroMsg.db") != true) {
                    return
                }
                if (com.gh0u1l5.wechatmagician.storage.SnsCache.snsDB !== param.result) {
                    com.gh0u1l5.wechatmagician.storage.SnsCache.snsDB = param.result
                    // Force Wechat to retrieve existing SNS data online.
                    de.robv.android.xposed.XposedHelpers.callMethod(com.gh0u1l5.wechatmagician.storage.SnsCache.snsDB, "delete",
                            "snsExtInfo3", "local_flag=0", null
                    )
                }
            }
        })

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "rawQueryWithFactory", typeCursorFactory, com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.StringArray, com.gh0u1l5.wechatmagician.C.String, typeCancellationSignal, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                if (preferences.getBoolean("developer_database_query", false)) {
                    val sql = param.args[1] as String? ?: return
                    val selectionArgs = param.args[2] as Array<*>?
                    de.robv.android.xposed.XposedBridge.log("DB => query sql = $sql, selectionArgs = ${com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString(selectionArgs)}")
                }
            }
        })

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "insertWithOnConflict", com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.ContentValues, com.gh0u1l5.wechatmagician.C.Int, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                if (preferences.getBoolean("developer_database_insert", false)) {
                    val table = param.args[0] as String? ?: return
                    val values = param.args[2] as android.content.ContentValues? ?: return
                    de.robv.android.xposed.XposedBridge.log("DB => insert table = $table, values = $values")
                }
            }
        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments.
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "updateWithOnConflict", com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.ContentValues, com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.StringArray, com.gh0u1l5.wechatmagician.C.Int, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                val table = param.args[0] as String? ?: return
                val values = param.args[1] as android.content.ContentValues? ?: return
                if (preferences.getBoolean("developer_database_update", false)) {
                    val whereClause = param.args[2] as String?
                    val whereArgs = param.args[3] as Array<*>?
                    de.robv.android.xposed.XposedBridge.log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString(whereArgs)}")
                }

                when (table) {
                    "message" -> { // recall message
                        if (values["type"] != 10000) {
                            return
                        }
                        if (!values.getAsString("content").startsWith("\"")) {
                            return
                        }
                        if (!preferences.getBoolean("settings_chatting_recall", true)) {
                            return
                        }
                        handleMessageRecall(values)
                        param.result = 1
                    }
                    "SnsInfo" -> { // delete moment
                        if (values["type"] !in listOf(1, 2, 3, 15)) {
                            return
                        }
                        if (values["sourceType"] != 0) {
                            return
                        }
                        if (!preferences.getBoolean("settings_sns_delete_moment", true)) {
                            return
                        }
                        val content = values["content"] as ByteArray?
                        handleMomentDelete(content, values)
                    }
                    "SnsComment" -> { // delete moment comment
                        if (values["type"] == 1) {
                            return
                        }
                        if (values["commentflag"] != 1) {
                            return
                        }
                        if (!preferences.getBoolean("settings_sns_delete_comment", true)) {
                            return
                        }
                        val curActionBuf = values["curActionBuf"] as ByteArray?
                        handleCommentDelete(curActionBuf, values)
                    }
                }
            }
        })

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "delete", com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.StringArray, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                if (preferences.getBoolean("developer_database_delete", false)) {
                    val table = param.args[0] as String?
                    val whereClause = param.args[1] as String?
                    val whereArgs = param.args[2] as Array<*>?
                    de.robv.android.xposed.XposedBridge.log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString(whereArgs)}")
                }
            }
        })

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(pkg.SQLiteDatabaseClass, "executeSql", com.gh0u1l5.wechatmagician.C.String, com.gh0u1l5.wechatmagician.C.ObjectArray, typeCancellationSignal, object : de.robv.android.xposed.XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                if (preferences.getBoolean("developer_database_execute", false)) {
                    val sql = param.args[0] as String?
                    val bindArgs = param.args[1] as Array<*>?
                    de.robv.android.xposed.XposedBridge.log("DB => executeSql sql = $sql, bindArgs = ${com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString(bindArgs)}")
                }
            }
        })

        com.gh0u1l5.wechatmagician.storage.HookStatus.Database = true
    }

    // handleMessageRecall notifies user that someone has recalled the given message.
    private fun handleMessageRecall(values: ContentValues) {
        if (pkg.MsgStorageObject == null || pkg.MsgStorageInsertMethod == "") {
            return
        }

        val msgId = values["msgId"] as Long
        val msg = MessageCache[msgId] ?: return

        val copy = msg.javaClass.newInstance()
        PackageUtil.shadowCopy(msg, copy)

        val createTime = XposedHelpers.getLongField(msg, "field_createTime")
        XposedHelpers.setIntField(copy, "field_type", values["type"] as Int)
        XposedHelpers.setObjectField(copy, "field_content", values["content"])
        XposedHelpers.setLongField(copy, "field_createTime", createTime + 1L)

        XposedHelpers.callMethod(pkg.MsgStorageObject, pkg.MsgStorageInsertMethod, copy, false)
    }

    // handleMomentDelete notifies user that someone has deleted the given moment.
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(str["label_deleted"], content)?.let { msg ->
            values.remove("sourceType")
            values.put("content", msg)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments.
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(str["label_deleted"], curActionBuf)?.let { msg ->
            values.remove("commentflag")
            values.put("curActionBuf", msg)
        }
    }
}