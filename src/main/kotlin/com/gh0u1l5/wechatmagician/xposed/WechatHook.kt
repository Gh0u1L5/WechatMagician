package com.gh0u1l5.wechatmagician.xposed

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.XModuleResources
import android.view.Menu
import com.gh0u1l5.wechatmagician.util.C
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.xposed.MessageCache.WechatMessage
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
                val menu = param.args[0] as Menu
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

    private fun hookImgStorage() {
        if (pkg.ImgStorageClass == "") {
            return
        }

        // Analyze dynamically to find the global image storage instance
        val imgStorageClass = findClass(pkg.ImgStorageClass, loader)
        hookAllConstructors(imgStorageClass, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (pkg.ImgStorageObject !== param.thisObject) {
                    pkg.ImgStorageObject = param.thisObject
                }
            }
        })

//        findAndHookMethod(imgStorageClass, pkg.ImgStorageLoadMethod, C.String, C.String, C.String, C.Boolean, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val imgId = param.args[0] as String?
//                val prefix = param.args[1] as String?
//                val suffix = param.args[2] as String?
//                log("IMG => imgId = $imgId, prefix = $prefix, suffix = $suffix")
//            }
//        })

        // Hook FileOutputStream to prevent Wechat from overwriting disk cache
        val clazz = findClass("java.io.FileOutputStream", loader)
        findAndHookConstructor(clazz, C.File, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val path = (param.args[0] as File).path
                synchronized(ImageUtil.blockTable) {
                    if (path in ImageUtil.blockTable) {
                        param.throwable = IOException()
                    }
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
                param.result = (param.result as MutableMap<String, String?>?)?.apply {
                    if (this[".sysmsg.\$type"] != "revokemsg") {
                        return
                    }
                    val msgtag = ".sysmsg.revokemsg.replacemsg"
                    val msg = this[msgtag] ?: return
                    if (msg.startsWith("\"")) {
                        this[msgtag] = MessageUtil.applyEasterEgg(msg, res.labelEasterEgg)
                    }
                }
            }
        })
    }

    private fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == ""){
            return
        }

        val clazz = findClass(pkg.SQLiteDatabaseClass, loader)

        // Hook SQLiteDatabase.insert to update MessageCache
        findAndHookMethod(clazz, "insertWithOnConflict", C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String?
                val values = param.args[2] as ContentValues? ?: return
//                log("DB => insert table = $table, values = $values")

                if (table == "message") {
                    var msgId = values["msgId"] as Long
                    synchronized(MessageCache.nextMsgId) {
                        if (MessageCache.nextMsgId == -1L) {
                            MessageCache.nextMsgId = msgId + 1
                        } else {
                            msgId = MessageCache.nextMsgId++
                            values.put("msgId", msgId)
                        }
                    }

                    if (values["isSend"] == 1) {
                        return // ignore the messages sent by myself
                    }
                    if (values["type"] == 10000) {
                        return // ignore system messages
                    }
                    MessageCache[msgId] = WechatMessage(values)
                }
            }
        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments
        findAndHookMethod(clazz, "updateWithOnConflict", C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val table = param.args[0] as String?
                val values = param.args[1] as ContentValues?
//                val whereClause = param.args[2] as String?
//                val whereArgs = param.args[3] as Array<*>?
//                log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")

                when (table) {
                    "message" -> values?.apply { // recall message
                        if (!containsKey("type") || values["type"] != 10000) {
                            return
                        }
                        param.result = 1

                        val msgId = values["msgId"] as Long
                        val msg = MessageCache[msgId] ?: return
                        values.put("talker", msg.talker)
                        values.put("createTime", msg.createTime + 1L)

                        val db = param.thisObject
                        callMethod(db, "insert", "message", null, values)
                    }
                    "SnsInfo" -> values?.apply { // delete moment
                        if (!containsKey("sourceType") || values["sourceType"] != 0) {
                            return
                        }
                        val content =  values["content"] as ByteArray?
                        handleMomentDelete(content, values)
                    }
                    "SnsComment" -> values?.apply { // delete moment comment
                        if (!containsKey("type") || values["type"] == 1) {
                            return
                        }
                        if (!containsKey("commentflag") || values["commentflag"] != 1) {
                            return
                        }
                        val curActionBuf = values["curActionBuf"] as ByteArray?
                        handleCommentDelete(curActionBuf, values)
                    }
                }
            }
        })

//        findAndHookMethod(clazz, "delete", C.String, C.String, C.StringArray, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String?
//                val whereClause = param.args[1] as String?
//                val whereArgs = param.args[2] as Array<*>?
//                log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
//            }
//        })

//        findAndHookMethod(clazz, "executeSql", C.String, C.ObjectArray, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val sql = param.args[0] as String?
//                val bindArgs = param.args[1] as Array<*>?
//                log("DB => executeSql sql = $sql, bindArgs = ${MessageUtil.argsToString(bindArgs)}")
//            }
//        })
    }

    // handleMessageRecall notifies user that someone has recalled the message
    private fun handleMessageRecall(origin: WechatMessage, values: ContentValues) {
        // Split speaker and message for chatrooms
        val speaker: String?; var message: String?
        if (origin.talker.contains("chatroom")) {
            val len = (origin.content?.indexOf(":\n") ?: 0) + 2
            speaker = origin.content?.take(len)
            message = origin.content?.drop(len)
        } else {
            speaker = ""; message = origin.content
        }

        // Modify runtime data to notify user
        values.remove("type")
        values.remove("content")
        when (origin.type) {
            1 -> {
                message = MessageUtil.notifyMessageRecall(res.labelRecalled, message!!)
                values.put("content", speaker + message)
            }
            3 -> {
                ImageUtil.replaceThumbnail(origin.imgPath!!, res.bitmapRecalled)
            }
            49 -> {
                message = MessageUtil.notifyLinkRecall(res.labelRecalled, message!!)
                values.put("content", speaker + message)
            }
        }
    }

    // handleMomentDelete notifies user that someone has deleted the given moment
    private fun handleMomentDelete(content: ByteArray?, values: ContentValues) {
        MessageUtil.notifyInfoDelete(res.labelDeleted, content)?.let {
            values.remove("sourceType")
            values.put("content", it)
        }
    }

    // handleCommentDelete notifies user that someone has deleted the given comment in moments
    private fun handleCommentDelete(curActionBuf: ByteArray?, values: ContentValues) {
        MessageUtil.notifyCommentDelete(res.labelDeleted, curActionBuf)?.let {
            values.remove("commentflag")
            values.put("curActionBuf", it)
        }
    }
}