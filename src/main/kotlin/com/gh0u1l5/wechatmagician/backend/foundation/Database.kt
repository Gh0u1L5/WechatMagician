package com.gh0u1l5.wechatmagician.backend.foundation

import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IDatabaseHookRaw
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Database : EventCenter() {

    private val pkg = WechatPackage

    @JvmStatic fun hookEvents() {
        findAndHookMethod(
                pkg.SQLiteDatabase, "openDatabase",
                C.String, pkg.SQLiteCursorFactory, C.Int, pkg.SQLiteErrorHandler, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseOpen") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseOpen(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseOpen") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseOpen(param)
                    }
                }
                notify("onDatabaseOpen") { plugin ->
                    if (plugin is IDatabaseHook) {
                        val path     = param.args[0] as String
                        val database = param.result
                        plugin.onDatabaseOpen(path, database)
                    }
                }
            }
        })

        findAndHookMethod(
                pkg.SQLiteDatabase, "rawQueryWithFactory",
                pkg.SQLiteCursorFactory, C.String, C.StringArray, C.String, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseQuery") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseQuery(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseQuery") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseQuery(param)
                    }
                }
            }
        })

        findAndHookMethod(
                pkg.SQLiteDatabase, "insertWithOnConflict",
                C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseInsert") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseInsert(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseInsert") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseInsert(param)
                    }
                }
            }
        })

        findAndHookMethod(
                pkg.SQLiteDatabase, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseUpdate") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseUpdate(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseUpdate") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseUpdate(param)
                    }
                }
            }
        })

        findAndHookMethod(
                pkg.SQLiteDatabase, "delete",
                C.String, C.String, C.StringArray, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseDelete") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseDelete(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseDelete") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseDelete(param)
                    }
                }
            }
        })

        findAndHookMethod(
                pkg.SQLiteDatabase, "executeSql",
                C.String, C.ObjectArray, pkg.SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseExecute") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.beforeDatabaseExecute(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseExecute") { plugin ->
                    if (plugin is IDatabaseHookRaw) {
                        plugin.afterDatabaseExecute(param)
                    }
                }
            }
        })

        pkg.setStatus(STATUS_FLAG_DATABASE, true)
    }

}