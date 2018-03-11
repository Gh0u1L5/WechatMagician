package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification.queue

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification.Classes.NotificationItem
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification.queue.Classes.NotificationAppMsgQueue
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import java.lang.reflect.Method

object Methods {
    val NotificationAppMsgQueue_add: Method by wxLazy("NotificationAppMsgQueue_add") {
        findMethodsByExactParameters(NotificationAppMsgQueue, null, NotificationItem).firstOrNull()
    }
}