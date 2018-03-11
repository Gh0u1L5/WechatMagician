package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage

object Classes {
    private val classesInCurrentPackage by wxLazy("$wxPackageName.sdk.platformtools") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.sdk.platformtools")
    }

    val Logcat: Class<*> by wxLazy("Logcat") {
        classesInCurrentPackage
                .filterByEnclosingClass(null)
                .filterByMethod(C.Int, "getLogLevel")
                .firstOrNull()
    }

    val LruCache: Class<*> by wxLazy("LruCache") {
        classesInCurrentPackage
                .filterByMethod(null, "trimToSize", C.Int)
                .firstOrNull()
    }

    val XmlParser: Class<*> by wxLazy("XmlParser") {
        classesInCurrentPackage
                .filterByMethod(C.Map, C.String, C.String)
                .firstOrNull()
    }
}