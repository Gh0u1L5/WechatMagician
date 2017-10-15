package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.MessageCache
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.storage.Strings
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

class Database(private val loader: ClassLoader, private val preferences: Preferences) {

    private val str = Strings
    private val pkg = WechatPackage

    fun hookDatabase() {
        when (null) {
            pkg.SQLiteDatabaseClass,
            pkg.SQLiteCursorFactory,
            pkg.SQLiteErrorHandler -> return
        }

        // Hook SQLiteDatabase.openDatabase to capture the database instance for SNS.
        findAndHookMethod(
                pkg.SQLiteDatabaseClass, "openDatabase",
                C.String, pkg.SQLiteCursorFactory, C.Int, pkg.SQLiteErrorHandler, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                val path = param.args[0] as String?
                if (path?.endsWith("SnsMicroMsg.db") != true) {
                    return
                }
                if (SnsCache.snsDB !== param.result) {
                    SnsCache.snsDB = param.result
                    // Force Wechat to retrieve existing SNS data online.
                    callMethod(SnsCache.snsDB, "delete",
                            "snsExtInfo3", "local_flag=0", null
                    )
                }
            }
        })



        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments.
        findAndHookMethod(
                pkg.SQLiteDatabaseClass, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: de.robv.android.xposed.XC_MethodHook.MethodHookParam) {
                val table = param.args[0] as String? ?: return
                val values = param.args[1] as android.content.ContentValues? ?: return

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

        pkg.status.Database = true
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