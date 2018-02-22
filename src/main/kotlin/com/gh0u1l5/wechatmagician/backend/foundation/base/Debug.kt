package com.gh0u1l5.wechatmagician.backend.foundation.base

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ListAdapter
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.util.MessageUtil
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File

object Debug {

    private val pkg = WechatPackage
    private val pref = WechatHook.developer

    // Hook View.onTouchEvent to trace touch events.
    @JvmStatic fun traceTouchEvents() {
        if (pref.getBoolean(Global.DEVELOPER_UI_TOUCH_EVENT, false)) {
            XposedHelpers.findAndHookMethod(
                    "android.view.View", pkg.loader,
                    "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("View.onTouchEvent => obj.class = ${param.thisObject.javaClass}")
                }
            })
        }
    }

    // Hook Activity.startActivity and Activity.onCreate to trace activities.
    @JvmStatic fun traceActivities() {
        if (pref.getBoolean(Global.DEVELOPER_UI_TRACE_ACTIVITIES, false)) {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity", pkg.loader,
                    "startActivity", C.Intent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent?
                    XposedBridge.log("Activity.startActivity => " +
                            "${param.thisObject.javaClass}, " +
                            "intent => ${MessageUtil.bundleToString(intent?.extras)}")
                }
            })

            XposedHelpers.findAndHookMethod(
                    "android.app.Activity", pkg.loader,
                    "onCreate", C.Bundle, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val bundle = param.args[0] as Bundle?
                    val intent = (param.thisObject as Activity).intent
                    XposedBridge.log("Activity.onCreate => " +
                            "${param.thisObject.javaClass}, " +
                            "intent => ${MessageUtil.bundleToString(intent?.extras)}, " +
                            "bundle => ${MessageUtil.bundleToString(bundle)}")
                }
            })
        }
    }

    // Hook MMListPopupWindow to trace every popup menu.
    @JvmStatic fun dumpPopupMenu() {
        if (pref.getBoolean(Global.DEVELOPER_UI_DUMP_POPUP_MENU, false)) {
            XposedBridge.hookAllConstructors(pkg.MMListPopupWindow, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val menu = param.thisObject
                    val context = param.args[0]
                    XposedBridge.log("POPUP => menu.class = ${menu.javaClass}")
                    XposedBridge.log("POPUP => context.class = ${context.javaClass}")
                }
            })

            XposedHelpers.findAndHookMethod(
                    pkg.MMListPopupWindow, "setAdapter",
                    C.ListAdapter, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val adapter = param.args[0] as ListAdapter? ?: return
                    XposedBridge.log("POPUP => adapter.count = ${adapter.count}")
                    (0 until adapter.count).forEach { index ->
                        XposedBridge.log("POPUP => adapter.item[$index] = ${adapter.getItem(index)}")
                        XposedBridge.log("POPUP => adapter.item[$index].class = ${adapter.getItem(index).javaClass}")
                    }
                }
            })
        }
    }

    // Hook SQLiteDatabase to trace all the database operations.
    @JvmStatic fun traceDatabase() {
        if (pref.getBoolean(Global.DEVELOPER_DATABASE_QUERY, false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabase, "rawQueryWithFactory",
                    pkg.SQLiteCursorFactory, C.String, C.StringArray, C.String, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[1] as String?
                    val selectionArgs = param.args[2] as Array<*>?
                    XposedBridge.log("DB => query sql = $sql, selectionArgs = ${MessageUtil.argsToString(selectionArgs)}, db = ${param.thisObject}")
                }
            })
        }

        if (pref.getBoolean(Global.DEVELOPER_DATABASE_INSERT, false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabase, "insertWithOnConflict",
                    C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[2] as ContentValues?
                    XposedBridge.log("DB => insert table = $table, values = $values, db = ${param.thisObject}")
                }
            })
        }

        if (pref.getBoolean(Global.DEVELOPER_DATABASE_UPDATE, false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabase, "updateWithOnConflict",
                    C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[1] as ContentValues?
                    val whereClause = param.args[2] as String?
                    val whereArgs = param.args[3] as Array<*>?
                    XposedBridge.log("DB => update " +
                            "table = $table, " +
                            "values = $values, " +
                            "whereClause = $whereClause, " +
                            "whereArgs = ${MessageUtil.argsToString(whereArgs)}, " +
                            "db = ${param.thisObject}")
                }
            })
        }

        if (pref.getBoolean(Global.DEVELOPER_DATABASE_DELETE, false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabase, "delete",
                    C.String, C.String, C.StringArray, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val whereClause = param.args[1] as String?
                    val whereArgs = param.args[2] as Array<*>?
                    XposedBridge.log("DB => delete " +
                            "table = $table, " +
                            "whereClause = $whereClause, " +
                            "whereArgs = ${MessageUtil.argsToString(whereArgs)}, " +
                            "db = ${param.thisObject}")
                }
            })
        }

        if (pref.getBoolean(Global.DEVELOPER_DATABASE_EXECUTE, false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabase, "executeSql",
                    C.String, C.ObjectArray, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[0] as String?
                    val bindArgs = param.args[1] as Array<*>?
                    XposedBridge.log("DB => executeSql sql = $sql, bindArgs = ${MessageUtil.argsToString(bindArgs)}, db = ${param.thisObject}")
                }
            })
        }
    }

    // Hook Log to trace hidden logcat output.
    @JvmStatic fun traceLogCat() {
        if (pref.getBoolean(Global.DEVELOPER_TRACE_LOGCAT, false)) {
            val functions = listOf("d", "e", "f", "i", "v", "w")
            functions.forEach { func ->
                XposedHelpers.findAndHookMethod(
                        pkg.LogCat, func,
                        C.String, C.String, C.ObjectArray, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val tag = param.args[0] as String?
                        val msg = param.args[1] as String?
                        val args = param.args[2] as Array<*>?
                        if (args == null) {
                            XposedBridge.log("LOG.${func.toUpperCase()} => [$tag] $msg")
                        } else {
                            XposedBridge.log("LOG.${func.toUpperCase()} => [$tag] ${msg?.format(*args)}")
                        }
                    }
                })
            }
        }
    }

    // Hook FileInputStream / FileOutputStream to trace file operations.
    @JvmStatic fun traceFiles() {
        if (pref.getBoolean(Global.DEVELOPER_TRACE_FILES, false)) {
            XposedHelpers.findAndHookConstructor(
                    "java.io.FileInputStream", pkg.loader,
                    C.File, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val path = (param.args[0] as File?)?.absolutePath ?: return
                    XposedBridge.log("FILE => Read $path")
                }
            })

            XposedHelpers.findAndHookConstructor(
                    "java.io.FileOutputStream", pkg.loader,
                    C.File, C.Boolean, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val path = (param.args[0] as File?)?.absolutePath ?: return
                    XposedBridge.log("FILE => Write $path")
                }
            })

            XposedHelpers.findAndHookMethod(
                    "java.io.File", pkg.loader, "delete", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val file = param.thisObject as File
                    XposedBridge.log("FILE => Delete ${file.absolutePath}")
                }
            })
        }
    }

    // Hook XML Parser to trace the XML files used in Wechat.
    @JvmStatic fun traceXMLParse() {
        if (pref.getBoolean(Global.DEVELOPER_XML_PARSER, false)) {
            findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val xml = param.args[0] as String?
                    val root = param.args[1] as String?
                    XposedBridge.log("XML => root = $root, xml = $xml")
                }
            })
        }
    }
}