package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.booter.notification

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val NotificationItem: Class<*> by wxLazy("NotificationItem") {
        findClassIfExists("$wxPackageName.booter.notification.NotificationItem", wxLoader)
    }
}