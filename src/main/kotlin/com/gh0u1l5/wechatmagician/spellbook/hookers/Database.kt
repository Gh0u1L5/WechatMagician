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
import com.gh0u1l5.wechatmagician.spellbook.util.C
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object Database : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(IDatabaseHook::class.java, IDatabaseHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(
                SQLiteDatabase, "openDatabase",
                C.String, SQLiteCursorFactory, C.Int, SQLiteErrorHandler, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseOpening") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseOpening(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                val path     = param.args[0] as String
                val database = param.result
                notify("onDatabaseOpen") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseOpened(path, database)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "rawQueryWithFactory",
                SQLiteCursorFactory, C.String, C.StringArray, C.String, SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseQuerying") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseQuerying(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onDatabaseQueried") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseQueried(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "insertWithOnConflict",
                C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseInserting") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseInserting(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onDatabaseInserted") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseInserted(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseUpdating") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseUpdating(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onDatabaseUpdated") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseUpdated(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "delete",
                C.String, C.String, C.StringArray, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseDeleting") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseDeleting(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onDatabaseDeleted") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseDeleted(param)
                }
            }
        })

        findAndHookMethod(
                SQLiteDatabase, "executeSql",
                C.String, C.ObjectArray, SQLiteCancellationSignal, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onDatabaseExecuting") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseExecuting(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onDatabaseExecuted") { plugin ->
                    (plugin as IDatabaseHook).onDatabaseExecuted(param)
                }
            }
        })

        WechatStatus.toggle(STATUS_FLAG_DATABASE, true)
    }
}