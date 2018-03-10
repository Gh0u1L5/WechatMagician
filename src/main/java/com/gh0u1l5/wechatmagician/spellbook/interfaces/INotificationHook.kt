package com.gh0u1l5.wechatmagician.spellbook.interfaces

import de.robv.android.xposed.XC_MethodHook

interface INotificationHook {

    fun onMessageNotificationAdding(param: XC_MethodHook.MethodHookParam) { }

    fun onMessageNotificationAdded(param: XC_MethodHook.MethodHookParam) { }
}