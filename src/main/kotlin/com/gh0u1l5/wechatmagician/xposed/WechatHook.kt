package com.gh0u1l5.wechatmagician.xposed

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.XModuleResources
import android.view.Menu
import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil.deepCopy
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedBridge.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.*

// WechatHook contains the entry points and all the hooks.
class WechatHook : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val res = ModuleResources
    private lateinit var loader: ClassLoader

    // Hook for initializing localized resources.
    override fun initZygote(param: IXposedHookZygoteInit.StartupParam?) {
        ModuleResources.init(XModuleResources.createInstance(param?.modulePath, null))
    }

    // Hook for hacking Wechat application.
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != "com.tencent.mm") {
            return
        }

        WechatPackage.init(param)
        loader = param.classLoader


        tryHook(this::hookOptionsMenu, {})

        tryHook(this::hookSelectContactUI, {
            pkg.SelectContactUI = ""
        })
        tryHook(this::hookSelectConversationUI, {
            pkg.SelectConversationUI = ""
        })

        tryHook(this::hookMsgStorage, {
            pkg.MsgStorageClass = ""
        })
        tryHook(this::hookImgStorage, {
            pkg.ImgStorageClass = ""
        })
        tryHook(this::hookXMLParse, {
            pkg.XMLParserClass = ""
        })
        tryHook(this::hookDatabase, {
            pkg.SQLiteDatabaseClass = ""
        })
    }

    private fun tryHook(hook: () -> Unit, cleanup: (Throwable) -> Unit) {
        try { hook() } catch (e: Throwable) { log("HOOK => $e"); cleanup(e) }
    }

    private fun hookOptionsMenu() {
        // Hook onCreateOptionsMenu to add new buttons in the OptionsMenu.
        findAndHookMethod(pkg.MMActivity, loader, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val menu = param.args[0] as Menu? ?: return
                val button = WechatButtons[param.thisObject.javaClass.name] ?: return

                val item = menu.add(button.groupId, button.itemId, button.order, button.title)
                button.decorate(item, param.thisObject)
                val listener = button.listener(param.thisObject)
                item.setOnMenuItemClickListener(listener)
            }
        })
    }

    private fun hookSelectContactUI() {
        if (pkg.SelectContactUI == "") {
            return
        }

        // Hook SelectContactUI to help the "Select All" button.
        findAndHookMethod(pkg.SelectContactUI, loader, "onActivityResult", C.Int, C.Int, C.Intent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val requestCode = param.args[0] as Int
                val resultCode = param.args[1] as Int
                val data = param.args[2] as Intent?

                if (requestCode == 5) {
                    val activity = param.thisObject as Activity
                    activity.setResult(resultCode, data)
                    activity.finish()
                    param.result = null
                }
            }
        })

        // Hook SelectContactUI to bypass the limitation in retransmitting messages.
        findAndHookMethod(pkg.MMFragmentActivity, loader, "startActivityForResult", C.Intent, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent? ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        })
    }

    private fun hookSelectConversationUI() {
        if (pkg.SelectConversationUI == "" || pkg.SelectConversationUIMaxLimitMethod == "") {
            return
        }

        // Hook SelectConversationUI to bypass the limitation in retransmitting messages.
        findAndHookMethod(pkg.SelectConversationUI, loader, pkg.SelectConversationUIMaxLimitMethod, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }

    private fun hookMsgStorage() {
        if (pkg.MsgStorageClass == "" || pkg.MsgStorageInsertMethod == "") {
            return
        }

        // Analyze dynamically to find the global message storage instance
        val typeMsgStorage = findClass(pkg.MsgStorageClass, loader)
        hookAllConstructors(typeMsgStorage, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.MsgStorageObject !== param.thisObject) {
                    pkg.MsgStorageObject = param.thisObject
                }
            }
        })

        // Hook MsgStorage to record the received messages.
        val typeMsgInfo = findClass(pkg.MsgInfoClass, loader)
        findAndHookMethod(pkg.MsgStorageClass, loader, pkg.MsgStorageInsertMethod, typeMsgInfo, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val msg = param.args[0]
                val msgId = getLongField(msg, "field_msgId")
                MessageCache[msgId] = msg
            }
        })
    }

    private fun hookImgStorage() {
        if (pkg.ImgStorageClass == "") {
            return
        }

        // Analyze dynamically to find the global image storage instance
        val typeImgStorage = findClass(pkg.ImgStorageClass, loader)
        hookAllConstructors(typeImgStorage, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.ImgStorageObject !== param.thisObject) {
                    pkg.ImgStorageObject = param.thisObject
                }
            }
        })

//        findAndHookMethod(typeImgStorage, pkg.ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val imgId = param.args[0] as String?
//                val prefix = param.args[1] as String?
//                val suffix = param.args[2] as String?
//                log("IMG => imgId = $imgId, prefix = $prefix, suffix = $suffix")
//            }
//        })

        // Hook FileOutputStream to prevent Wechat from overwriting disk cache
        val typeFileOutputStream = findClass("java.io.FileOutputStream", loader)
        findAndHookConstructor(typeFileOutputStream, C.File, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val path = (param.args[0] as File?)?.path ?: return
                if (path in ImageUtil.blockTable) {
                    param.throwable = IOException()
                }
            }
        })
    }

    private fun hookXMLParse() {
        if (pkg.XMLParserClass == "" || pkg.XMLParseMethod == "") {
            return
        }

        // Hook XML Parser for the status bar easter egg
        findAndHookMethod(pkg.XMLParserClass, loader, pkg.XMLParseMethod, C.String, C.String, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
//                val xml = param.args[0] as String?
//                val tag = param.args[1] as String?
//                log("XML => xml = $xml, tag = $tag")

                @Suppress("UNCHECKED_CAST")
                val result = param.result as MutableMap<String, String?>? ?: return
                if (result[".sysmsg.\$type"] != "revokemsg") {
                    return
                }
                val msgTag = ".sysmsg.revokemsg.replacemsg"
                val msg = result[msgTag] ?: return
                if (msg.startsWith("\"")) {
                    result[msgTag] = MessageUtil.applyEasterEgg(msg, res.labelEasterEgg)
                }
            }
        })
    }

    private fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == ""){
            return
        }

        val typeSQLiteDatabase = findClass(pkg.SQLiteDatabaseClass, loader)

//        findAndHookMethod(typeSQLiteDatabase, "insertWithOnConflict", C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String? ?: return
//                val values = param.args[2] as ContentValues? ?: return
//                log("DB => insert table = $table, values = $values")
//            }
//        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments
        findAndHookMethod(typeSQLiteDatabase, "updateWithOnConflict", C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String? ?: return
                val values = param.args[1] as ContentValues? ?: return
//                val whereClause = param.args[2] as String?
//                val whereArgs = param.args[3] as Array<*>?
//                log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")

                when (table) {
                    "message" -> { // recall message
                        if (values["type"] != 10000) {
                            return
                        }

                        val msgId = values["msgId"] as Long
                        val msg = MessageCache[msgId] ?: return

                        val copy = msg.javaClass.newInstance()
                        deepCopy(msg, copy)

                        val createTime = getLongField(msg, "field_createTime")
                        setIntField(copy, "field_type", values["type"] as Int)
                        setObjectField(copy, "field_content", values["content"])
                        setLongField(copy, "field_createTime", createTime + 1L)

                        callMethod(pkg.MsgStorageObject, pkg.MsgStorageInsertMethod, copy, false)
                        param.result = 1
                    }
                    "SnsInfo" -> { // delete moment
                        if (values["sourceType"] != 0) {
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
                        val curActionBuf = values["curActionBuf"] as ByteArray?
                        handleCommentDelete(curActionBuf, values)
                    }
                }
            }
        })

//        val typeCursorFactory = findClass("com.tencent.wcdb.database.SQLiteDatabase.CursorFactory", loader)
//        val typeCancellationSignal = findClass("com.tencent.wcdb.support.CancellationSignal", loader)
//        findAndHookMethod(typeSQLiteDatabase, "rawQueryWithFactory", typeCursorFactory, C.String, C.StringArray, C.String, typeCancellationSignal, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val sql = param.args[1] as String? ?: return
//                val selectionArgs = param.args[2] as Array<*>?
//                log("DB => sql = $sql, selectionArgs = ${MessageUtil.argsToString(selectionArgs)}")
//            }
//        })

//        findAndHookMethod(typeSQLiteDatabase, "delete", C.String, C.String, C.StringArray, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String?
//                val whereClause = param.args[1] as String?
//                val whereArgs = param.args[2] as Array<*>?
//                log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
//            }
//        })

//        findAndHookMethod(typeSQLiteDatabase, "executeSql", C.String, C.ObjectArray, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val sql = param.args[0] as String?
//                val bindArgs = param.args[1] as Array<*>?
//                log("DB => executeSql sql = $sql, bindArgs = ${MessageUtil.argsToString(bindArgs)}")
//            }
//        })
    }

    // handleImageRecall notifies user that someone has recalled an image.
    private fun handleImageRecall(origin: Any, values: ContentValues) {
        if (getIntField(origin, "field_type") != 3) {
            return
        }
        values.remove("type")
        values.remove("content")
        val imgPath = getObjectField(origin, "field_imgPath")
        ImageUtil.replaceThumbnail(imgPath as String, res.bitmapRecalled)
    }

    // handleMomentDelete notifies user that someone has deleted the given moment.
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(res.labelDeleted, content)?.let { msg ->
            values.remove("sourceType")
            values.put("content", msg)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments.
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(res.labelDeleted, curActionBuf)?.let { msg ->
            values.remove("commentflag")
            values.put("curActionBuf", msg)
        }
    }
}