package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.chatting

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Classes.MMFragmentActivity
import com.gh0u1l5.wechatmagician.spellbook.util.C
import com.gh0u1l5.wechatmagician.spellbook.util.PackageUtil.findClassesFromPackage

object Classes {
    val ChattingUI: Class<*> by wxLazy("ChattingUI") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.ui.chatting")
                .filterBySuper(MMFragmentActivity)
                .filterByMethod(null, "onRequestPermissionsResult", C.Int, C.StringArray, C.IntArray)
                .firstOrNull()
    }
}