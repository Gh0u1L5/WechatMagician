package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.base.stub

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.base.stub.Classes.WXCustomScheme
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import java.lang.reflect.Method

object Methods {
    val WXCustomScheme_entry: Method by wxLazy("WXCustomScheme_entry") {
        findMethodsByExactParameters(WXCustomScheme, C.Boolean, C.Intent).firstOrNull()
    }
}