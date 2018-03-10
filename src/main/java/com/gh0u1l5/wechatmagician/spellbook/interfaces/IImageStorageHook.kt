package com.gh0u1l5.wechatmagician.spellbook.interfaces

interface IImageStorageHook {

    fun onImageStorageCreated(storage: Any) { }

    fun onImageStorageLoaded(imageId: String?, prefix: String?, suffix: String?) { }
}