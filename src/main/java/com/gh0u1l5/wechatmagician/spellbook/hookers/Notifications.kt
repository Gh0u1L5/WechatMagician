package com.gh0u1l5.wechatmagician.spellbook.hookers

import com.gh0u1l5.wechatmagician.spellbook.annotations.WechatHookMethod
import com.gh0u1l5.wechatmagician.spellbook.base.EventCenter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.INotificationHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification.queue.Classes.NotificationAppMsgQueue
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification.queue.Methods.NotificationAppMsgQueue_add
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object Notifications : EventCenter() {

    override val interfaces: List<Class<*>>
        get() = listOf(INotificationHook::class.java)

    @WechatHookMethod @JvmStatic fun hookEvents() {
        findAndHookMethod(NotificationAppMsgQueue, NotificationAppMsgQueue_add, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                notify("onMessageNotificationAdding") { plugin ->
                    (plugin as INotificationHook).onMessageNotificationAdding(param)
                }
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                notify("onMessageNotificationAdded") { plugin ->
                    (plugin as INotificationHook).onMessageNotificationAdded(param)
                }
            }
        })
    }
}