package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.base

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val MMListPopupWindow: Class<*> by wxLazy("MMListPopupWindow") {
        findClassIfExists("$wxPackageName.ui.base.MMListPopupWindow", wxLoader)
    }
}