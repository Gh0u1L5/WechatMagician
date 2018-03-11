package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.plugin.gallery.ui

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val AlbumPreviewUI: Class<*> by wxLazy("AlbumPreviewUI") {
        findClassIfExists("$wxPackageName.plugin.gallery.ui.AlbumPreviewUI", wxLoader)
    }
}