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

        try {
            WechatPackage.init(param)
            loader = param.classLoader
        } catch (e: Throwable) {
            log("INIT => ${e.message}")
            return
        }

//        tryHook(this::hookUIEvents, {})

        tryHook(this::hookOptionsMenu, {
            pkg.MMActivity = null
        })

        tryHook(this::hookAlbumPreviewUI, {
            pkg.AlbumPreviewUI = null
        })
        tryHook(this::hookSelectContactUI, {
            pkg.SelectContactUI = null
        })
        tryHook(this::hookSelectConversationUI, {
            pkg.SelectConversationUI = null
        })

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

    private fun hookUIEvents() {
        // Hook Activity.onCreate to help analyze activities.
        findAndHookMethod(pkg.MMFragmentActivity, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val obj = param.thisObject
                val intent = callMethod(obj, "getIntent") as Intent?
                val extras =  intent?.extras
                log("Activity.onCreate => ${obj.javaClass}, intent => ${extras?.keySet()?.map{"$it = ${extras[it]}"}}")
            }
        })

        // Hook View.onTouchEvent to help analyze UI objects.
        findAndHookMethod("android.view.View", loader, "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val obj = param.thisObject
                log("View.onTouchEvent => obj.class = ${obj.javaClass}")
            }
        })
    }

    private fun hookOptionsMenu() {
        if (pkg.MMActivity == null) {
            return
        }

        // Hook onCreateOptionsMenu to add new buttons in the OptionsMenu.
        findAndHookMethod(pkg.MMActivity, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
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

    private fun hookAlbumPreviewUI() {
        if (pkg.AlbumPreviewUI == null) {
            return
        }

        // Hook AlbumPreviewUI to bypass the limit on number of selected photos.
        findAndHookMethod(pkg.AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val obj = param.thisObject
                val intent = callMethod(obj, "getIntent") as Intent? ?: return
                if (intent.getIntExtra("max_select_count", -1) == 9) {
                    intent.putExtra("max_select_count", 1000)
                }
            }
        })
    }

    private fun hookSelectContactUI() {
        if (pkg.SelectContactUI == null) {
            return
        }

        // Hook SelectContactUI to help the "Select All" button.
        findAndHookMethod(pkg.SelectContactUI, "onActivityResult", C.Int, C.Int, C.Intent, object : XC_MethodHook() {
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

        // Hook SelectContactUI to bypass the limit on number of recipients.
        findAndHookMethod(pkg.SelectContactUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val obj = param.thisObject
                val intent = callMethod(obj, "getIntent") as Intent? ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        })
    }

    private fun hookSelectConversationUI() {
        if (pkg.SelectConversationUI == null || pkg.SelectConversationUIMaxLimitMethod == "") {
            return
        }

        // Hook SelectConversationUI to bypass the limit on number of recipients.
        findAndHookMethod(pkg.SelectConversationUI, pkg.SelectConversationUIMaxLimitMethod, C.Boolean, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }

    private fun hookMsgStorage() {
        if (pkg.MsgStorageClass == null || pkg.MsgStorageInsertMethod == "") {
            return
        }

        // Analyze dynamically to find the global message storage instance
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
                val msg = param.args[0]
                val msgId = getLongField(msg, "field_msgId")
                MessageCache[msgId] = msg
            }
        })
    }

    private fun hookImgStorage() {
        if (pkg.ImgStorageClass == null) {
            return
        }

        // Analyze dynamically to find the global image storage instance
        hookAllConstructors(pkg.ImgStorageClass, object : XC_MethodHook() {
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
        findAndHookConstructor("java.io.FileOutputStream", loader, C.File, C.Boolean, object : XC_MethodHook() {
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
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == "") {
            return
        }

        // Hook XML Parser for the status bar easter egg
        findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, C.String, C.String, object : XC_MethodHook() {
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
        if (pkg.SQLiteDatabaseClass == null){
            return
        }


//        findAndHookMethod(pkg.SQLiteDatabaseClass, "insertWithOnConflict", C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String? ?: return
//                val values = param.args[2] as ContentValues? ?: return
//                log("DB => insert table = $table, values = $values")
//            }
//        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments
        findAndHookMethod(pkg.SQLiteDatabaseClass, "updateWithOnConflict", C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
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
//        findAndHookMethod(pkg.SQLiteDatabaseClass, "rawQueryWithFactory", typeCursorFactory, C.String, C.StringArray, C.String, typeCancellationSignal, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val sql = param.args[1] as String? ?: return
//                val selectionArgs = param.args[2] as Array<*>?
//                log("DB => sql = $sql, selectionArgs = ${MessageUtil.argsToString(selectionArgs)}")
//            }
//        })

//        findAndHookMethod(pkg.SQLiteDatabaseClass, "delete", C.String, C.String, C.StringArray, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String?
//                val whereClause = param.args[1] as String?
//                val whereArgs = param.args[2] as Array<*>?
//                log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${MessageUtil.argsToString(whereArgs)}")
//            }
//        })

//        findAndHookMethod(pkg.SQLiteDatabaseClass, "executeSql", C.String, C.ObjectArray, object : XC_MethodHook() {
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

    // dumpViewGroup dumps the structure of a view group.
    private fun dumpViewGroup(prefix: String, viewGroup: ViewGroup) {
        repeat(viewGroup.childCount, {
            var attrs = mapOf<String, Any>()
            val child = viewGroup.getChildAt(it)

            val getAttr = {getter: String ->
                if (child.javaClass.methods.count{ it.name == getter } != 0) {
                    attrs += getter to callMethod(child, getter)
                }
            }
            getAttr("getText")
            getAttr("isClickable")

            log("$prefix[$it] => ${child.javaClass}, $attrs")
            if (child is ViewGroup) {
                dumpViewGroup("$prefix[$it]", child)
            }
        })
    }
}