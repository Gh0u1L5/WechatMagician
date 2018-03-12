package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.sns.ui

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findClassIfExists
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findClassesFromPackage

object Classes {
    private val classesInCurrentPackage by wxLazy("$wxPackageName.plugin.sns.ui") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.plugin.sns.ui")
    }

    val SnsActivity: Class<*> by wxLazy("SnsActivity") {
        classesInCurrentPackage
                .filterByField("$wxPackageName.ui.base.MMPullDownView")
                .firstOrNull()
    }

    val SnsTimeLineUI: Class<*> by wxLazy("SnsTimeLineUI") {
        classesInCurrentPackage
                .filterByField("android.support.v7.app.ActionBar")
                .firstOrNull()
    }

    val SnsUploadUI: Class<*> by wxLazy("SnsUploadUI") {
        classesInCurrentPackage
                .filterByField("$wxPackageName.plugin.sns.ui.LocationWidget")
                .filterByField("$wxPackageName.plugin.sns.ui.SnsUploadSayFooter")
                .firstOrNull()
    }

    val SnsUserUI: Class<*> by wxLazy("SnsUserUI") {
        findClassIfExists("$wxPackageName.plugin.sns.ui.SnsUserUI", wxLoader)
    }
}