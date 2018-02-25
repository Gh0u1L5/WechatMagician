package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IDatabaseHook {

    // SQLiteDatabase.openDatabase(path: String, factory: CursorFactory, flags: Int, errorHandler: SQLiteErrorHandler): SQLiteDatabase
    fun onDatabaseOpening(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseOpened(path: String, database: Any) { }

    // SQLiteDatabase.rawQueryWithFactory(factory: CursorFactory, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: SQLiteCancellationSignal?): Cursor
    fun onDatabaseQuerying(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseQueried(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.insertWithOnConflict(table: String, nullColumnHack: String, initialValues: ContentValues?, conflictAlgorithm: Int): Long
    fun onDatabaseInserting(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseInserted(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.updateWithOnConflict(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun onDatabaseUpdating(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseUpdated(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int
    fun onDatabaseDeleting(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseDeleted(param: XC_MethodHook.MethodHookParam) { }

    // SQLiteDatabase.executeSql(sql: String, bindArgs: Array<Any>?, cancellationSignal: SQLiteCancellationSignal?): Unit
    fun onDatabaseExecuting(param: XC_MethodHook.MethodHookParam) { }
    fun onDatabaseExecuted(param: XC_MethodHook.MethodHookParam) { }
}