package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString
import com.gh0u1l5.wechatmagician.util.MessageUtil.bundleToString
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class Developer(private val loader: ClassLoader, private val preferences: Preferences) {

    private val pkg = WechatPackage

    // Hook View.onTouchEvent to trace touch events.
    fun traceTouchEvents() {
        if (preferences.getBoolean("developer_ui_touch_event", false)) {
            XposedHelpers.findAndHookMethod(
                    "android.view.View", loader,
                    "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("View.onTouchEvent => obj.class = ${param.thisObject.javaClass}")
                }
            })
        }
    }

    // Hook Activity.startActivity and Activity.onCreate to trace activities.
    fun traceActivities() {
        if (preferences.getBoolean("developer_ui_trace_activities", false)) {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity", loader,
                    "startActivity", C.Intent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent?
                    XposedBridge.log("Activity.startActivity => ${param.thisObject.javaClass}, intent => ${bundleToString(intent?.extras)}")
                }
            })

            XposedHelpers.findAndHookMethod(
                    "android.app.Activity", loader,
                    "onCreate", C.Bundle, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val bundle = param.args[0] as Bundle?
                    val intent = (param.thisObject as Activity).intent
                    XposedBridge.log("Activity.onCreate => ${param.thisObject.javaClass}, intent => ${bundleToString(intent?.extras)}, bundle => ${bundleToString(bundle)}")
                }
            })
        }
    }

    // Hook XLog to print internal errors into logcat.
    fun enableXLog() {
        if (pkg.XLogSetup == null) {
            return
        }

        if (preferences.getBoolean("developer_ui_xlog", false)) {
            XposedBridge.hookAllMethods(
                    pkg.XLogSetup, "keep_setupXLog", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[5] = true // enable logcat output
                }
            })
        }
    }

    // Hook XML Parser to trace the XML files used in Wechat.
    fun traceXMLParse() {
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == "") {
            return
        }

        if (preferences.getBoolean("developer_xml_parser", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.XMLParserClass, pkg.XMLParseMethod,
                    C.String, C.String, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val xml = param.args[0] as String?
                    val tag = param.args[1] as String?
                    XposedBridge.log("XML => xml = $xml, tag = $tag")
                }
            })
        }
    }

    // Hook SQLiteDatabase to trace all the database operations.
    fun traceDatabase() {
        when (null) {
            pkg.SQLiteDatabaseClass,
            pkg.SQLiteCursorFactory,
            pkg.SQLiteCancellationSignal -> return
        }

        if (preferences.getBoolean("developer_database_query", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabaseClass, "rawQueryWithFactory",
                    pkg.SQLiteCursorFactory, C.String, C.StringArray, C.String, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[1] as String?
                    val selectionArgs = param.args[2] as Array<*>?
                    XposedBridge.log("DB => query sql = $sql, selectionArgs = ${argsToString(selectionArgs)}")
                }
            })
        }

        if (preferences.getBoolean("developer_database_insert", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabaseClass, "insertWithOnConflict",
                    C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[2] as android.content.ContentValues?
                    XposedBridge.log("DB => insert table = $table, values = $values")
                }
            })
        }

        if (preferences.getBoolean("developer_database_update", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabaseClass, "updateWithOnConflict",
                    C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[1] as android.content.ContentValues?
                    val whereClause = param.args[2] as String?
                    val whereArgs = param.args[3] as Array<*>?
                    XposedBridge.log("DB => update table = $table, values = $values, whereClause = $whereClause, whereArgs = ${argsToString(whereArgs)}")
                }
            })
        }

        if (preferences.getBoolean("developer_database_delete", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabaseClass, "delete",
                    C.String, C.String, C.StringArray, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val whereClause = param.args[1] as String?
                    val whereArgs = param.args[2] as Array<*>?
                    XposedBridge.log("DB => delete table = $table, whereClause = $whereClause, whereArgs = ${argsToString(whereArgs)}")
                }
            })
        }

        if (preferences.getBoolean("developer_database_execute", false)) {
            XposedHelpers.findAndHookMethod(
                    pkg.SQLiteDatabaseClass, "executeSql",
                    C.String, C.ObjectArray, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[0] as String?
                    val bindArgs = param.args[1] as Array<*>?
                    XposedBridge.log("DB => executeSql sql = $sql, bindArgs = ${argsToString(bindArgs)}")
                }
            })
        }
    }
}