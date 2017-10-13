package com.gh0u1l5.wechatmagician.backend

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.plugins.Frontend
import com.gh0u1l5.wechatmagician.backend.plugins.Limits
import com.gh0u1l5.wechatmagician.backend.plugins.SnsUI
import com.gh0u1l5.wechatmagician.backend.plugins.System
import com.gh0u1l5.wechatmagician.storage.*
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil.shadowCopy
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

// WechatHook contains the entry points and all the hooks.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val res = LocalizedResources
    private val preferences = Preferences()
    @Volatile private lateinit var loader: ClassLoader

    // Hook for hacking Wechat application.
    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName == "com.gh0u1l5.wechatmagician") {
            val pluginFrontend = Frontend(param.classLoader)
            tryHook(pluginFrontend::notifyStatus, {})
        }

        if (param.packageName != "com.tencent.mm") {
            return
        }

        try {
            pkg.init(param)
            loader = param.classLoader
            preferences.load(
                    XSharedPreferences("com.gh0u1l5.wechatmagician")
            )

            val process = param.processName
            if (process == "com.tencent.mm") {
                thread(start = true) {
                    pkg.dumpPackage()
                }
            }
        } catch (e: Throwable) {
            log("INIT => ${e.message}")
            log("${e.stackTrace}")
            return
        }

        val pluginSystem = System(param.classLoader, preferences)
        tryHook(pluginSystem::traceTouchEvents, {})
        tryHook(pluginSystem::traceActivities, {})
        tryHook(pluginSystem::enableXLog, {
            pkg.XLogSetup = null
        })

        val pluginSnsUI = SnsUI(preferences)
        tryHook(pluginSnsUI::setItemLongPressPopupMenu, {
            pkg.AdFrameLayout = null
        })
        tryHook(pluginSnsUI::cleanTextViewForForwarding, {
            pkg.SnsUploadUI = null
        })

        val pluginLimits = Limits(preferences)
        tryHook(pluginLimits::breakSelectPhotosLimit, {
            pkg.AlbumPreviewUI = null
        })
        tryHook(pluginLimits::breakSelectContactLimit, {
            pkg.SelectContactUI = null
        })
        tryHook(pluginLimits::breakSelectConversationLimit, {
            pkg.SelectConversationUI = null
        })

        // Hooks for Storage / XML / Database
        tryHook(this::hookMsgStorage, {
            pkg.MsgStorageClass = null
        })
        tryHook(this::hookImgStorage, {
            pkg.ImgStorageClass = null
        })
        tryHook(this::hookXMLParse, {
            pkg.XMLParserClass = null
        })
        tryHook(this::hookDatabase, {
            pkg.SQLiteDatabaseClass = null
        })
    }

    private fun tryHook(hook: () -> Unit, cleanup: (Throwable) -> Unit) {
        try { hook() } catch (e: Throwable) { log("HOOK => $e"); cleanup(e) }
    }

    private fun hookMsgStorage() {
        if (pkg.MsgStorageClass == null || pkg.MsgStorageInsertMethod == "") {
            return
        }

        // Analyze dynamically to find the global message storage instance.
        hookAllConstructors(pkg.MsgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.MsgStorageObject !== param.thisObject) {
                    pkg.MsgStorageObject = param.thisObject
                }
            }
        })

        // Hook MsgStorage to record the received messages.
        findAndHookMethod(pkg.MsgStorageClass, pkg.MsgStorageInsertMethod, pkg.MsgInfoClass, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                thread(start = true) {
                    val msg = param.args[0]
                    val msgId = getLongField(msg, "field_msgId")
                    MessageCache[msgId] = msg
                }
            }
        })

        HookStatus += "MsgStorage"
    }

    private fun hookImgStorage() {
        if (pkg.ImgStorageClass == null) {
            return
        }

        // Analyze dynamically to find the global image storage instance.
        hookAllConstructors(pkg.ImgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.ImgStorageObject !== param.thisObject) {
                    pkg.ImgStorageObject = param.thisObject
                }
            }
        })

//        findAndHookMethod(pkg.ImgStorageClass, pkg.ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val imgId = param.args[0] as String?
//                val prefix = param.args[1] as String?
//                val suffix = param.args[2] as String?
//                log("IMG => imgId = $imgId, prefix = $prefix, suffix = $suffix")
//            }
//        })

        // Hook FileOutputStream to prevent Wechat from overwriting disk cache.
        findAndHookConstructor("java.io.FileOutputStream", loader, C.File, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val path = (param.args[0] as File?)?.path ?: return
                if (path in ImageUtil.blockTable) {
                    param.throwable = IOException()
                }
            }
        })

        HookStatus += "ImgStorage"
    }

    private fun hookXMLParse() {
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == "") {
            return
        }

        // Hook XML Parser for the status bar easter egg.
        findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, C.String, C.String, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_xml_parser", false)) {
                    val xml = param.args[0] as String?
                    val tag = param.args[1] as String?
                    log("XML => xml = $xml, tag = $tag")
                }

                @Suppress("UNCHECKED_CAST")
                val result = param.result as MutableMap<String, String?>? ?: return
                if (result[".sysmsg.\$type"] == "revokemsg") {
                    val msgTag = ".sysmsg.revokemsg.replacemsg"
                    val msg = result[msgTag] ?: return
                    if (!msg.startsWith("\"")) {
                        return
                    }
                    if (!preferences.getBoolean("settings_chatting_recall", true)) {
                        return
                    }
                    val prompt = preferences.getString(
                            "settings_chatting_recall_prompt", res["prompt_recall"])
                    result[msgTag] = MessageUtil.applyEasterEgg(msg, prompt)
                }
                if (result[".TimelineObject"] != null) {
                    thread(start = true) {
                        val id = result[".TimelineObject.id"]
                        if (id != null) {
                            SnsCache[id] = SnsCache.SnsInfo(result)
                        }
                    }
                }
            }
        })

        HookStatus += "XMLParser"
    }

    private fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == null){
            return
        }

        val dbPkg = pkg.SQLiteDatabasePkg
        val typeSQLiteCipherSpec = findClass("$dbPkg.database.SQLiteCipherSpec", loader)
        val typeCursorFactory = findClass("$dbPkg.database.SQLiteDatabase.CursorFactory", loader)
        val typeDatabaseErrorHandler = findClass("$dbPkg.DatabaseErrorHandler", loader)
        val typeCancellationSignal = findClass("$dbPkg.support.CancellationSignal", loader)

        // Hook SQLiteDatabase.openDatabase to capture the database instance for SNS.
        findAndHookMethod(pkg.SQLiteDatabaseClass, "openDatabase", C.String, C.ByteArray, typeSQLiteCipherSpec, typeCursorFactory, C.Int, typeDatabaseErrorHandler, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
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

        findAndHookMethod(pkg.SQLiteDatabaseClass, "rawQueryWithFactory", typeCursorFactory, C.String, C.StringArray, C.String, typeCancellationSignal, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_database_query", false)) {
                    val sql = param.args[1] as String? ?: return
                    val selectionArgs = param.args[2] as Array<*>?
                    log("DB => query sql = $sql, selectionArgs = ${MessageUtil.argsToString(selectionArgs)}")
                }
            }
        })

        findAndHookMethod(pkg.SQLiteDatabaseClass, "insertWithOnConflict", C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_database_insert", false)) {
                    val table = param.args[0] as String? ?: return
                    val values = param.args[2] as ContentValues? ?: return
                    log("DB => insert table = $table, values = $values")
                }
            }
        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments.
        findAndHookMethod(pkg.SQLiteDatabaseClass, "updateWithOnConflict", C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String? ?: return
                val values = param.args[1] as ContentValues? ?: return
                if (preferences.getBoolean("developer_database_update", false)) {
                    val whereClause = param.args[2] as String?
                    val whereArgs = param.args[3] as Array<*>?
                    log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
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
                        val content =  values["content"] as ByteArray?
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

        findAndHookMethod(pkg.SQLiteDatabaseClass, "delete", C.String, C.String, C.StringArray, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_database_delete", false)) {
                    val table = param.args[0] as String?
                    val whereClause = param.args[1] as String?
                    val whereArgs = param.args[2] as Array<*>?
                    log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
                }
            }
        })

        findAndHookMethod(pkg.SQLiteDatabaseClass, "executeSql", C.String, C.ObjectArray, typeCancellationSignal, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (preferences.getBoolean("developer_database_execute", false)) {
                    val sql = param.args[0] as String?
                    val bindArgs = param.args[1] as Array<*>?
                    log("DB => executeSql sql = $sql, bindArgs = ${MessageUtil.argsToString(bindArgs)}")
                }
            }
        })

        HookStatus += "Database"
    }

    // handleMessageRecall notifies user that someone has recalled the given message.
    private fun handleMessageRecall(values: ContentValues) {
        if (pkg.MsgStorageObject == null || pkg.MsgStorageInsertMethod == "") {
            return
        }

        val msgId = values["msgId"] as Long
        val msg = MessageCache[msgId] ?: return

        val copy = msg.javaClass.newInstance()
        shadowCopy(msg, copy)

        val createTime = getLongField(msg, "field_createTime")
        setIntField(copy, "field_type", values["type"] as Int)
        setObjectField(copy, "field_content", values["content"])
        setLongField(copy, "field_createTime", createTime + 1L)

        callMethod(pkg.MsgStorageObject, pkg.MsgStorageInsertMethod, copy, false)
    }

    // handleMomentDelete notifies user that someone has deleted the given moment.
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(res["label_deleted"], content)?.let { msg ->
            values.remove("sourceType")
            values.put("content", msg)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments.
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(res["label_deleted"], curActionBuf)?.let { msg ->
            values.remove("commentflag")
            values.put("curActionBuf", msg)
        }
    }
}
