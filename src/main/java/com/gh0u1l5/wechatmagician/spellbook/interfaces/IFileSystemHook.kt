package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IFileSystemHook {

    fun onFileDeleting(param: XC_MethodHook.MethodHookParam) { }

    fun onFileDeleted(param: XC_MethodHook.MethodHookParam) { }

    fun onFileWriting(param: XC_MethodHook.MethodHookParam) { }
}