package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui

import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Classes.MMBaseAdapter

object Methods {
    val MMBaseAdapter_getItemInternal: String by wxLazy("MMBaseAdapter_getItemInternal") {
        MMBaseAdapter.declaredMethods.filter {
            it.parameterTypes.size == 1 && it.parameterTypes[0] == C.Int
        }.firstOrNull {
            it.name != "getItem" && it.name != "getItemId"
        }?.name
    }
}