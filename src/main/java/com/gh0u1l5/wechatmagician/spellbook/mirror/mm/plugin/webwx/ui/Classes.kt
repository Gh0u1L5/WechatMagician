package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.webwx.ui

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val WebWXLoginUI: Class<*> by wxLazy("WebWXLoginUI") {
        findClassIfExists("$wxPackageName.plugin.webwx.ui.ExtDeviceWXLoginUI", wxLoader)
    }
}