package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.transmit

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.transmit.Classes.SelectConversationUI
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import java.lang.reflect.Method

object Methods {
    val SelectConversationUI_checkLimit: Method by wxLazy("SelectConversationUI_checkLimit") {
        findMethodsByExactParameters(SelectConversationUI, C.Boolean, C.Boolean).firstOrNull()
    }
}