package com.gh0u1l5.wechatmagician.spellbook.interfaces

interface IDatabaseHook {

    // IDatabaseHookRaw.afterDatabaseOpen(param: MethodHookParam)
    //     path     => param.args[0] as String
    //     database => param.result
    fun onDatabaseOpen(path: String, database: Any) { }
}