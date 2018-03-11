package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.modelsfs

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.modelsfs.Classes.EncEngine
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import java.lang.reflect.Method

object Methods {
    val EncEngine_transFor: Method by wxLazy("EncEngine_transFor") {
        findMethodsByExactParameters(EncEngine, C.Int, C.ByteArray, C.Int).firstOrNull()
    }
}