package com.gh0u1l5.wechatmagician.spellbook.interfaces

interface IMessageStorageHook {
    // TODO: onXXXCreated or onCreateXXX?
    fun onMessageStorageCreated(storage: Any) { }
    fun onMessageStorageInsert(msgId: Long, msgObject: Any) { }
}