package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.NotificationAppMsgQueue
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.NotificationAppMsgQueueAddMethod
import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.hookers.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.INotificationHookRaw
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object Notifications : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(INotificationHookRaw::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
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