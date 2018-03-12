package com.gh0u1l5.wechatmagician.spellbook.mirror.wcdb

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.mirror.wcdb.Package.WECHAT_PACKAGE_SQLITE
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findClassIfExists

object Classes {
    val SQLiteErrorHandler: Class<*> by wxLazy("SQLiteErrorHandler") {
        findClassIfExists("$WECHAT_PACKAGE_SQLITE.DatabaseErrorHandler", wxLoader)
    }
}