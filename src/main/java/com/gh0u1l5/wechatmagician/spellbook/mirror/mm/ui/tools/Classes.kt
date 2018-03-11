package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.tools

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val ActionBarEditText: Class<*> by wxLazy("ActionBarEditText") {
        findClassIfExists("$wxPackageName.ui.tools.ActionBarSearchView.ActionBarEditText", wxLoader)
    }
}