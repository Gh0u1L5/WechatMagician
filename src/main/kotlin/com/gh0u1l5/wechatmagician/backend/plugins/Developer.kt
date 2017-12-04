package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.util.MessageUtil.argsToString
import com.gh0u1l5.wechatmagician.util.MessageUtil.bundleToString
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Developer {

    private var loader: ClassLoader? = null
    private var preferences: Preferences? = null

    @JvmStatic fun init(_loader: ClassLoader, _preferences: Preferences) {
        loader = _loader
        preferences = _preferences
    }

    private val pkg = WechatPackage

    // Hook View.onTouchEvent to trace touch events.
    @JvmStatic fun traceTouchEvents() {
        if (preferences!!.getBoolean("developer_ui_touch_event", false)) {
            findAndHookMethod(
                    "android.view.View", loader,
                    "onTouchEvent", C.MotionEvent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    log("View.onTouchEvent => obj.class = ${param.thisObject.javaClass}")
                }
            })
        }
    }

    // Hook Activity.startActivity and Activity.onCreate to trace activities.
    @JvmStatic fun traceActivities() {
        if (preferences!!.getBoolean("developer_ui_trace_activities", false)) {
            findAndHookMethod(
                    "android.app.Activity", loader,
                    "startActivity", C.Intent, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent?
                    log("Activity.startActivity => " +
                            "${param.thisObject.javaClass}, " +
                            "intent => ${bundleToString(intent?.extras)}")
                }
            })

            findAndHookMethod(
                    "android.app.Activity", loader,
                    "onCreate", C.Bundle, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val bundle = param.args[0] as Bundle?
                    val intent = (param.thisObject as Activity).intent
                    log("Activity.onCreate => " +
                            "${param.thisObject.javaClass}, " +
                            "intent => ${bundleToString(intent?.extras)}, " +
                            "bundle => ${bundleToString(bundle)}")
                }
            })
        }
    }

    // Hook XLog to print internal errors into logcat.
    @JvmStatic fun enableXLog() {
        if (pkg.XLogSetup == null) {
            return
        }

        if (preferences!!.getBoolean("developer_ui_xlog", false)) {
            hookAllMethods(
                    pkg.XLogSetup, "keep_setupXLog", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[5] = true // enable logcat output
                }
            })
        }
    }

    // Hook XML Parser to trace the XML files used in Wechat.
    @JvmStatic fun traceXMLParse() {
        if (pkg.XMLParserClass == null || pkg.XMLParseMethod == null) {
            return
        }

        if (preferences!!.getBoolean("developer_xml_parser", false)) {
            findAndHookMethod(pkg.XMLParserClass, pkg.XMLParseMethod, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val xml = param.args[0] as String?
                    val tag = param.args[1] as String?
                    log("XML => xml = $xml, tag = $tag")
                }
            })
        }
    }

    // Hook SQLiteDatabase to trace all the database operations.
    @JvmStatic fun traceDatabase() {
        when (null) {
            pkg.SQLiteDatabase,
            pkg.SQLiteCursorFactory,
            pkg.SQLiteCancellationSignal -> return
        }

        if (preferences!!.getBoolean("developer_database_query", false)) {
            findAndHookMethod(
                    pkg.SQLiteDatabase, "rawQueryWithFactory",
                    pkg.SQLiteCursorFactory, C.String, C.StringArray, C.String, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[1] as String?
                    val selectionArgs = param.args[2] as Array<*>?
                    log("DB => query sql = $sql, selectionArgs = ${argsToString(selectionArgs)}")
                }
            })
        }

        if (preferences!!.getBoolean("developer_database_insert", false)) {
            findAndHookMethod(
                    pkg.SQLiteDatabase, "insertWithOnConflict",
                    C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[2] as ContentValues?
                    log("DB => insert table = $table, values = $values")
                }
            })
        }

        if (preferences!!.getBoolean("developer_database_update", false)) {
            findAndHookMethod(
                    pkg.SQLiteDatabase, "updateWithOnConflict",
                    C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val values = param.args[1] as ContentValues?
                    val whereClause = param.args[2] as String?
                    val whereArgs = param.args[3] as Array<*>?
                    log("DB => update " +
                            "table = $table, " +
                            "values = $values, " +
                            "whereClause = $whereClause, " +
                            "whereArgs = ${argsToString(whereArgs)}")
                }
            })
        }

        if (preferences!!.getBoolean("developer_database_delete", false)) {
            findAndHookMethod(
                    pkg.SQLiteDatabase, "delete",
                    C.String, C.String, C.StringArray, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val table = param.args[0] as String?
                    val whereClause = param.args[1] as String?
                    val whereArgs = param.args[2] as Array<*>?
                    log("DB => delete " +
                            "table = $table, " +
                            "whereClause = $whereClause, " +
                            "whereArgs = ${argsToString(whereArgs)}")
                }
            })
        }

        if (preferences!!.getBoolean("developer_database_execute", false)) {
            findAndHookMethod(
                    pkg.SQLiteDatabase, "executeSql",
                    C.String, C.ObjectArray, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val sql = param.args[0] as String?
                    val bindArgs = param.args[1] as Array<*>?
                    log("DB => executeSql sql = $sql, bindArgs = ${argsToString(bindArgs)}")
                }
            })
        }
    }
}