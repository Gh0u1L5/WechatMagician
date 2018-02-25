package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.Global.STATUS_FLAG_DATABASE
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SQLiteCancellationSignal
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SQLiteCursorFactory
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SQLiteDatabase
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.SQLiteErrorHandler
import com.gh0u1l5.wechatmagician.spellbook.WechatStatus
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHookRaw
import com.gh0u1l5.wechatmagician.spellbook.util.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Database : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IDatabaseHook::class.java, IDatabaseHookRaw::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(
                SQLiteDatabase, "openDatabase",
                C.String, SQLiteCursorFactory, C.Int, SQLiteErrorHandler, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseOpen") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseOpen(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseOpen") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseOpen(param)
                }

                val path     = param.args[0] as String
                val database = param.result
                notify("onDatabaseOpen") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseOpen(path, database)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "rawQueryWithFactory",
                SQLiteCursorFactory, C.String, C.StringArray, C.String, SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseQuery") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseQuery(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseQuery") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseQuery(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "insertWithOnConflict",
                C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseInsert") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseInsert(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseInsert") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseInsert(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseUpdate") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseUpdate(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseUpdate") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseUpdate(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "delete",
                C.String, C.String, C.StringArray, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseDelete") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseDelete(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseDelete") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseDelete(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "executeSql",
                C.String, C.ObjectArray, SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeDatabaseExecute") { plugin ->
                    (plugin as IDatabaseHookRaw).beforeDatabaseExecute(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterDatabaseExecute") { plugin ->
                    (plugin as IDatabaseHookRaw).afterDatabaseExecute(param)
                }
            }
        })

        WechatStatus.toggle(STATUS_FLAG_DATABASE, true)
    }
}