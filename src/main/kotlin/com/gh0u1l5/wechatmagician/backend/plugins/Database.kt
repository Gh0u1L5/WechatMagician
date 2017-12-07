package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_RECALL
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_DELETE_COMMENT
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SNS_DELETE_MOMENT
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.LABEL_DELETED
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.cache.MessageCache
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.mainDB
import com.gh0u1l5.wechatmagician.storage.database.SnsDatabase.snsDB
import com.gh0u1l5.wechatmagician.storage.list.SnsBlacklist
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Database {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    @JvmStatic fun hookDatabase() {
        // Hook SQLiteDatabase constructors to catch the database instances.
        hookAllConstructors(pkg.SQLiteDatabase, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val path = param.thisObject.toString()
                if (path.endsWith("EnMicroMsg.db")) {
                    if (mainDB !== param.thisObject) {
                        mainDB = param.thisObject
                    }
                }
            }
        })

        // Hook SQLiteDatabase.openDatabase to initialize the database instance for SNS.
        findAndHookMethod(
                pkg.SQLiteDatabase, "openDatabase",
                C.String, pkg.SQLiteCursorFactory, C.Int, pkg.SQLiteErrorHandler, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val path = param.args[0] as String? ?: return
                if (path.endsWith("SnsMicroMsg.db")) {
                    if (snsDB !== param.result) {
                        snsDB = param.result
                        // Force Wechat to retrieve existing SNS data from remote server.
                        val deleted = ContentValues().apply { put("sourceType", 0) }
                        callMethod(snsDB, "delete", "snsExtInfo3", "local_flag=0", null)
                        callMethod(snsDB, "update", "SnsInfo", deleted, "sourceType in (8,10,12,14)", null)
                    }
                }
            }
        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments.
        findAndHookMethod(
                pkg.SQLiteDatabase, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String? ?: return
                val values = param.args[1] as ContentValues? ?: return

                when (table) {
                    "message" -> { // recall message
                        if (values["type"] != 10000) {
                            return
                        }
                        if (!values.getAsString("content").startsWith("\"")) {
                            return
                        }
                        if (!preferences!!.getBoolean(SETTINGS_CHATTING_RECALL, true)) {
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
                        if (values["stringSeq"] in SnsBlacklist) {
                            return
                        }
                        if (!preferences!!.getBoolean(SETTINGS_SNS_DELETE_MOMENT, true)) {
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
                        if (!preferences!!.getBoolean(SETTINGS_SNS_DELETE_COMMENT, true)) {
                            return
                        }
                        val curActionBuf = values["curActionBuf"] as ByteArray?
                        handleCommentDelete(curActionBuf, values)
                    }
                }
            }
        })

        pkg.setStatus(STATUS_FLAG_DATABASE, true)
    }

    // handleMessageRecall notifies user that someone has recalled the given message.
    private fun handleMessageRecall(values: ContentValues) {
        if (pkg.MsgStorageObject == null) {
            return
        }

        try {
            val msgId = values["msgId"] as Long
            val msg = MessageCache[msgId] ?: return

            val copy = msg.javaClass.newInstance()
            PackageUtil.shadowCopy(msg, copy)

            val createTime = XposedHelpers.getLongField(msg, "field_createTime")
            XposedHelpers.setIntField(copy, "field_type", values["type"] as Int)
            XposedHelpers.setObjectField(copy, "field_content", values["content"])
            XposedHelpers.setLongField(copy, "field_createTime", createTime + 1L)

            when (pkg.MsgStorageInsertMethod.parameterTypes.size) {
                1 -> pkg.MsgStorageInsertMethod.invoke(pkg.MsgStorageObject, copy)
                2 -> pkg.MsgStorageInsertMethod.invoke(pkg.MsgStorageObject, copy, false)
            }
        } catch (e: Throwable) {
            XposedBridge.log("DB => Handle message recall failed: $e")
        }
    }

    // handleMomentDelete notifies user that someone has deleted the given moment.
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(str[LABEL_DELETED], content)?.let { msg ->
            values.remove("sourceType")
            values.put("content", msg)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments.
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(str[LABEL_DELETED], curActionBuf)?.let { msg ->
            values.remove("commentflag")
            values.put("curActionBuf", msg)
        }
    }
}