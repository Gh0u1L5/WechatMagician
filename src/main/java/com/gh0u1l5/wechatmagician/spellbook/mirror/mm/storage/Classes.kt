package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.storage

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxVersion
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage
import com.gh0u1l5.wechatmagician.spellbook.base.Version

object Classes {
    private val classesInCurrentPackage by wxLazy("$wxPackageName.storage") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.storage")
    }

    val MsgInfo: Class<*> by wxLazy("MsgInfo") {
        classesInCurrentPackage
                .filterByMethod(C.Boolean, "isSystem")
                .firstOrNull()
    }

    val MsgInfoStorage: Class<*> by wxLazy("MsgInfoStorage") {
        when {
            wxVersion!! >= Version("6.5.8") ->
                classesInCurrentPackage
                        .filterByMethod(C.Long, MsgInfo, C.Boolean)
                        .firstOrNull()
            else ->
                classesInCurrentPackage
                        .filterByMethod(C.Long, MsgInfo)
                        .firstOrNull()
        }
    }

    val ContactInfo: Class<*> by wxLazy("ContactInfo") {
        classesInCurrentPackage
                .filterByMethod(C.String, "getCityCode")
                .filterByMethod(C.String, "getCountryCode")
                .firstOrNull()
    }
}