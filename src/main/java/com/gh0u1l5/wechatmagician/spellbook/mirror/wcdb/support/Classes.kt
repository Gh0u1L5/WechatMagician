package com.gh0u1l5.wechatmagician.spellbook.mirror.wcdb.support

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.mirror.wcdb.Package.WECHAT_PACKAGE_SQLITE
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassIfExists

object Classes {
    val SQLiteCancellationSignal: Class<*> by wxLazy("SQLiteCancellationSignal") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.support.CancellationSignal", wxLoader)
    }
}