package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.storage

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxVersion
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.storage.Classes.MsgInfo
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.storage.Classes.MsgInfoStorage
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findMethodsByExactParameters
import com.gh0u1l5.wechatmagician.spellbook.base.Version
import java.lang.reflect.Method

object Methods {
    val MsgInfoStorage_insert: Method by wxLazy("MsgInfoStorage_insert") {
        when {
            wxVersion!! >= Version("6.5.8") ->
                findMethodsByExactParameters(MsgInfoStorage, C.Long, MsgInfo, C.Boolean).firstOrNull()
            else ->
                findMethodsByExactParameters(MsgInfoStorage, C.Long, MsgInfo).firstOrNull()
        }
    }
}