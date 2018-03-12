package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.remittance.ui

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findClassIfExists

object Classes {
    val RemittanceAdapter: Class<*> by wxLazy("RemittanceAdapter") {
        findClassIfExists("$wxPackageName.plugin.remittance.ui.RemittanceAdapterUI", wxLoader)
    }
}