package com.gh0u1l5.wechatmagician.spellbook.mirror.mm

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools.Classes.LruCache
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage

object Classes {
    private val classesInDepthOne by wxLazy("MM packages on depth one") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, wxPackageName, 1)
    }

    val ImgInfoStorage: Class<*> by wxLazy("ImgInfoStorage") {
        classesInDepthOne
                .filterByMethod(C.String, C.String, C.String, C.String, C.Boolean)
                .firstOrNull()
    }

    val LruCacheWithListener: Class<*> by wxLazy("LruCacheWithListener") {
        classesInDepthOne
                .filterBySuper(LruCache)
                .firstOrNull()
    }
}