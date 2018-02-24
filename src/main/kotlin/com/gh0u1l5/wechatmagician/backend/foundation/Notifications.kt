package com.gh0u1l5.wechatmagician.backend.foundation

import com.gh0u1l5.wechatmagician.backend.WechatPackage.NotificationAppMsgQueue
import com.gh0u1l5.wechatmagician.backend.WechatPackage.NotificationAppMsgQueueAddMethod
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.INotificationHookRaw
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object Notifications : EventCenter() {
    @JvmStatic fun hookEvents() {
        findAndHookMethod(NotificationAppMsgQueue, NotificationAppMsgQueueAddMethod, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("beforeAddMessageNotification") { plugin ->
                    if (plugin is INotificationHookRaw) {
                        plugin.beforeAddMessageNotification(param)
                    }
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("afterAddMessageNotification") { plugin ->
                    if (plugin is INotificationHookRaw) {
                        plugin.afterAddMessageNotification(param)
                    }
                }
            }
        })
    }
}