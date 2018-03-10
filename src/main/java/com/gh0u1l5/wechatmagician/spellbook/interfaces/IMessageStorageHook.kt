package com.gh0u1l5.wechatmagician.spellbook.interfaces

interface IMessageStorageHook {

    fun onMessageStorageCreated(storage: Any) { }

    fun onMessageStorageInserted(msgId: Long, msgObject: Any) { }
}