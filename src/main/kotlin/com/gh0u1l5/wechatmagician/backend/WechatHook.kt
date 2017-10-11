package com.gh0u1l5.wechatmagician.backend

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.storage.MessageCache
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import com.gh0u1l5.wechatmagician.storage.SnsCache
import com.gh0u1l5.wechatmagician.util.ImageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.MessageUtil.bundleToString
import com.gh0u1l5.wechatmagician.util.PackageUtil.shadowCopy
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedBridge.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.*
import kotlin.concurrent.thread

// WechatHook contains the entry points and all the hooks.
class WechatHook : IXposedHookLoadPackage {

    private val pkg = WechatPackage
    private val res = LocalizedResources
    private val listeners = WechatListeners
    private lateinit var loader: ClassLoader

    // Hook for hacking Wechat application.
    // NOTE: Remember to catch all the exceptions here, otherwise you may get boot loop.
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != "com.tencent.mm") {
            return
        }

        try {
            pkg.init(param)
            loader = param.classLoader
            val process = param.processName
            if (process == "com.tencent.mm") {
                thread(start = true) {
                    pkg.dumpPackage()
                }
            }
        } catch (e: Throwable) {
            log("INIT => ${e.message}")
            return
        }

        // Hooks for Debug
//        tryHook(this::hookTouchEvents, {})
//        tryHook(this::hookCreateActivity, {})
//        tryHook(this::hookXLogSetup, {
//            pkg.XLogSetup = null
//        })

        // Hooks for SNS
        tryHook(this::hookSnsItemLongPress, {
            pkg.AdFrameLayout = null
        })
        tryHook(this::hookSnsUploadUI, {
            pkg.SnsUploadUI = null
        })

        // Hooks for Selecting
        tryHook(this::hookAlbumPreviewUI, {
            pkg.AlbumPreviewUI = null
        })
        tryHook(this::hookSelectContactUI, {
            pkg.SelectContactUI = null
        })
        tryHook(this::hookSelectConversationUI, {
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

    private fun hookTouchEvents() {
        // Hook View.onTouchEvent to help analyze UI layouts.
        findAndHookMethod("android.view.View", loader, "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                log("View.onTouchEvent => obj.class = ${param.thisObject.javaClass}")
            }
        })

        HookStatus += "TouchEvents"
    }

    private fun hookCreateActivity() {
        // Hook Activity.startActivity to trace source activities.
        findAndHookMethod("android.app.Activity", loader, "startActivity", C.Intent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent?
                log("Activity.startActivity => ${param.thisObject.javaClass}, intent => ${bundleToString(intent?.extras)}")
            }
        })

        // Hook Activity.onCreate to trace target activities.
        findAndHookMethod("android.app.Activity", loader, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val bundle = param.args[0] as Bundle?
                val intent = (param.thisObject as Activity).intent
                log("Activity.onCreate => ${param.thisObject.javaClass}, intent => ${bundleToString(intent?.extras)}, bundle => ${bundleToString(bundle)}")
            }
        })

        HookStatus += "CreateActivity"
    }

    private fun hookXLogSetup() {
        if (pkg.XLogSetup == null) {
            return
        }

        // Hook XLog to print internal errors into logcat.
        XposedBridge.hookAllMethods(pkg.XLogSetup, "keep_setupXLog", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.args[5] = true // enable logcat output
            }
        })

        HookStatus += "XLogSetup"
    }

    private fun hookSnsItemLongPress() {
        if (pkg.AdFrameLayout == null) {
            return
        }

        // Hook AdFrameLayout constructors to add onLongClickListeners.
        findAndHookConstructor(pkg.AdFrameLayout, C.Context, C.AttributeSet, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val layout = param.thisObject as FrameLayout
                layout.isLongClickable = true
                layout.setOnLongClickListener { false }
            }
        })

        // Hook AdFrameLayout.setOnLongClickListener to prevent someone else from overwriting the listener.
        findAndHookMethod("android.view.View", loader, "setOnLongClickListener", C.ViewOnLongClickListener, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == pkg.AdFrameLayout) {
                    param.args[0] = listeners.onAdFrameLongClickListener(param.thisObject)
                }
            }
        })

        HookStatus += "SnsItemLongPress"
    }

    private fun hookSnsUploadUI() {
        if (pkg.SnsUploadUI == null || pkg.SnsUploadUIEditTextField == "") {
            return
        }

        // Hook SnsUploadUI.onPause to destroy the activity properly when forwarding moments.
        findAndHookMethod(pkg.SnsUploadUI, "onPause", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent
                if (intent?.extras?.getBoolean("Ksnsforward") == true) {
                    val editText = getObjectField(
                            param.thisObject, pkg.SnsUploadUIEditTextField
                    )
                    callMethod(editText, "setText", "")
                }
            }
        })

        HookStatus += "SnsUploadUI"
    }

    private fun hookAlbumPreviewUI() {
        if (pkg.AlbumPreviewUI == null) {
            return
        }

        // Hook AlbumPreviewUI to bypass the limit on number of selected photos.
        findAndHookMethod(pkg.AlbumPreviewUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                val max = intent.getIntExtra("max_select_count", 9)
                if (max <= 9) {
                    intent.putExtra("max_select_count", max + 991)
                }
            }
        })

        HookStatus += "AlbumPreviewUI"
    }

    private fun hookSelectContactUI() {
        if (pkg.MMActivity == null || pkg.SelectContactUI == null) {
            return
        }

        // Hook MMActivity.onCreateOptionsMenu to add "Select All" button.
        findAndHookMethod(pkg.MMActivity, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass != pkg.SelectContactUI) {
                    return
                }

                val menu = param.args[0] as Menu? ?: return
                val activity = param.thisObject as Activity
                val checked = activity.intent?.getBooleanExtra(
                        "select_all_checked", false
                ) ?: false

                val selectAll = menu.add(0, 2, 0, "")
                selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                if (WechatResHook.MODULE_RES == null) {
                    selectAll.isChecked = checked
                    selectAll.title = res["button_select_all"] + "  " +
                            if (checked) "\u2611" else "\u2610"
                    selectAll.setOnMenuItemClickListener {
                        listeners.onSelectContactUISelectAll(activity, !selectAll.isChecked); true
                    }
                } else {
                    val checkedTextView = activity.layoutInflater.inflate(
                            WechatResHook.MODULE_RES?.getLayout(R.layout.wechat_checked_textview), null
                    )
                    checkedTextView.findViewById<TextView>(R.id.ctvTextView).apply {
                        setTextColor(Color.WHITE)
                        text = res["button_select_all"]
                    }
                    checkedTextView.findViewById<CheckBox>(R.id.ctvCheckBox).apply {
                        isChecked = checked
                        setOnCheckedChangeListener {_, checked ->
                            listeners.onSelectContactUISelectAll(activity, checked)
                        }
                    }
                    selectAll.actionView = checkedTextView
                }
            }
        })

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
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getIntExtra("max_limit_num", -1) == 9) {
                    intent.putExtra("max_limit_num", 0x7FFFFFFF)
                }
            }
        })

        HookStatus += "SelectContactUI"
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

        HookStatus += "SelectConversationUI"
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
//                val xml = param.args[0] as String?
//                val tag = param.args[1] as String?
//                log("XML => xml = $xml, tag = $tag")

                @Suppress("UNCHECKED_CAST")
                val result = param.result as MutableMap<String, String?>? ?: return
                if (result[".sysmsg.\$type"] == "revokemsg") {
                    val msgTag = ".sysmsg.revokemsg.replacemsg"
                    val msg = result[msgTag] ?: return
                    if (!msg.startsWith("\"")) {
                        return
                    }
                    result[msgTag] = MessageUtil.applyEasterEgg(msg, res["label_easter_egg"])
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

        HookStatus += "XMLParse"
    }

    private fun hookDatabase() {
        if (pkg.SQLiteDatabaseClass == null){
            return
        }

        val typeSQLiteCipherSpec = findClass("com.tencent.wcdb.database.SQLiteCipherSpec", loader)
        val typeCursorFactory = findClass("com.tencent.wcdb.database.SQLiteDatabase.CursorFactory", loader)
        val typeDatabaseErrorHandler = findClass("com.tencent.wcdb.DatabaseErrorHandler", loader)
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

//        findAndHookMethod(pkg.SQLiteDatabaseClass, "insertWithOnConflict", C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
//            @Throws(Throwable::class)
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val table = param.args[0] as String? ?: return
//                val values = param.args[2] as ContentValues? ?: return
//                log("DB => insert table = $table, values = $values")
//            }
//        })

        // Hook SQLiteDatabase.update to prevent Wechat from recalling messages or deleting moments.
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
                        if (!values.getAsString("content").startsWith("\"")) {
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
