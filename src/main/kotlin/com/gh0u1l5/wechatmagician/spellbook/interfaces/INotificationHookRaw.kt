package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface INotificationHookRaw {
    fun beforeAddMessageNotification(param: XC_MethodHook.MethodHookParam) { }
    fun afterAddMessageNotification(param: XC_MethodHook.MethodHookParam) { }
}