package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IDatabaseHookRaw {

    // SQLiteDatabase.openDatabase(path: String, factory: CursorFactory, flags: Int, errorHandler: SQLiteErrorHandler): SQLiteDatabase
    fun beforeDatabaseOpen(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseOpen(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.rawQueryWithFactory(factory: CursorFactory, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: SQLiteCancellationSignal?): Cursor
    fun beforeDatabaseQuery(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseQuery(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.insertWithOnConflict(table: String, nullColumnHack: String, initialValues: ContentValues?, conflictAlgorithm: Int): Long
    fun beforeDatabaseInsert(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseInsert(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.updateWithOnConflict(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun beforeDatabaseUpdate(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseUpdate(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int
    fun beforeDatabaseDelete(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseDelete(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.executeSql(sql: String, bindArgs: Array<Any>?, cancellationSignal: SQLiteCancellationSignal?): Unit
    fun beforeDatabaseExecute(param: XC_MethodHook.MethodHookParam) { }
    fun afterDatabaseExecute(param: XC_MethodHook.MethodHookParam) { }
}