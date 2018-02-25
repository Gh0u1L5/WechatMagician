package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface IFileSystemHookRaw {
    // TODO: onXXXDoing and onXXXDone
    fun beforeFileDelete(param: XC_MethodHook.MethodHookParam) { }
    fun afterFileDelete(param: XC_MethodHook.MethodHookParam) { }
    fun beforeFileWrite(param: XC_MethodHook.MethodHookParam) { }
}