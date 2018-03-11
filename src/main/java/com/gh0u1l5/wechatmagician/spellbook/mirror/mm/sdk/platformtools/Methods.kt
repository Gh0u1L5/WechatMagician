package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools.Classes.XmlParser
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import java.lang.reflect.Method

object Methods {
    val XmlParser_parse: Method by wxLazy("XmlParser_parse") {
        findMethodsByExactParameters(XmlParser, C.Map, C.String, C.String).firstOrNull()
    }
}